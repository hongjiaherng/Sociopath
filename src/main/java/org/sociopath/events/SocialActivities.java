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
        ArrayList<Student> lunchMates = new ArrayList<>();
        Student host = sociograph.getStudent(hostName);
        LocalTime hostStart = host.getLunchStart();
        LocalTime hostEnd = host.getLunchEnd();

        // Get students who have low dive rate & their lunch times are within host's lunch time
        for (Student lunchMate : sociograph.getAllStudents()) {
            if (lunchMate.getName().equals(hostName)) {
                continue;
            }
            if (lunchMate.getDive() <= 50) {  // Make sure the student is reliable
                LocalTime mateStart = lunchMate.getLunchStart();
                LocalTime mateEnd = lunchMate.getLunchEnd();
                if (mateStart.isBefore(hostEnd) && mateEnd.isAfter(hostStart)) {
                    lunchMates.add(lunchMate);
                }
            }
        }

        if (lunchMates.isEmpty()) {
            System.out.println("No lunch mates available due to the time and also you don't want diver");
            return;
        }

        // Sort the list in ascending order according to lunchStart, sort by lunchEnd if lunchStart is the same
        for (int i = 0; i < lunchMates.size() - 1; i++) {
            for (int j = 0; j < lunchMates.size() - i - 1; j++) {
                if (lunchMates.get(j).getLunchStart().isAfter(lunchMates.get(j + 1).getLunchStart())) {
                    Student temp = lunchMates.get(j);
                    lunchMates.set(j, lunchMates.get(j + 1));
                    lunchMates.set(j + 1, temp);
                } else if (lunchMates.get(j).getLunchStart().compareTo(lunchMates.get(j + 1).getLunchStart()) == 0) {
                    if (lunchMates.get(j).getLunchEnd().isAfter(lunchMates.get(j + 1).getLunchEnd())) {
                        Student temp = lunchMates.get(j);
                        lunchMates.set(j, lunchMates.get(j + 1));
                        lunchMates.set(j + 1, temp);
                    }
                }
            }
        }

        System.out.println("Original Schedule for " + hostName);
        System.out.println("Host " + hostName + "\t| " + hostStart + "\t-> " + hostEnd);
        for (Student lunchMate : lunchMates) {
            System.out.println(lunchMate.getName() + "\t\t| " + lunchMate.getLunchStart() + "\t-> " + lunchMate.getLunchEnd());
        }
        System.out.println();

        // Obtain a map which maps student names to their respective lunch time with the host
        Map<String, LocalTime[]> hostSchedule = new LinkedHashMap<>();

        NormalCase:
        for (int i = 0; i < lunchMates.size(); i++) {
            String mateName = lunchMates.get(i).getName();
            LocalTime mateStart = lunchMates.get(i).getLunchStart();
            LocalTime mateEnd = lunchMates.get(i).getLunchEnd();
            boolean ignoreMiddle = false;  // A variable to make sure it enter the correct condition when multiple same lunchStart is occur at middle of the list

            // Enter this loop when more than 1 lunchStart is the same for some student
            while (lunchMates.size() > 1 && i < lunchMates.size() - 1 && mateStart.compareTo(lunchMates.get(i + 1).getLunchStart()) == 0) {
//                System.out.println(i + mateName + " " + mateStart + " " + mateEnd);
                if (i == 0) {   // First process of having multiple same lunchStart at first few lines
                    hostSchedule.put(mateName, new LocalTime[]{maxTime(mateStart, hostStart), maxTime(mateEnd, lunchMates.get(i + 1).getLunchStart())});
                } else if (!ignoreMiddle) {     // First process of having multiple same lunchStart at middle lines
                    hostSchedule.put(mateName, new LocalTime[]{mateStart, maxTime(mateEnd, lunchMates.get(i + 1).getLunchStart())});
                } else {    // Default process for all
                    hostSchedule.put(mateName, new LocalTime[]{ lunchMates.get(i - 1).getLunchEnd(), maxTime(mateEnd, lunchMates.get(i + 1).getLunchStart()) });
                }
                i++;
                mateName = lunchMates.get(i).getName();
                mateStart = lunchMates.get(i).getLunchStart();
                mateEnd = lunchMates.get(i).getLunchEnd();

                // Enter here when all the same lunchStart of students have been exhausted but left a final one to execute
                if (i == lunchMates.size() - 1) {   // When it's the last element in the list
                    hostSchedule.put(mateName, new LocalTime[]{ lunchMates.get(i - 1).getLunchEnd(), minTime(hostEnd, mateEnd)});
                    continue NormalCase;
                } else if (mateStart.compareTo(lunchMates.get(i + 1).getLunchStart()) != 0) {   // When it's the last repeated lunchStart student element (but not last element in the list)
//                    System.out.println(i + mateName + " " + mateStart + " " + mateEnd);
                    hostSchedule.put(mateName, new LocalTime[]{ lunchMates.get(i - 1).getLunchEnd(), minTime(mateEnd, lunchMates.get(i + 1).getLunchStart()) });
                    continue NormalCase;
                }
                ignoreMiddle = true;
            }
//            System.out.println(i + mateName + " " + mateStart + " " + mateEnd);

            if (lunchMates.size() == 1) {
                hostSchedule.put(mateName, new LocalTime[]{maxTime(mateStart, hostStart), minTime(mateEnd, hostEnd)});
            } else if (i == 0) {
                LocalTime nextMateStart = lunchMates.get(i + 1).getLunchStart();
                hostSchedule.put(mateName, new LocalTime[]{maxTime(mateStart, hostStart), minTime(mateEnd, nextMateStart)});
            } else if (i == lunchMates.size() - 1) {
                hostSchedule.put(mateName, new LocalTime[]{mateStart, minTime(hostEnd, mateEnd)});
            } else {
                LocalTime nextMateStart = lunchMates.get(i + 1).getLunchStart();
                hostSchedule.put(mateName, new LocalTime[]{mateStart, minTime(mateEnd, nextMateStart)});
            }
        }

        System.out.println("Adjusted Schedule for " + hostName);
        System.out.println("Host " + hostName + "\t| " + hostStart + "\t-> " + hostEnd);
        for (Map.Entry<String, LocalTime[]> entry : hostSchedule.entrySet()) {
            System.out.println(entry.getKey() + "\t\t| " + entry.getValue()[0] + "\t-> " + entry.getValue()[1]);
        }

        System.out.println();
        for (String lunchMate : hostSchedule.keySet()) {
            if (sociograph.hasDirectedEdge(hostName, lunchMate)) {
                double oldRep = sociograph.getStudent(hostName).getRepPoints().get(lunchMate);
                sociograph.setDirectedEdgeWeight(hostName, lunchMate, oldRep + 1);
            } else {
                sociograph.addDirectedEdge(hostName, lunchMate, 1);
            }
        }

        System.out.println(hostName + " had lunch with " + lunchMates.size() + " persons, " + lunchMates.size() + " rep points earned!");

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
        List<List<String>> allPaths = sociograph.dfs(stranger, crush);
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
        ArrayList<Student> friendsOfNewFriend = sociograph.neighbours(newFriendName);
        friendsOfNewFriend.removeAll(visitedRecord);
        Student host = sociograph.getStudent(hostName);
        if (friendsOfNewFriend.isEmpty()) {
            return;
        } else {
            for (Student friend : friendsOfNewFriend) {
                if (visitedRecord.contains(friend)) {
                    continue;
                }
                double repRelativeToHost = 0;
                if (Math.random() < 0.5) {  // if talk bad
                    repRelativeToHost -= Math.abs(host.getRepPoints().get(newFriendName));
                } else {    // if talk good
                    repRelativeToHost += (host.getRepPoints().get(newFriendName) / 2.0);
                }

                if (sociograph.hasDirectedEdge(hostName, friend.getName())) {
                    repRelativeToHost += sociograph.getDirectedEdgeWeight(hostName, friend.getName());
                    sociograph.setDirectedEdgeWeight(hostName, friend.getName(), repRelativeToHost);
                } else {
                    sociograph.addDirectedEdge(hostName, friend.getName(), repRelativeToHost);
                }
                visitedRecord.add(friend);
                System.out.println("Propagated: " + friend.getName());
                System.out.println(sociograph);
                System.out.println();
                chitchat(sociograph, hostName, friend.getName(), visitedRecord);
            }
        }
    }

    private static LocalTime minTime(LocalTime t1, LocalTime t2) {
        if (t1.isAfter(t2)) {
            return t2;
        } else if (t1.isBefore(t2)) {
            return t1;
        } else {
            return t1;
        }
    }

    private static LocalTime maxTime(LocalTime t1, LocalTime t2) {
        if (t1.isAfter(t2)) {
            return t1;
        } else if (t1.isBefore(t2)) {
            return t2;
        } else {
            return t1;
        }
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
