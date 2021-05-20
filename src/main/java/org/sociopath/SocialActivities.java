package org.sociopath;

import java.util.Random;

public class SocialActivities {

    private static final Random rd = new Random();

    public static void event1(String teacher, String student, Sociograph graph){
        boolean areFriends = graph.hasUndirectedEdge(teacher, student) || graph.hasDirectedEdge(student, teacher) || graph.hasDirectedEdge(teacher, student);
        if(!areFriends){
            System.out.println("Now " + teacher + " will teach " + student);
            System.out.println("Please wait for a while.....");

            /*try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            double rep = rd.nextDouble() < 0.5 ? 2 : 10;
            graph.addUndirectedEdge(teacher, student, rd.nextInt(10) + 1, rep) ;
            System.out.println("The result of the teaching is " + (rep == 10 ? "successful" : "not successful"));
            System.out.println("So the rep point the new friend give you is : " + rep);
            System.out.println("####################");
            System.out.println(graph);

        }

        else
            System.out.println("They are friends before. You cannot teach a person lab question if he is your friend.");

    }

}
