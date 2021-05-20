package org.sociopath;

import java.util.ArrayList;

public class TestGraph {
    public static void main(String[] args) {
        Sociograph sociograph = new Sociograph();
//                            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}
        String[] nodesToAdd = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

        for (String node: nodesToAdd) {
            sociograph.addVertex(node);
        }

        sociograph.addUndirectedEdge("A", "G", 3, 4);
        sociograph.addUndirectedEdge("A", "B", 8, 5);
        sociograph.addUndirectedEdge("B", "F", 7, 9);
        sociograph.addUndirectedEdge("B", "E", 2, 6);
        sociograph.addUndirectedEdge("B", "C", 4, 5);
        sociograph.addUndirectedEdge("H", "D", 7, 10);
        sociograph.addUndirectedEdge("D", "J", 7, 7);
        sociograph.addUndirectedEdge("J", "I", 5, 6);

//        System.out.println(sociograph);
//        System.out.println();
//        ArrayList<Student> arrayList = sociograph.getAllStudents();
//        for (int i = 0; i < arrayList.size(); i++) {
//            System.out.println(arrayList.get(i));
//            System.out.println();
//        };

        // Event 2 Tests
        // Test simple case, G make new friend with H (H connects to a path, one direction propagate)
//        sociograph.addDirectedEdge("G", "H", 10);
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 1");
//        SocialActivities.event1(sociograph, "G", "H");

        // Test simple case, G make new friend with D (D connects to H and J, two direction propagate)
//        sociograph.addDirectedEdge("G", "D", 10);
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 2");
//        SocialActivities.event1(sociograph, "G", "D");

        // Test G connects H when H, D, J, I is a cycle (make sure won't go into infinity loop)
//        sociograph.addUndirectedEdge("I", "H", 1, 1); // connect it to be a cycle
//        sociograph.addDirectedEdge("G", "H", 10);            // G makes friend with H (G has 10 rep relative to H)
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 3");
//        SocialActivities.event1(sociograph, "G", "H");           // H tells his friends about G

        // Test G connects C in the same group (make sure rep point relative to already known friend A change)
//        sociograph.addDirectedEdge("G", "C", 10);
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 4");
//        SocialActivities.event1(sociograph, "G", "C");
    }
}
