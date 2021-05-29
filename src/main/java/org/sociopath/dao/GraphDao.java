package org.sociopath.dao;

import org.neo4j.ogm.session.Session;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;
import org.sociopath.utils.DBConnect;

import java.util.*;

public class GraphDao {
    /**
     * Connection for Neo4j database
     */
    private static final Session session = DBConnect.getSession();

    /**
     * Save the graph into the database
     * @param graph A Sociograph object that is going to be saved
     */
    public static void saveGraph(Sociograph graph){

        List<Student> vertices = graph.getAllStudents();
        for(Student student : vertices){
            session.save(student);
        }

    }

    /**
     * Delete all nodes and relationship in the database
     */
    public static void deleteGraph(){
        session.deleteAll(Student.class);
    }

    /**
     * Get all the nodes from the database
     * @return A list of Student object
     */
    public static ArrayList<Student> getAllVertices(){
        ArrayList<Student> students ;

        Collection<Student> collection = session.loadAll(Student.class);
        students = new ArrayList<>(collection);

        return students;
    }

//    public static Sociograph getGraph(){
//        Sociograph sociograph = new Sociograph();
//        HashMapConverter converter = new HashMapConverter();
//
//        ArrayList<Student> allStudents = new ArrayList<>(session.loadAll(Student.class));
//
//        System.out.println(allStudents);
//        for(Student student : allStudents)
//            sociograph.addVertexFromDB(student);
//
//        getRelationship(allStudents, sociograph);
//
//
//        return sociograph;
//    }


    public static void db_addOrUpdateNode(Student node) {
        session.save(node);
    }

    private static void getRelationship(ArrayList<Student> allStudents, Sociograph sociograph){
        for(Student s : allStudents){
            String cypher = "MATCH (n1:Student {Name:'" + s.getName() + "'})-[r:FRIEND]->(n2) RETURN n2.Name";
            Iterable<Map<String, Object>> collection = session.query(cypher, Collections.EMPTY_MAP);

            String repPointsCypher = "MATCH (n1:Student {Name:'" + s.getName() + "'}) RETURN n1.repPoints";
            Iterable<Map<String, Object>> repPoints = session.query(repPointsCypher, Collections.EMPTY_MAP);

            List<Map<String, Object>> friends = new ArrayList<>();
            collection.forEach(friends::add);

            List<Map<String, Object>> repPointList = new ArrayList<>();
            repPoints.forEach(repPointList::add);

            HashMap<String, Double> mapping = StringSplitter(repPointList);
            System.out.println(mapping);

            for(int i = 0; i<friends.size(); i++){
                Map<String, Object> friend = friends.get(i);
                String name = (String) friend.get("n2.Name");


                System.out.println(friend);

            }
        }
    }

    private static HashMap<String, Double> StringSplitter(List<Map<String, Object>> repPoints){
        HashMap<String, Double> rep = new HashMap<>();
        String[] temp = (String[]) repPoints.get(0).get("n1.repPoints");

        for(String map : temp){
            String[] splitted = map.split(":");
            rep.put(splitted[0], Double.parseDouble(splitted[1]));
        }
        return rep;
    }
}
