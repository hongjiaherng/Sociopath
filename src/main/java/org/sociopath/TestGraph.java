package org.sociopath;

public class TestGraph {
    public static void main(String[] args) {
        Sociograph sociograph = new Sociograph();

        initialization(sociograph);
//        event1Test(sociograph);
//        event2Test(sociograph);
        event3Test(sociograph);
//        event5Test(sociograph);
    }

    public static void initialization(Sociograph sociograph) {
//                            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}
        String[] nodesToAdd = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

        for (String node: nodesToAdd) {
            sociograph.addVertex(node);
        }

        sociograph.addUndirectedEdge("A", "G", 3, 4);
        sociograph.addUndirectedEdge("A", "B", 8, 5, Relationship.FRIEND);
        sociograph.addUndirectedEdge("B", "F", 7, 9, Relationship.FRIEND);
        sociograph.addUndirectedEdge("B", "E", 2, 6, Relationship.FRIEND);
        sociograph.addUndirectedEdge("B", "C", 4, 5, Relationship.FRIEND);
        sociograph.addUndirectedEdge("H", "D", 7, 10, Relationship.FRIEND);
        sociograph.addUndirectedEdge("D", "J", 7, 7, Relationship.FRIEND);
        sociograph.addUndirectedEdge("J", "I", 5, 6, Relationship.FRIEND);
        sociograph.setRelationship("G", "A" , Relationship.FRIEND);


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
//        event1("B" , "D", sociograph);

        // test whether if they are friends then can the event run??
//        event1("A", "B", sociograph);

        // test if whether if they one of them know the other but the other do not know him
        // whether the event can run
        // B -> D
        // so the event cannot run
//        sociograph.addDirectedEdge("B", "D", 3);
//        event1("B" , "D", sociograph);
    }

    public static void event2Test(Sociograph sociograph) {
        // Event 2 Tests
        // Test simple case, G make new friend with H (H connects to a path, one direction propagate)
//        sociograph.addDirectedEdge("G", "H", 10);
//        System.out.println("Original Graph");
//        System.out.println(sociograph);
//        System.out.println();
//        System.out.println("Mutated Graph 1");
//        SocialActivities.event2(sociograph, "G", "H");

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

    public static void event3Test(Sociograph sociograph) {
        // Event 3 Tests

        // TODO: Start n End are the same

        // Normal condition
//        SocialActivities.event3(sociograph, "A");

        // When first 3 having same lunchStart
//        sociograph.getStudent("A").setLunchStart(10, 45);
//        sociograph.getStudent("A").setLunchPeriod(60);
//        sociograph.getStudent("D").setLunchStart(10, 40);
//        sociograph.getStudent("D").setLunchPeriod(10);
//        sociograph.getStudent("G").setLunchStart(10, 40);
//        sociograph.getStudent("G").setLunchPeriod(20);
//        sociograph.getStudent("C").setLunchStart(10, 40);
//        sociograph.getStudent("C").setLunchPeriod(30);
//        sociograph.getStudent("I").setLunchStart(11, 5);
//        sociograph.getStudent("I").setLunchPeriod(20);
//        sociograph.getStudent("F").setLunchStart(11, 20);
//        sociograph.getStudent("F").setLunchPeriod(30);
//        SocialActivities.event3(sociograph,"A");

        // When middle 3 having same lunchStart
//        sociograph.getStudent("A").setLunchStart(10, 45);
//        sociograph.getStudent("A").setLunchPeriod(60);
//        sociograph.getStudent("D").setLunchStart(10, 40);
//        sociograph.getStudent("D").setLunchPeriod(20);
//        sociograph.getStudent("G").setLunchStart(10, 50);
//        sociograph.getStudent("G").setLunchPeriod(10);
//        sociograph.getStudent("C").setLunchStart(10, 50);
//        sociograph.getStudent("C").setLunchPeriod(20);
//        sociograph.getStudent("I").setLunchStart(10, 50);
//        sociograph.getStudent("I").setLunchPeriod(30);
//        sociograph.getStudent("F").setLunchStart(11, 20);
//        sociograph.getStudent("F").setLunchPeriod(30);
//        SocialActivities.event3(sociograph,"A");

        // When last 3 having same lunchStart
//        sociograph.getStudent("A").setLunchStart(10, 45);
//        sociograph.getStudent("A").setLunchPeriod(60);
//        sociograph.getStudent("D").setLunchStart(10, 40);
//        sociograph.getStudent("D").setLunchPeriod(15);
//        sociograph.getStudent("G").setLunchStart(10, 55);
//        sociograph.getStudent("G").setLunchPeriod(10);
//        sociograph.getStudent("C").setLunchStart(11, 0);
//        sociograph.getStudent("C").setLunchPeriod(10);
//        sociograph.getStudent("I").setLunchStart(11, 0);
//        sociograph.getStudent("I").setLunchPeriod(20);
//        sociograph.getStudent("F").setLunchStart(11, 0);
//        sociograph.getStudent("F").setLunchPeriod(30);

//        Student studentA = sociograph.getStudent("A");
//        System.out.println("Original rep points of A relative to others");
//        System.out.println(studentA.getRepPoints() + "\n");
//        SocialActivities.event3(sociograph,"A");
//        System.out.println(studentA.getRepPoints());

        // When 2 have exactly the same lunchStart and lunchEnd
        sociograph.getStudent("A").setLunchStart(10, 45);
        sociograph.getStudent("A").setLunchPeriod(60);
        sociograph.getStudent("D").setLunchStart(10, 50);
        sociograph.getStudent("D").setLunchPeriod(30);
        sociograph.getStudent("G").setLunchStart(10, 50);
        sociograph.getStudent("G").setLunchPeriod(30);
        sociograph.getStudent("C").setLunchStart(11, 0);
        sociograph.getStudent("C").setLunchPeriod(10);
        sociograph.getStudent("I").setLunchStart(11, 0);
        sociograph.getStudent("I").setLunchPeriod(20);
        sociograph.getStudent("F").setLunchStart(11, 0);
        sociograph.getStudent("F").setLunchPeriod(30);

        Student studentA = sociograph.getStudent("A");
        System.out.println("Original rep points of A relative to others");
        System.out.println(studentA.getRepPoints() + "\n");
        SocialActivities.event3(sociograph,"A");
        System.out.println(studentA.getRepPoints());

        // Use this to test event 3
//        Student studentA = sociograph.getStudent("A");
//        System.out.println("Original rep points of A relative to others");
//        System.out.println(studentA.getRepPoints() + "\n");
//        SocialActivities.event3(sociograph,"A");
//        System.out.println(studentA.getRepPoints());
    }

    public static void event4Test(Sociograph sociograph) {

    }

    public static void event5Test(Sociograph sociograph) {
        sociograph.addUndirectedEdge("G", "E", 1, 1, Relationship.FRIEND);
        sociograph.addUndirectedEdge("E", "C", 1, 1, Relationship.FRIEND);
        SocialActivities.event5(sociograph, "F", "C");
    }

    public static void event6Test(Sociograph sociograph) {

    }
}
