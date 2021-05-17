package org.sociopath;

import java.util.ArrayList;

public class TestGraph {
    public static void main(String[] args) {
        Sociograph sociograph = new Sociograph();
        String[] nodesToAdd = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        for (String node: nodesToAdd) {
            sociograph.addNode(node);
        }

        sociograph.addEdge("A", "G", 3, 4);
        sociograph.addEdge("A", "B", 8, 5);
        sociograph.addEdge("B", "F", 7, 9);
        sociograph.addEdge("B", "E", 2, 6);
        sociograph.addEdge("B", "C", 4, 5);
        sociograph.addEdge("H", "D", 7, 10);
        sociograph.addEdge("D", "J", 7, 7);
        sociograph.addEdge("J", "I", 5, 6);

        System.out.println(sociograph);
        System.out.println();
        ArrayList<Student> arrayList = sociograph.getAllVertices();
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(arrayList.get(i));
            System.out.println();
        }
    }
}
