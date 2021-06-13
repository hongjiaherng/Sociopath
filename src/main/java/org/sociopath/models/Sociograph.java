package org.sociopath.models;

import org.sociopath.dao.GraphDao;

import java.util.*;

/**
 * Graph object to simulate friendship
 */
public class Sociograph {

    private List<Vertex> vertices;
    private int size;    // Total vertices in the graph

    public Sociograph() {
        this.vertices = new ArrayList<>();
        this.size = 0;
    }

    /**
     * Get the total number of vertices in the graph
     * @return total vertices in the graph
     */
    public int getSize() {
        return size;
    }

    /**
     * Check if the graph contains the vertex with Student object named "name"
     * @param name student's name
     * @return true if name exist in any of the vertices, otherwise false
     */
    public boolean hasVertex(String name) {
        for(Vertex v : vertices){
            if(v.studentInfo.getName().equals(name))
                return true;
        }
        return false;
    }

    /**
     * Check if there's an edge from vertex srcName to vertex adjName (direction-wise). Also known as check
     * if vertex srcName has some rep points relative to vertex adjName
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @return true if from vertex srcName to vertex adjName contains an edge (direction-wise), otherwise false
     */
    public boolean hasDirectedEdge(String srcName, String adjName) {
        if(hasVertex(srcName) && hasVertex(adjName)){
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Edge currentEdge = srcVertex.firstEdge;

            while(currentEdge != null){
                if(currentEdge.adjVertex.studentInfo.getName().equals(adjName))
                    return true;

                currentEdge = currentEdge.nextEdge;
            }
        }
        return false;
    }

    /**
     * Check if there's an edge both from vertex srcName to vertex adjName and from vertex adjName to vertex srcName.
     * Also known as check if vertex srcName has some rep points relative to vertex adjName and the same applies to another direction.
     * In short, this method checks if both direction contain edge.
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @return true if both from vertex srcName to vertex adjName and from vertex adjName to vertex srcName contains an
     * edge (both direction contain edges), otherwise false
     */
    public boolean hasUndirectedEdge(String srcName, String adjName) {
        return hasDirectedEdge(srcName, adjName) && hasDirectedEdge(adjName, srcName);
    }

    /**
     * Create a Student object with "name" and add a new vertex using the newly created Student object. The new vertex is
     * being add at the end of the linked list of the graph.
     * @param name student's name
     * @return true if the vertex is successfully added, otherwise false
     */
    public boolean addVertex(String name) {
        if (!hasVertex(name)) {
            Vertex newVertex = new Vertex(new Student(name));
            this.vertices.add(newVertex);
            size++;
            return true;
        }
        return false;
    }

    public boolean addVertexFromDB(Student student){
        if(!hasVertex(student.getName())) {
            Vertex newVertex = new Vertex(student);
            this.vertices.add(newVertex);
            size++;
            return true;
        }

        return false;
    }

    public boolean changeVertexName(String oldName, String newName) {
        Student student = getStudent(oldName);
        if (!hasVertex(newName)) {
            student.setName(newName);
            return true;
        }
        return false;
    }

    public boolean deleteVertex(String name) {
        if (hasVertex(name)) {
            int vertexIndex = indexOf(name);
            next:
                for (Vertex v : vertices) {
                    if (v.studentInfo.getName().equals(name)) {
                        continue;
                    }

                    Edge currentEdge = v.firstEdge;
                    while (currentEdge != null) {
                        if (currentEdge.adjVertex.studentInfo.getName().equals(name)) {
                            removeEdge(v.studentInfo.getName(), name);
                            continue next;
                        }
                        currentEdge = currentEdge.nextEdge;
                    }
                }
            this.vertices.remove(vertexIndex);
            size--;
            return true;
        }
        return false;
    }

    public boolean removeEdge(String srcName, String adjName) {
        if (hasDirectedEdge(srcName, adjName)) {
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Edge currentEdge = srcVertex.firstEdge;
            Edge prevEdge = srcVertex.firstEdge;
            Relationship rel;

            while (currentEdge != null) {
                if (currentEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                    Student personToRemove = currentEdge.adjVertex.studentInfo;
                    rel = checkRelationship(srcName, adjName);
                    if (srcVertex.firstEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                        srcVertex.firstEdge = currentEdge.nextEdge;
                    } else {
                        prevEdge.nextEdge = currentEdge.nextEdge;
                    }
                    // remove adjStudent from HashMap
                    // remove rep point of adjStudent
                    srcVertex.studentInfo.deleteRelationship(personToRemove, rel);
                    return true;
                }
                prevEdge = currentEdge;
                currentEdge = currentEdge.nextEdge;
            }
        }
        return false;
    }

    public void clear() {
        this.vertices.clear();
        this.size = 0;
    }

    /**
     * Add the undirected edges from srcName and adjName but with different weight. Can also be rewritten as
     * <br> - addDirectedEdge(srcName, adjName, srcRep)
     * <br> - addDirectedEdge(adjName, srcName, adjRep)
     * <br> Also note that self loop is not allowed in this graph. The relationship on both the edges are determined by relationship.
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param srcRep rep point of vertex srcName relative to vertex adjName / weight of edge from vertex srcName to vertex adjName
     * @param adjRep rep point of vertex adjName relative to vertex srcName / weight of edge from vertex adjName to vertex srcName
     * @param rel relationship to set to both of the edges
     * @return true if both the edges is successfully added, otherwise false
     */
    public boolean addUndirectedEdge(String srcName, String adjName, double srcRep, double adjRep, Relationship rel) {      // possible rel : FRIEND, ENEMY, THE_OTHER_HALF, NONE

        if (size == 0) {
            return false;
        } else if (srcName.equals(adjName)) {
            System.out.println("Self loop is not allowed");
            return false;
        } else if (!hasVertex(srcName) || !hasVertex(adjName)) {
            return false;
        }

        if (rel == Relationship.ADMIRED_BY) {
            System.out.println("Admirer can't be undirected relationship!");
            throw new IllegalArgumentException("Admirer can't be undirected relationship!");
        }

        Vertex srcVertex = vertices.get(indexOf(srcName));
        Vertex adjVertex = vertices.get(indexOf(adjName));
        Edge newSrcEdge = new Edge(adjVertex, srcRep, rel, srcVertex.firstEdge);
        srcVertex.firstEdge = newSrcEdge;
        srcVertex.studentInfo.setRepPoints(adjName, srcRep);

        Edge newAdjEdge = new Edge(srcVertex, adjRep, rel, adjVertex.firstEdge);
        adjVertex.firstEdge = newAdjEdge;
        adjVertex.studentInfo.setRepPoints(srcName, adjRep);

        srcVertex.studentInfo.setRelationship(adjVertex.studentInfo, null, rel);
        adjVertex.studentInfo.setRelationship(srcVertex.studentInfo, null, rel);

        return true;
    }

    /**
     * Add the directed edges from srcName and adjName with weight "srcRep". Also note that self
     * loop is not allowed in this graph. The relationship on the edge is null.
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param srcRep rep point of vertex srcName relative to vertex adjName / weight of edge from vertex srcName to vertex adjName
     * @return true if the edge is successfully added, otherwise false
     */
    public boolean addDirectedEdge(String srcName, String adjName, double srcRep, Relationship rel) {     // possible rel : NONE or ADMIRED_BY
        if(srcName.equals(adjName))
            return false;

        if (rel != Relationship.NONE && rel != Relationship.ADMIRED_BY) {
            System.out.println("Only NONE & ADMIRED_BY is allowed in directed edge");
            throw new IllegalArgumentException("Only NONE & ADMIRED_BY is allowed in directed edge");
        }

        if(hasVertex(srcName) && hasVertex(adjName)){
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Vertex adjVertex = vertices.get(indexOf(adjName));

            Edge newSrcEdge = new Edge(adjVertex, srcRep, rel, srcVertex.firstEdge);
            srcVertex.firstEdge = newSrcEdge;
            srcVertex.studentInfo.setRepPoints(adjName, srcRep);
            srcVertex.studentInfo.setRelationship(adjVertex.studentInfo, null, rel);

            return true;
        }

        return false;
    }

    /**
     * Check the relationship from a vertex srcName to a vertex adjName.(Directed) Return the relationship if there exist one.
     * Otherwise, return null
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @return relationship type in enum (Either FRIEND, ENEMY, THE_OTHER_HALF, ADMIRED_BY, or NONE), null if no relationship
     */
    public Relationship checkRelationship(String srcName, String adjName) {
//        System.out.println(srcName + " " + adjName + " has directed edge ? " + hasDirectedEdge(srcName, adjName));
        if(hasDirectedEdge(srcName, adjName)){
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Edge srcEdge = srcVertex.firstEdge;

            while(srcEdge != null){
                if(srcEdge.adjVertex.studentInfo.getName().equals(adjName))
                    return srcEdge.relationship;
                srcEdge = srcEdge.nextEdge;
            }
        }
        return null;
        // TODO : MIGHT have problem (if return null will throw exception in Student.setRelationship())
    }

    public boolean isAdmiredBy(String srcName, String adjName) {
        if (!hasUndirectedEdge(srcName, adjName)) {
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Edge srcEdge = srcVertex.firstEdge;

            while(srcEdge != null){
                if(srcEdge.adjVertex.studentInfo.getName().equals(adjName))
                    return srcEdge.relationship == Relationship.ADMIRED_BY;
                srcEdge = srcEdge.nextEdge;
            }
        }
        return false;
    }

    /**
     * Set a new relationship from the source Student to the adjacent Student
     * The relationship can be only directed only (CRUSH or NONE)
     *
     * @param srcName the name of the Student as the source of the relationship
     * @param adjName the name of the Student as the destination of the relationship
     * @param relationship the relationship that is directed (CRUSH or NONE)
     * @return the relationship is successful or not
     */
    public boolean setDirectedRelationshipOnEdge(String srcName, String adjName, Relationship relationship){
        if (!hasUndirectedEdge(srcName, adjName)) {
            if (relationship == Relationship.ADMIRED_BY || relationship == Relationship.NONE) {

                int srcIndex = indexOf(srcName);
                int adjIndex = indexOf(adjName);
                Vertex srcVertex = vertices.get(srcIndex);
                Vertex adjVertex = vertices.get(adjIndex);
                Edge currentEdge = srcVertex.firstEdge;
                Student srcStudent = srcVertex.studentInfo;
                Student adjStudent = adjVertex.studentInfo;

                while (currentEdge != null) {
                    if (currentEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                        Relationship oriRelationship = currentEdge.relationship;
                        currentEdge.relationship = relationship;

                        srcStudent.setRelationship(adjStudent, oriRelationship, relationship);

                        return true;
                    }

                    currentEdge = currentEdge.nextEdge;
                }
            }
        }

        return false;
    }


    /**
     * Set the undirected relationship to the two vertices srcName and adjName. A relationship can only be set
     * when there are both edges exist between them (meaning rep points are present relative to each other).
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param relationship relationship to set to both of the edges
     * @return true if the relationship is successfully set, otherwise false
     */
    public boolean setUndirectedRelationshipOnEdge(String srcName, String adjName, Relationship relationship) {
        if (hasUndirectedEdge(srcName, adjName)) {
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Vertex adjVertex = vertices.get(indexOf(adjName));
            Edge srcEdge = srcVertex.firstEdge;
            Edge adjEdge = adjVertex.firstEdge;
            Relationship prevRel = checkRelationship(srcName, adjName);

            while (srcEdge != null) {
                if (srcEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                    srcEdge.relationship = relationship;
                    break;
                }
                srcEdge = srcEdge.nextEdge;
            }

            while (adjEdge != null) {
                if (adjEdge.adjVertex.studentInfo.getName().equals(srcName)) {
                    adjEdge.relationship = relationship;
                    break;
                }
                adjEdge = adjEdge.nextEdge;
            }

            srcVertex.studentInfo.setRelationship(adjVertex.studentInfo, prevRel, relationship);
            adjVertex.studentInfo.setRelationship(srcVertex.studentInfo, prevRel, relationship);

            return true;
        } else {
            System.out.println("Relationship can't be set. They must both know each other to have a relationship (having rep point relative to each other)");
            return false;
        }
    }

    /**
     * Get the weight of edge from vertex srcName to vertex adjName. Also known as get the rep point
     * of vertex srcName relative to vertex adjName. To understand this better, think of this, in the
     * opinion of vertex adjName, he thinks that the reputation point of vertex srcName is the returned value
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @return the weight of edge from vertex srcName to vertex adjName / rep point of vertex srcName relative to vertex adjName
     * <br> throw NoSuchElementException if there's no edge from vertex srcName to vertex adjName
     */
    public double getSrcRepRelativeToAdj(String srcName, String adjName) {
        if (hasDirectedEdge(srcName, adjName)) {
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Edge srcEdge = srcVertex.firstEdge;
            while (srcEdge != null) {
                if (srcEdge.adjVertex.studentInfo.getName().equals(adjName))
                    return srcEdge.repRelativeToAdj;
                srcEdge = srcEdge.nextEdge;
            }
        }
        throw new NoSuchElementException("No edge between srcName & adjName or they don't exist");
    }

    /**
     * Set the weight of edge from vertex srcName to vertex adjName. Also known as setting the rep point
     * of vertex srcName relative to vertex adjName. To understand this better, think of this, in the
     * opinion of vertex adjName, he thinks that the reputation point of vertex srcName is the returned value
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param newRep weight of edge from vertex srcName to vertex adjName / rep point of vertex srcName relative to vertex adjName
     */
    public void setSrcRepRelativeToAdj(String srcName, String adjName, double newRep) {
        if (hasDirectedEdge(srcName, adjName)) {
            Vertex srcVertex = vertices.get(indexOf(srcName));
            Edge srcEdge = srcVertex.firstEdge;
            while (srcEdge != null) {
                if (srcEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                    srcEdge.repRelativeToAdj = newRep;
                    srcVertex.studentInfo.setRepPoints(adjName, newRep);

                    return;
                }
                srcEdge = srcEdge.nextEdge;
            }
        }
        throw new NoSuchElementException("No edge between srcName & adjName or they don't exist");
    }

    /**
     * Get the Student object of vertex named "name" (contains overloaded version of this method)
     * @param name student's name
     * @return Student object of vertex named "name"
     * <br> null if "name" is not exist in any of the vertex of the graph
     */
    public Student getStudent(String name) {
        if (hasVertex(name)) {
            Vertex vertex = vertices.get(indexOf(name));
            return vertex.studentInfo;
        }
        return null;
    }

    /**
     * Get all the Student objects in the graph
     * @return all the Student objects in the graph in the form of ArrayList
     */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        for (Vertex v : vertices) {
            list.add(v.studentInfo);
        }
        return list;
    }

    /**
     * Get the neighbours of vertex "name"
     * @param name student's name
     * @return Student object of vertices which is directly connected to vertex "name"
     */
    public List<Student> neighbours(String name) {
        if (hasVertex(name)) {
            List<Student> list = new ArrayList<>();
            Vertex srcVertex = vertices.get(indexOf(name));
            Edge srcEdge = srcVertex.firstEdge;
            while (srcEdge != null) {
                list.add(srcEdge.adjVertex.studentInfo);
                srcEdge = srcEdge.nextEdge;
            }
            return list;
        }
        throw new NoSuchElementException("Vertex " + name + " is not exist");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            sb.append(vertices.get(i).studentInfo.getName()).append("\t=> [");
            Edge currentEdge = vertices.get(i).firstEdge;
            while (currentEdge != null) {
                sb.append("(").append(currentEdge.adjVertex.studentInfo.getName()).append(" | ");
                sb.append("rep:").append(currentEdge.repRelativeToAdj).append(" | ");
                sb.append("rel:").append(currentEdge.relationship).append(")");
                if (currentEdge.nextEdge != null) {
                    sb.append(", ");
                }
                currentEdge = currentEdge.nextEdge;
            }
            sb.append("]");
            if (i != vertices.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Find all the path from source to destination (Edge of the 2 vertices that are not FRIEND will not be passed through)
     * @param source source vertex
     * @param destination destination vertex
     * @return a list of path from source to destination
     */
    public List<List<String>> dfTraversal(String source, String destination) {
        List<List<String>> listOfPathList = new LinkedList<>();
        Map<String, Boolean> isVisited = new HashMap<>();
        for (Vertex v : vertices) {
            isVisited.put(v.studentInfo.getName(), false);
        }
        LinkedList<String> pathList = new LinkedList<>();

        pathList.add(source);

        dfTraversalRecur(source, destination, isVisited, pathList, listOfPathList);
        return listOfPathList;
    }

    private void dfTraversalRecur(String current, String destination, Map<String, Boolean> isVisited, List<String> localPathList, List<List<String>> listOfPathList) {
        if (current.equals(destination)) {
            List<String> copy = new LinkedList<>(localPathList);
            listOfPathList.add(copy);
            return;
        }

        isVisited.put(current, true);

        for (Student neighborObj : neighbours(current)) {
            String neighbor = neighborObj.getName();
            if (!isVisited.get(neighbor) &&
                    (checkRelationship(neighbor, current) == Relationship.FRIEND ||
                            checkRelationship(neighbor, current) == Relationship.THE_OTHER_HALF)) {
                localPathList.add(neighbor);

                dfTraversalRecur(neighbor, destination, isVisited, localPathList, listOfPathList);

                localPathList.remove(neighbor);
            }
        }
        isVisited.put(current, false);
    }

    public List<List<String>> bfs(String source, String dest){
        // To store all the path
        Queue<List<String>> queue = new LinkedList<>();
        List<List<String>> allPath = new ArrayList<>();

        // Current path
        List<String> path = new ArrayList<>();
        path.add(source);
        queue.offer(path);

        while(!queue.isEmpty()){
            path = queue.remove();
            String s = path.get(path.size() - 1);

            if(s.equals(dest)){
                allPath.add(path);
            }

            List<Student> neighbours = neighbours(s);
            for(Student student : neighbours){
                String name = student.getName();

                if(isNotVisited(name, path)){
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(name);
                    queue.offer(newPath);
                }
            }
        }

        return allPath;

    }

    private static boolean isNotVisited(String s, List<String> path){
        for(String str : path){
            if(str.equals(s))
                return false;
        }

        return true;
    }


    private int indexOf(String name) {
        int index = -1;

        for (int i = 0; i < vertices.size(); i++) {
            Vertex srcVertex = vertices.get(i);

            if (srcVertex.studentInfo.getName().equals(name)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Static class for vertex of the graph
     */
    public static class Vertex {
        private Student studentInfo;
        private Edge firstEdge;

        public Vertex(Student studentInfo) {
            this.studentInfo = studentInfo;
            this.firstEdge = null;
        }
    }

    /**
     * Static class for edge of the graph
     */
    public static class Edge {
        private Vertex adjVertex;
        private Edge nextEdge;

        // Also the weight of the edge
        private double repRelativeToAdj; // src's rep point in the opinion of adjVertex
        private Relationship relationship;

        public Edge(Vertex adjVertex, double repRelativeToAdj, Edge nextEdge) {
            this(adjVertex, repRelativeToAdj, Relationship.NONE, nextEdge);
        }

        public Edge(Vertex adjVertex, double repRelativeToAdj, Relationship relationship, Edge nextEdge) {
            this.adjVertex = adjVertex;
            this.repRelativeToAdj = repRelativeToAdj;
            this.relationship = relationship;
            this.nextEdge = nextEdge;
        }
    }
}