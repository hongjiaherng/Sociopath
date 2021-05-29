package org.sociopath.events;

import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.time.LocalTime;
import java.util.*;

public class SocialActivities {

    private static final Random rd = new Random();
    private static Scanner sc = new Scanner(System.in);

    public static void event1(String teacher, String student, Sociograph graph){
        boolean areFriends = graph.checkRelationship(teacher, student) == Relationship.FRIEND;
        if(!areFriends) {
            System.out.println("Now " + teacher + " will teach " + student);
            System.out.println("Please wait for a while.....");

            /*try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            double repSrc = rd.nextDouble() < 0.5 ? 2 : 10;
            double repDest = rd.nextInt(10) + 1;
            graph.addUndirectedEdge(teacher, student, repSrc, repDest, Relationship.FRIEND) ;
            System.out.println("The result of the teaching is " + (repSrc == 10 ? "successful" : "not successful"));
            System.out.println("So the rep point the new friend give you is : " + repSrc);
            System.out.println("So the rep point you give the new friend is : " + repDest);
            System.out.println("####################");
            System.out.println(graph);
            System.out.println();
        } else {
            System.out.println("They are friends before. You cannot teach a person lab question if he is your friend.");
        }
    }

    public static void event2(Sociograph sociograph, String hostName, String newFriendName) {   // new friend talk to his friends about the host
        if (!hostName.equals(newFriendName)) {
            HashSet<Student> visitedRecord = new HashSet<>();
            visitedRecord.add(sociograph.getStudent(newFriendName));
            visitedRecord.add(sociograph.getStudent(hostName));
            chitchat(sociograph, hostName, newFriendName, visitedRecord);
            System.out.println("All " + newFriendName + "'s friend and " + newFriendName + "'s friends' friends' friends' ... know " + hostName);
        } else {
            System.out.println("hostName & newFriendName can't be the same");
        }
    }

    public static void event3(Sociograph sociograph, String hostName) {
        // List to keep all the students that has intersection with host's lunch time
        List<Student> potentialLunchMates = new ArrayList<>();      // didn't consider the number of ppl that can have lunch with in parallel way (3 persons)
        List<Student> actualLunchMates = new ArrayList<>();         // after considering the number of ppl that can simultaneously to have lunch with
        int totalRepObtained = 0;

        Student host = sociograph.getStudent(hostName);
        host.estimateLunchEnd();

        // Timeslot to keep who are having lunch with host at a certain minute (row array - minute; col arraylist - Student)
        ArrayList<String>[] timeslot = new ArrayList[host.getAvgLunchPeriod()];
        for (int i = 0; i < timeslot.length; i++) {
            timeslot[i] = new ArrayList<>();
        }

        // Add the student who has lunch time that intersect with host's lunch time to the list
        // Filter out those students who have high dive rate (>50)
        // Estimate the lunchEnd of everyone
        for (Student mate : sociograph.getAllStudents()) {
            if (mate.getName().equals(hostName)) {
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
        System.out.println(hostName + "\t\t" + host.getAvgLunchStart() + "\t\t\t" + host.getAvgLunchPeriod() + "\t\t\t\t\t" + host.getEstimatedLunchEnd() + "\t\t\t" + host.getDive());
        potentialLunchMates.forEach(mate -> System.out.println(mate.getName() + "\t\t" + mate.getAvgLunchStart() + "\t\t\t" + mate.getAvgLunchPeriod() + "\t\t\t\t\t" + mate.getEstimatedLunchEnd() + "\t\t\t" + mate.getDive()));

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
        System.out.println("\n" + "Schedule of " + hostName + " in terms of minutes");
        System.out.println("==================================");
        LocalTime time = host.getAvgLunchStart();
        for (int i = 0; i < host.getAvgLunchPeriod(); i++) {
            System.out.println(time + " : " + timeslot[i]);
            time = time.plusMinutes(1);
        }
        System.out.println();

        // Add edge (rep point) to the host after having lunch with those ppl
        for (Student actualMate : actualLunchMates) {
            if (sociograph.hasDirectedEdge(hostName, actualMate.getName())) {
                double newRep = sociograph.getSrcRepRelativeToAdj(hostName, actualMate.getName()) + 1;
                sociograph.setSrcRepRelativeToAdj(hostName, actualMate.getName(), newRep);
            } else {
                sociograph.addDirectedEdge(hostName, actualMate.getName(), 1);
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

    public static void event5(Sociograph sociograph, String you, String crush) {
        // Get the stranger object randomly
        ArrayList<String> possibleStrangers = new ArrayList<>();
        for (Student student : sociograph.getAllStudents()) {
            if (!(student.getName().equals(you) || student.getName().equals(crush))) {
                possibleStrangers.add(student.getName());
            }
        }
        final String stranger = possibleStrangers.get(rd.nextInt(possibleStrangers.size()));

        System.out.println("That stranger: " + stranger);

        // Find all the path between stranger and crush
        List<List<String>> allPaths = sociograph.dfTraversal(stranger, crush);
        System.out.println(allPaths + "\n");

        // Rumors can't spread if the path is empty
        // Start spreading if it's not empty
        if (!allPaths.isEmpty()) {
            // Declare pplKnewSecret to keep the ppl who knows the secret according to their respective path
            List<List<String>> pplKnewSecret = new ArrayList<>();
            for (int i = 0; i < allPaths.size(); i++) {
                pplKnewSecret.add(new ArrayList<>());
            }
            // Add stranger to every path of pplKnewSecret first
            pplKnewSecret.forEach(list -> list.add(stranger));

            // Remove stranger from every path to mark as visited
            allPaths.forEach(list -> list.remove(stranger));

            int day = 1;

            // Spreading process start, this process will stop if crush know the rumor or you stop the rumor successfully
            for (boolean terminate = false; !terminate; day++) {
                System.out.println("Day " + day);
                System.out.println("===================================");

//                System.out.println("Path today " + allPaths);
//                System.out.println("PplKnewSecret today " + pplKnewSecret);

                // If day 1, spreading process not started yet
                if (day == 1) {
                    System.out.println("Stranger " + stranger + " is going to start spreading your secret tomorrow! Get ready!");
                } else {
                    // After day 1, stranger starts to spread the rumors to his neighbouring friends, his friends also will spread
                    // to his own friends in the consecutive days

                    // Return true if the rumor reaches crush, else false
                    boolean reachCrush = spreadRumor(allPaths, crush, pplKnewSecret);

                    // Terminate loop if reachCrush
                    if (reachCrush) {
                        terminate = true;
                        continue;
                    }
                }

                // Input ppl to convince
                System.out.print("Who would you want to convince today?: ");
                String pplToConvince = sc.next();

                // Return true if all rumor chains are broken
                boolean allChainStopped = convince(allPaths, pplToConvince, pplKnewSecret, crush);

                // Terminate loop if all rumor chains are broken
                if (allChainStopped) {
                    terminate = true;
                    System.out.println("You've break all the rumors chain");
                    continue;
                }
                System.out.println();
            }
        } else {    // Exit if the path is empty
            System.out.println("No path from " + stranger + " to " + crush);
        }

    }

    private static void chitchat(Sociograph sociograph, String hostName, String newFriendName, HashSet<Student> visitedRecord) {
        List<Student> friendsOfNewFriend = sociograph.neighbours(newFriendName);
        friendsOfNewFriend.removeAll(visitedRecord);
        if (friendsOfNewFriend.isEmpty()) {
            return;
        } else {
            for (Student friend : friendsOfNewFriend) {
                if (visitedRecord.contains(friend)) {
                    continue;
                }
                double hostRepRelativeToFriend = 0;
                if (Math.random() < 0.5) {  // if talk bad
                    hostRepRelativeToFriend -= Math.abs(sociograph.getSrcRepRelativeToAdj(hostName, newFriendName));
                } else {    // if talk good
                    hostRepRelativeToFriend += (sociograph.getSrcRepRelativeToAdj(hostName, newFriendName) / 2.0);
                }

                if (sociograph.hasDirectedEdge(hostName, friend.getName())) {
                    hostRepRelativeToFriend += sociograph.getSrcRepRelativeToAdj(hostName, friend.getName());
                    sociograph.setSrcRepRelativeToAdj(hostName, friend.getName(), hostRepRelativeToFriend);
                } else {
                    sociograph.addDirectedEdge(hostName, friend.getName(), hostRepRelativeToFriend);
                }
                // Update graph (update student's properties, add edge)
                visitedRecord.add(friend);
                System.out.println("Propagated: " + friend.getName());
                System.out.println(sociograph);
                System.out.println();
                chitchat(sociograph, hostName, friend.getName(), visitedRecord);
            }
        }
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

    private static boolean spreadRumor(List<List<String>> allPaths, String crush, List<List<String>> pplKnewSecret) {
//        System.out.println("Inside before removing " + allPaths);
        for (int i = 0; i < allPaths.size(); i++) {
            if (allPaths.get(i).contains("stop")) {
                continue;
            }
            String newlySpread = allPaths.get(i).remove(0);
            pplKnewSecret.get(i).add(newlySpread);
            if (newlySpread.equals(crush)) {
                System.out.println(pplKnewSecret.get(i).get(pplKnewSecret.get(i).size() - 2) + " told your crush, " + crush + " ...");
                return true;
            }
            System.out.println(pplKnewSecret.get(i).get(pplKnewSecret.get(i).size() - 2) + " told " + newlySpread + " your secret!");
        }
//        System.out.println("Inside after removing " + allPaths);
        return false;
    }

    private static boolean convince(List<List<String>> allPaths, String pplToConvince, List<List<String>> pplKnewSecret, String crush) {
        HashSet<String> pplKnewSecretSet = new HashSet<>();
        pplKnewSecret.forEach(list -> pplKnewSecretSet.addAll(list));
        if (allPaths.stream().noneMatch(path -> path.contains(pplToConvince))) {
            System.out.println("You convince the wrong person!");
        } else if (pplToConvince.equals(crush)) {
            System.out.println("You can't convince your crush directly!");
        } else if (pplKnewSecretSet.contains(pplToConvince)) {
            System.out.println("You can't convince the person who already know your secret!");
        } else {
            boolean someoneConvinced = false;
            for (List<String> path : allPaths) {
                if (path.contains("stop")) continue;

                if (path.contains(pplToConvince)) {
                    someoneConvinced = true;
                    path.add("stop");
                    StringBuilder chain = new StringBuilder("[");
                    path.forEach(v -> {
                        if (v.equals("stop"))
                            chain.append("]");
                        else if (v.equals(crush))
                            chain.append(v);
                        else
                            chain.append(v).append(", ");
                    });
                    System.out.println("This chain " + chain + " stopped");
                }
            }

            if (someoneConvinced) {
                System.out.print("You convinced the right person, " + pplToConvince + "! ");
                if (allPaths.stream().allMatch(v -> v.contains("stop"))) {
                    System.out.println();
                } else {
                    System.out.println("But there's still someone to convince.");
                }
            }
        }
        return allPaths.stream().allMatch(path -> path.contains("stop"));
    }

}
