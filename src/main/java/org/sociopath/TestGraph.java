package org.sociopath;

import java.util.ArrayList;
import static org.sociopath.SocialActivities.event1;

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

        System.out.println(sociograph);
        System.out.println();
        ArrayList<Student> arrayList = sociograph.getAllStudents();
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(arrayList.get(i));
            System.out.println();
        }

        event1("B" , "D", sociograph);

        // test whether if they are friends then can the event run??
        //event1("A", "B", sociograph);

        // test if whether if they one of them know the other but the other do not know him
        // whether the event can run
        // B -> D
        // so the event cannot run
        //sociograph.addDirectedEdge("B", "D", 3);
        //event1("B" , "D", sociograph);

    }
}
