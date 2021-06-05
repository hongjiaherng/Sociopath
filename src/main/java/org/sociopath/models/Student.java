package org.sociopath.models;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.sociopath.utils.HashMapConverter;
import org.sociopath.utils.LocalTimeArrayConverter;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@NodeEntity
public class Student {

    @Id @GeneratedValue
    private Long id;

    private static final Random rand = new Random();  // Set seed here if you want a fixed random values

    @Property(name = "Name")
    private String name;

    @Property(name = "Dive Rate")
    private double dive;                        // 0 < dive < 100

    @Property(name = "Lunch Start Time") @Convert(LocalTimeArrayConverter.class)
    private final LocalTime[] lunchStart = new LocalTime[3];               // 1100 <= lunchStart <= 1400

    @Property(name = "Lunch Period")
    private final int[] lunchPeriod = new int[3];                    // 5 < lunchPeriod < 60

    @Property(name = "repPoints") @Convert(HashMapConverter.class)
    private HashMap<String, Double> repPoints;  // 1 <= rep <= 10

    @Relationship(type = "FRIEND")
    private Set<Student> friends;

    @Relationship(type = "ENEMY")
    private Set<Student> enemies;

    @Relationship(type = "NONE")
    private Set<Student> nones;

    private transient int avgLunchPeriod;
    private transient LocalTime avgLunchStart;
    private transient LocalTime lunchEnd;

    public Student(){}

    public Student(String name) {
        this.name = name;
        this.dive = Math.round((rand.nextDouble() * 99 + 1) * 100.0) / 100.0;
        this.repPoints = new HashMap<>();
        this.friends = new HashSet<>();
        this.enemies = new HashSet<>();
//        this.crushes = new HashSet<>();
        this.nones = new HashSet<>();
        for (int i = 0; i < lunchStart.length; i++) {
            this.lunchStart[i] = LocalTime.of(11, 0).plusMinutes(rand.nextInt(181));
        }
        for (int i = 0; i < lunchPeriod.length; i++) {
            this.lunchPeriod[i] = rand.nextInt(55) + 5;
        }
    }

    public void estimateLunchEnd() {
        long totalSeconds = 0;
        int totalMins = 0;
        for (LocalTime localTime : lunchStart) {
            totalSeconds += localTime.toSecondOfDay();
        }
        for (int j : lunchPeriod) {
            totalMins += j;
        }
        avgLunchPeriod = totalMins / 3;
        avgLunchStart = LocalTime.ofSecondOfDay(totalSeconds / 3).truncatedTo(ChronoUnit.MINUTES);
        this.lunchEnd = avgLunchStart.plusMinutes(avgLunchPeriod);
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

    public LocalTime getAvgLunchStart() {
        return avgLunchStart;
    }

    public LocalTime getEstimatedLunchEnd() {
        return lunchEnd;
    }

    public int getAvgLunchPeriod() {
        return avgLunchPeriod;
    }

    void setRepPoints(String adjName, double repRelativeToAdj) {
        repPoints.put(adjName, repRelativeToAdj);
    }

    public HashMap<String, Double> getRepPoints() {
        return repPoints;
    }

    void setRelationship(Student person, org.sociopath.models.Relationship prevRelation, org.sociopath.models.Relationship newRelation) {
        // if prevRelation is null add relationship, if not null, change relationship
        if (prevRelation != null) { // change relationship
            switch (prevRelation) {
                case NONE:
                    nones.remove(person);
                    break;
                case FRIEND:
                    friends.remove(person);
                    break;
                case ENEMY:
                    enemies.remove(person);
                    break;
            }
        }
        switch (newRelation) {
            case NONE:
                nones.add(person);
                break;
            case FRIEND:
                friends.add(person);
                break;
            case ENEMY:
                enemies.add(person);
                break;
        }
    }

    void deleteRelationship(Student person, org.sociopath.models.Relationship prevRelation) {
        // Remove person from nones / friends / enemies HashSet
        switch (prevRelation) {
            case NONE:
                nones.remove(person);
            case FRIEND:
                friends.remove(person);
            case ENEMY:
                enemies.remove(person);
        }

        // Remove person from rep points HashMap
        this.repPoints.remove(person.getName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name").append("\t\t\t: ").append(name).append("\n");
        sb.append("Dive").append("\t\t\t: ").append(dive).append("\n");
        sb.append("Lunch start").append("\t\t: ").append(Arrays.toString(lunchStart)).append("\n");
        sb.append("Lunch period").append("\t: ").append(Arrays.toString(lunchPeriod)).append("\n");
        sb.append("Lunch end").append("\t\t: ").append(lunchEnd).append("\n");
        sb.append("Rep points").append("\t\t: ").append(repPoints).append("\n");
        sb.append("Friends").append("\t\t\t: [ ");
        friends.forEach(friend -> {
            sb.append(friend.getName()).append(" ");
        });
        sb.append("]\n");
        sb.append("Enemies").append("\t\t\t: [ ");
        enemies.forEach(enemy -> {
            sb.append(enemy.getName()).append(" ");
        });
        sb.append("]\n");
//        sb.append("Crushes").append("\t\t\t: [ ");
//        crushes.forEach(crush -> {
//            sb.append(crush.getName()).append(" ");
//        });
//        sb.append("]\n");
        sb.append("Nones").append("\t\t\t: [ ");
        nones.forEach(none -> {
            sb.append(none.getName()).append(" ");
        });
        sb.append("]");
        return sb.toString();
    }
}
