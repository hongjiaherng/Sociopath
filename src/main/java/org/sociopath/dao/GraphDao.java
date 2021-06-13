package org.sociopath.dao;

import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.session.Session;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;
import org.sociopath.utils.DBConnect;


import java.util.*;

public class GraphDao {
    /**
     * Connection for Neo4j database
     */
    private static final Session session = DBConnect.getSession();
    private static ArrayList<Student> allStudents;

    /**
     * Save the graph into the database
     * @param graph A Sociograph object that is going to be saved
     */
    public static void saveGraph(Sociograph graph) throws ClientException, ConnectionException{
        List<Student> vertices = graph.getAllStudents();
        for(Student student : vertices)
            session.save(student);

    }

    /**
     * Delete all nodes and relationship in the database
     */
    public static void deleteGraph() throws ConnectionException, ClientException{
        session.deleteAll(Student.class);
    }

    /**
     * Get all the nodes from the database
     * @return A list of Student object
     */
    public static ArrayList<Student> getAllVertices() throws ConnectionException, ClientException {
        ArrayList<Student> students ;

        Collection<Student> collection = session.loadAll(Student.class);
        students = new ArrayList<>(collection);

        return students;
    }

    /**
     * Delete a certain node in the database that has the provided name
     *
     * @param name name of the node
     */
    public static void deleteNode(String name) throws ConnectionException{
        Filter filter = new Filter("Name", ComparisonOperator.EQUALS, name);
        Collection<Filter> filters = new ArrayList<>();
        filters.add(filter);

        session.delete(Student.class, filters, true);
    }

    /**
     * Save or update the node in the database
     *
     * @param node the Student object that wanted to be updated
     */
    public static void db_addOrUpdateNode(Student node) {
        session.save(node);
    }

    /**
     * Get the graph from the database and make the relationship in the Sociograph
     *
     * @return a Socioraph object that contains all the relationship
     */
    public static Sociograph db_getGraph() throws ConnectionException, ClientException {
        allStudents = GraphDao.getAllVertices();
        String[] names = new String[allStudents.size()];
        ArrayList<HashMap<String, Double>> allRepPoints = new ArrayList<>();
        allStudents.forEach(e -> allRepPoints.add(e.getRepPoints()));
        Sociograph sociograph = new Sociograph();

        int i = 0;
        for(Student student : allStudents) {
            sociograph.addVertexFromDB(student);
            names[i++] = student.getName();
        }

        for(Student student : allStudents){
            HashMap<String, Double> repPoints = allRepPoints.get(findIndex(names, student.getName()));
            db_MakeRelationToSociograph(student, names,  Relationship.FRIEND, repPoints, sociograph);
            db_MakeRelationToSociograph(student, names,  Relationship.ENEMY, repPoints, sociograph);
            db_MakeRelationToSociograph(student, names,  Relationship.NONE, repPoints, sociograph);
            db_MakeRelationToSociograph(student, names,  Relationship.ADMIRED_BY, repPoints, sociograph);
            db_MakeRelationToSociograph(student, names,  Relationship.THE_OTHER_HALF, repPoints, sociograph);

        }

        return sociograph;
    }

    private static String[] getRelationFromDB(Student node, Relationship rel){
        String friendsCyper = "Match (a:Student {Name:'" + node.getName() +"'})-[:" + rel +"]->(b:Student) return b.Name";
        Iterable<Map<String, Object>> friends = session.query(friendsCyper, Collections.EMPTY_MAP);
        ArrayList<Map<String, Object>> friendsList = new ArrayList<>();
        friends.forEach(friendsList::add);

        String[] friendArr = new String[friendsList.size()];
        int j = 0;

        for(Map<String, Object> k : friendsList){
            friendArr[j++] = (String) k.get("b.Name");
        }

        return friendArr;
    }

    private static int findIndex(String[] names, String name){
        for(int i = 0; i< names.length; i++)
            if(name.equals(names[i]))
                return i;

        return -1;
    }

    private static void db_MakeRelationToSociograph(Student student,String[] names, Relationship rel, HashMap<String, Double> repPoints, Sociograph sociograph){
        String[] studentRelation = getRelationFromDB(student, rel);
        for (String s : studentRelation) {
            int index = findIndex(names, s);
            Student fr = allStudents.get(index);

            HashMap<String, Double> rep = fr.getRepPoints();

            boolean relationshipCheck = rel == Relationship.FRIEND || rel == Relationship.ENEMY || rel == Relationship.THE_OTHER_HALF;
            if (!sociograph.hasUndirectedEdge(student.getName(), fr.getName()) && relationshipCheck)
                sociograph.addUndirectedEdge(student.getName(), fr.getName(), repPoints.get(fr.getName()), rep.get(student.getName()), rel);

            else if ( (rel == Relationship.NONE || rel == Relationship.ADMIRED_BY) && !sociograph.hasDirectedEdge(student.getName(), fr.getName()))
                sociograph.addDirectedEdge(student.getName(), fr.getName(), repPoints.get(fr.getName()), rel);


        }
    }
}
