package org.sociopath.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Event3Controller {
    /**
     * 1. estimate lunchEnd
     * 2. display a list of possible lunch mates with their lunch details
     * 3. computer your lunch schedule
     * 4. Display the lunch schedule
     * 5. Ask if really wanna have lunch with them
     */
    private static GraphSimulationController canvasRef = MainPageController.canvasRef;

    // List to keep all the students that has intersection with host's lunch time
    private static List<Student> potentialLunchMates = new ArrayList<>();      // didn't consider the number of ppl that can have lunch with in parallel way (3 persons)
    private static List<Student> actualLunchMates = new ArrayList<>();         // after considering the number of ppl that can simultaneously to have lunch with
    private static ArrayList<String>[] timeslot;
    private static Student host;
    private static int totalRepObtained;

    public static void event3Prompt(Sociograph sociograph, GraphSimulationController.VertexFX hostVertexFX) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);
        alert.setOnHidden(e -> {    // Important! to make sure the state is changed
            canvasRef.markEventEnded();
        });

        if (hostVertexFX == null) {
            alert.setContentText("Please select a student!");
            alert.show();
        } else if (sociograph.getSize() < 2) {
            alert.setContentText("This event needs at least 2 students in your graph!");
            alert.show();
        } else {
            String descriptionTxt = "Who doesn’t want to feel respected? As a university student, you are ready to build " +
                    "up your reputation. One of the cores of human relationships is food. People like food, and if they usually " +
                    "see you when there is food, they are more likely to like you. If you want to build a good relationship with " +
                    "someone, try to have lunch with them. But you want to be efficient. You don’t want to befriend any assignment " +
                    "diver. You want to maximize the amount of reputation you can gain by befriending people with high reliability. " +
                    "To do this, you will estimate their lunch starting and ending time. Use these to find the maximum reputation you can " +
                    "obtain in any day given that you can only have lunch with at most 3 person at one time. Each person that had lunch " +
                    "with you will increase your rep with them by 1 point.";

            String title = "Event 3 - Your road to glory (Parallel farming)";
            String headerText = "Description";
            Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, headerText, descriptionTxt);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                potentialLunchMates.clear();
                actualLunchMates.clear();
                timeslot = null;
                totalRepObtained = 0;
                host = sociograph.getStudent(hostVertexFX.nameText.getText());
                event2Execution(sociograph);
            }
            canvasRef.markEventEnded();
        }
    }

    private static void event2Execution(Sociograph sociograph) {

        estimateLunchEnd(sociograph);
        if (potentialLunchMates.size() > 0) {
            arrangeLunchSchedule(sociograph);
            startLunch(sociograph);
        } else {
            // Dialog to tell no potential lunch mate
        }
    }

    private static void estimateLunchEnd(Sociograph sociograph) {
        host.estimateLunchEnd();

        // Timeslot to keep who are having lunch with host at a certain minute (row array - minute; col arraylist - Student)
        timeslot = new ArrayList[host.getAvgLunchPeriod()];
        for (int i = 0; i < timeslot.length; i++) {
            timeslot[i] = new ArrayList<>();
        }

        // Add the student who has lunch time that intersect with host's lunch time to the list
        // Filter out those students who have high dive rate (>50)
        // Estimate the lunchEnd of everyone
        for (Student mate : sociograph.getAllStudents()) {
            if (mate.getName().equals(host.getName())) {
                continue;
            }
            mate.estimateLunchEnd();
            if (mate.getAvgLunchStart().isBefore(host.getEstimatedLunchEnd()) &&
                    mate.getEstimatedLunchEnd().isAfter(host.getAvgLunchStart()) &&
                    mate.getDive() <= 50) {    // Turn diving rate filter off first
                potentialLunchMates.add(mate);
            }
        }

        // The method stops if there's no ppl to have lunch with
        if (potentialLunchMates.size() == 0) {
            System.out.println("You don't have people to have lunch with due to time constraint and their high diving rate");
            return;
        }

        // Sort the student with avgLunchStart, if avgLunchStart is same, use estimatedLunchEnd instead (ascending)
        potentialLunchMates.sort((mate1, mate2) -> {
            if (mate1.getAvgLunchStart().isBefore(mate2.getAvgLunchStart())) {
                return -1;
            } else if (mate1.getAvgLunchStart().isAfter(mate2.getAvgLunchStart())) {
                return 1;
            } else {
                if (mate1.getEstimatedLunchEnd().isBefore(mate2.getEstimatedLunchEnd())) {
                    return -1;
                } else if (mate1.getEstimatedLunchEnd().isAfter(mate2.getEstimatedLunchEnd())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // Display all lunch time for everyone
        System.out.println("Lunch time for all potential lunch mates");
        System.out.println("==================================");
        System.out.println("name\tavg_lunch_start\tavg_lunch_period\test_lunch_end\tdiving rate (<=50)");
        System.out.println(host.getName() + "\t\t" + host.getAvgLunchStart() + "\t\t\t" + host.getAvgLunchPeriod() + "\t\t\t\t\t" + host.getEstimatedLunchEnd() + "\t\t\t" + host.getDive());
        potentialLunchMates.forEach(mate -> System.out.println(mate.getName() + "\t\t" + mate.getAvgLunchStart() + "\t\t\t" + mate.getAvgLunchPeriod() + "\t\t\t\t\t" + mate.getEstimatedLunchEnd() + "\t\t\t" + mate.getDive()));
    }

    private static void arrangeLunchSchedule(Sociograph sociograph) {
        // Add all the satisfied mate to the timeslot by considering the number of ppl currently in the slot
        for (Student mate : potentialLunchMates) {
            for (int i = computeNthMinute(host.getAvgLunchStart(), mate.getAvgLunchStart());
                 i < computeNthMinute(host.getAvgLunchStart(), mate.getEstimatedLunchEnd());
                 i++) {
                if (i < timeslot.length && timeslot[i].size() < 3) {
                    timeslot[i].add(mate.getName());
                    if (!actualLunchMates.contains(mate)) {
                        actualLunchMates.add(mate);
                    }
                }
            }
        }

        // Display schedule in terms of minutes
        System.out.println("\n" + "Schedule of " + host.getName() + " in terms of minutes");
        System.out.println("==================================");
        LocalTime time = host.getAvgLunchStart();
        for (int i = 0; i < host.getAvgLunchPeriod(); i++) {
            System.out.println(time + " : " + timeslot[i]);
            time = time.plusMinutes(1);
        }
        System.out.println();
    }

    private static void startLunch(Sociograph sociograph) {
        // Add edge (rep point) to the host after having lunch with those ppl
        for (Student actualMate : actualLunchMates) {
            if (sociograph.hasDirectedEdge(host.getName(), actualMate.getName())) {
                double newRep = sociograph.getSrcRepRelativeToAdj(host.getName(), actualMate.getName()) + 1;
                sociograph.setSrcRepRelativeToAdj(host.getName(), actualMate.getName(), newRep);
            } else {
                sociograph.addDirectedEdge(host.getName(), actualMate.getName(), 1, Relationship.NONE);
            }
            totalRepObtained++;
        }

        // Print out result
        StringBuilder sb = new StringBuilder();
        sb.append("Your obtained total ").append(totalRepObtained).append(" reputation points after having lunch with ");
        for (int i = 0; i < actualLunchMates.size(); i++) {
            if (actualLunchMates.size() == 2) {
                sb.append(actualLunchMates.get(0).getName()).append(" and ").append(actualLunchMates.get(1).getName());
                break;
            } else if (i == actualLunchMates.size() - 1) {
                sb.append("and ").append(actualLunchMates.get(i).getName());
            } else {
                sb.append(actualLunchMates.get(i).getName()).append(", ");
            }
        }

        System.out.println(sb);
    }

    private static int computeNthMinute(LocalTime hostTime, LocalTime mateTime) {
        if (mateTime.isBefore(hostTime)) {
            return 0;
        }
        LocalTime resultDuration = mateTime.minusSeconds(hostTime.toSecondOfDay());
        int minute = resultDuration.getMinute();
        int hour = resultDuration.getHour() * 60;
        int nthMinute = minute + hour;
        return nthMinute;
    }

}
