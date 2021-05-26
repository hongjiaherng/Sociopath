package org.sociopath;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Student {

    private static Random rand = new Random(1234);  // Set seed here if you want a fixed random values
    private String name;
    private double dive;                        // 0 < dive < 100
    private LocalTime lunchStart;               // 1100 <= lunchStart <= 1400
    private int lunchPeriod;                    // 5 < lunchPeriod < 60
    private LocalTime lunchEnd;
    private HashMap<String, Double> repPoints;  // 1 <= rep <= 10
    private HashSet<String> friends;

    public Student(String name) {
        this.name = name;
//        this.dive = Math.round((rand.nextDouble() * 99 + 1) * 100.0) / 100.0;
        this.dive = 30;
        this.lunchStart = LocalTime.of(11, 0).plusMinutes(rand.nextInt(181));
        this.lunchPeriod = rand.nextInt(55) + 5;
        this.repPoints = new HashMap<>();
        this.friends = new HashSet<>();
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

    public HashMap<String, Double> getRepPoints() {
        return repPoints;
    }

    public void setRepPoints(HashMap<String, Double> repPoints) {
        this.repPoints = repPoints;
    }

    public HashSet<String> getFriends() {
        return friends;
    }

    public void setFriends(HashSet<String> friends) {
        this.friends = friends;
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
        sb.append("Friends").append("\t\t\t: ").append(friends);
        return sb.toString();
    }
}
