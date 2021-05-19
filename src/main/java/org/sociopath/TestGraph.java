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

        System.out.println(sociograph);
        System.out.println();
//        ArrayList<Student> arrayList = sociograph.getAllStudents();
//        for (int i = 0; i < arrayList.size(); i++) {
//            System.out.println(arrayList.get(i));
//            System.out.println();
//        }

        Event3.event3(sociograph,"A");
        System.out.println(sociograph);
    }
}
