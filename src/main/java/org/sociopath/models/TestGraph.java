package org.sociopath.models;

import org.sociopath.dao.GraphDao;
import org.sociopath.events.SocialActivities;
import org.sociopath.utils.DBConnect;

public class TestGraph {
    public static void main(String[] args) {
        Sociograph sociograph = new Sociograph();

        DBConnect.startCon();
        initialization(sociograph);
//        event1Test(sociograph);
//        event2Test(sociograph);
//        event3Test(sociograph);
//        event4Test(sociograph);
//        event5Test(sociograph);
//        event6Test(sociograph);
        GraphDao.saveGraph(sociograph);
        DBConnect.closeCon();
    }

    public static void initialization(Sociograph sociograph) {

//        DBConnect.startCon();
//        GraphDao.deleteGraph();     // reset

//                            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}
        String[] nodesToAdd = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

        for (String node: nodesToAdd) {
            sociograph.addVertex(node);
        }

        sociograph.addUndirectedEdge("A", "G", 3, 4, Relationship.ENEMY);
        sociograph.addUndirectedEdge("A", "B", 8, 5, Relationship.FRIEND);
        sociograph.addUndirectedEdge("B", "F", 7, 9, Relationship.FRIEND);
        sociograph.addUndirectedEdge("B", "E", 2, 6, Relationship.FRIEND);
        sociograph.addUndirectedEdge("B", "C", 4, 5, Relationship.FRIEND);
        sociograph.addUndirectedEdge("H", "D", 7, 10, Relationship.FRIEND);
        sociograph.addUndirectedEdge("D", "J", 7, 7, Relationship.FRIEND);
        sociograph.addUndirectedEdge("I", "J", 6, 5, Relationship.FRIEND);
        sociograph.addUndirectedEdge("A", "C", 3,10, Relationship.THE_OTHER_HALF);

        // Graph Tests
        /*
        System.out.println(sociograph);
        System.out.println();
        ArrayList<Student> arrayList = sociograph.getAllStudents();
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(arrayList.get(i));
            System.out.println();
        };
         */

    }

    public static void event1Test(Sociograph sociograph) {
        // Event 1 Tests
        SocialActivities.event1(sociograph);
        // test whether if they are friends then can the event run??
//        event1("A", "B", sociograph);

        // test if whether if they one of them know the other but the other do not know him
        // whether the event can run
        // B -> D
        // so the event cannot run
//        sociograph.addDirectedEdge("B", "D", 3);
//        event1("B" , "D", sociograph);
    }

    // Working well
    public static void event2Test(Sociograph sociograph) {
        // Event 2 Tests
        // Test simple case, G make new friend with H (H connects to a path, one direction propagate)
//        sociograph.addDirectedEdge("G", "H", 10);
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 1");
//        SocialActivities.event2(sociograph, "G", "H");
//        System.out.println(sociograph + "\n");

        // Test simple case, G make new friend with D (D connects to H and J, two direction propagate)
//        sociograph.addDirectedEdge("G", "D", 10);
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 2");
//        SocialActivities.event2(sociograph, "G", "D");

        // Test G connects H when H, D, J, I is a cycle (make sure won't go into infinity loop)
//        sociograph.addUndirectedEdge("I", "H", 1, 1); // connect it to be a cycle
//        sociograph.addDirectedEdge("G", "H", 10);            // G makes friend with H (G has 10 rep relative to H)
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 3");
//        SocialActivities.event2(sociograph, "G", "H");           // H tells his friends about G

        // Test G connects C in the same group (make sure rep point relative to already known friend A change)
//        sociograph.addDirectedEdge("G", "C", 10);
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 4");
//        SocialActivities.event2(sociograph, "G", "C");
    }

    // Working well
    public static void event3Test(Sociograph sociograph) {
        System.out.println(sociograph + "\n");
        SocialActivities.event3(sociograph, "A");
        System.out.println("\n" + sociograph);
    }

    // TODO: Jia Hong path
    public static void event4Test(Sociograph sociograph) {
        System.out.println("Event 4 - Arranging Books");
        System.out.println(sociograph + "\n");
        SocialActivities.event4();
        System.out.println("\n" + sociograph);
    }

    // Working well
    public static void event5Test(Sociograph sociograph) {
        sociograph.addUndirectedEdge("G", "E", 1, 1, Relationship.FRIEND);
        sociograph.addUndirectedEdge("E", "C", 1, 1, Relationship.FRIEND);
        SocialActivities.event5(sociograph, "F", "C");
    }

    // TODO: Jia Hong path
    public static void event6Test(Sociograph sociograph) {
        System.out.println("Event 6 - Friendship");
        System.out.println(sociograph + "\n");
        SocialActivities.event6();
        System.out.println("\n" + sociograph);
    }

    public static void sixDegreesOfKenThompson(Sociograph sociograph){
        SocialActivities.sixDegreeOfKenThompson(sociograph);
    }

    public static void doAssignmentsTest(Sociograph sociograph){
        SocialActivities.doAssignments(sociograph);
    }
}
