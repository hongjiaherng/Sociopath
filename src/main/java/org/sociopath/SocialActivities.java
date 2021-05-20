package org.sociopath;

import java.util.ArrayList;
import java.util.HashSet;

public class SocialActivities {

    public static void event1(Sociograph sociograph, String hostName, String newFriendName) {   // new friend talk to his friends about the host
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

}
