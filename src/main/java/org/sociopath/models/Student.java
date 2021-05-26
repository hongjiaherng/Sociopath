package org.sociopath.models;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.sociopath.utils.HashMapConverter;
import org.sociopath.utils.LocalTimeConverter;

import java.time.LocalTime;
import java.util.*;

@NodeEntity
public class Student {

    @Id
    @GeneratedValue
    private Long id;

    private static final Random rand = new Random();  // Set seed here if you want a fixed random values

    @Property(name = "Name")
    private String name;

    @Property(name = "Dive Rate")
    private double dive;                        // 0 < dive < 100

    @Property(name = "Lunch Start Time") @Convert(LocalTimeConverter.class)
    private LocalTime lunchStart;               // 1100 <= lunchStart <= 1400

    @Property(name = "Lunch Period")
    private int lunchPeriod;                    // 5 < lunchPeriod < 60

    @Property(name = "Lunch End Time") @Convert(LocalTimeConverter.class)
    private LocalTime lunchEnd;

    @Property(name = "repPoints") @Convert(HashMapConverter.class)
    private HashMap<String, Double> repPoints;  // 1 <= rep <= 10

    @Relationship(type = "FRIENDS")
    private List<Student> friends;

    public Student(){}

    public Student(String name) {
        this.name = name;
        this.dive = Math.round((rand.nextDouble() * 99 + 1) * 100.0) / 100.0;
        this.lunchStart = LocalTime.of(11, 0).plusMinutes(rand.nextInt(181));
        this.lunchPeriod = rand.nextInt(55) + 5;
        this.repPoints = new HashMap<>();
        this.friends = new LinkedList<>();
        this.lunchEnd = lunchStart.plusMinutes(lunchPeriod);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDive() {
        return dive;
    }

    public void setDive(double dive) {
        this.dive = dive;
    }

    public LocalTime getLunchStart() {
        return lunchStart;
    }

    public LocalTime getLunchEnd() {
        return lunchEnd;
    }

    public void setLunchStart(int hour, int minute) {
        this.lunchStart = LocalTime.of(hour, minute);
        this.lunchEnd = lunchStart.plusMinutes(lunchPeriod);
    }

    public int getLunchPeriod() {
        return lunchPeriod;
    }

    public void setLunchPeriod(int lunchPeriod) {
        this.lunchPeriod = lunchPeriod;
        this.lunchEnd = lunchStart.plusMinutes(lunchPeriod);
    }

    void setRepPoints(String adjName, double repRelativeToAdj) {
        repPoints.put(adjName, repRelativeToAdj);
    }

    public String getRepPoints() {
        return repPoints.toString();
    }

    void addFriend(Student newFriend) {
        if (!friends.contains(newFriend)) {
            friends.add(newFriend);
        }
    }

    void unfriend(Student newFriend) {
        friends.remove(newFriend);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name").append("\t\t\t: ").append(name).append("\n");
        sb.append("Dive").append("\t\t\t: ").append(dive).append("\n");
        sb.append("Lunch start").append("\t\t: ").append(lunchStart).append("\n");
        sb.append("Lunch period").append("\t: ").append(lunchPeriod).append("\n");
        sb.append("Lunch end").append("\t\t: ").append(lunchEnd).append("\n");
        sb.append("Rep points").append("\t\t: ").append(repPoints).append("\n");
        sb.append("Friends\t\t\t: [");
        for(int i =0; i<friends.size(); i++){
            if(i==friends.size() - 1)
                sb.append(friends.get(i).getName());
            else
                sb.append(friends.get(i).getName()).append(", ");
        }

        sb.append("]");
        return sb.toString();
    }
}
