package org.sociopath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class SocialActivities {

    private static final Random rd = new Random();

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
            event2(graph, teacher, student);
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

//    public static void event3(Sociograph sociograph, String hostName){
//        int n = 0;
//        ArrayList<Student> myLunchMates = new ArrayList<>();
//        ArrayList<Student> list = sociograph.getAllStudents();
//        list.remove(sociograph.getStudent(hostName));
//        Student host = sociograph.getStudent(hostName);
//
//        host.setLunchStart(12,00);
//        host.setLunchPeriod(180);
//        host.setLunchEnd(14,00);
//
//        System.out.println("My lunch time");
//        System.out.println("==============");
//        System.out.println(host);
//        System.out.println();
//
//        if(list.isEmpty()){
//            return;
//        }
//        else{
//            for (Student student : list) {
//                if(n > 7) {// i dont know why need 7, if i use 3 i will produce one lunchMate only
//                    continue;
//                }
//                else{
//                    double newRepPoint = 0;
//                    if (student.getDive() <= 50) {
//                        if (host.getLunchStart().isAfter(student.getLunchStart())) {
//                            continue;
//                        } else if (host.getLunchStart().isBefore(student.getLunchStart())) {
//                            if (host.getLunchEnd().isAfter(student.getLunchEnd())) {
//                                myLunchMates.add(student);
//                                if (sociograph.hasDirectedEdge(hostName, student.getName())) {
//                                    newRepPoint = host.getRepPoints().get(student.getName()) + 1;
//                                    sociograph.setDirectedEdgeWeight(hostName, student.getName(), newRepPoint);
//                                } else {
//                                    sociograph.addDirectedEdge(hostName, student.getName(), 1);
//                                }
//                            }
//                        }
//                    }
////                    n++;
//                }
//                n++;
//            }
//            System.out.println("My lunch mates");
//            System.out.println("==============");
//            for(Student mate : myLunchMates) {
//                System.out.println(mate);
//                System.out.println();
//            }
//        }
//    }
}
