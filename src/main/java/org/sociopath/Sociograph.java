package org.sociopath;

import java.util.*;

/**
 * Graph object to simulate friendship
 */
public class Sociograph {

    private StudentVertex head;   // First vertex of the graph
    private int size;           // Total vertices in the graph
    private List<List<String>> listOfPathList;      // all possible path from one src to another dest using dfs

    public Sociograph() {
        this.head = null;
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
        if (head == null) {
            return false;
        }
        StudentVertex temp = head;
        while (temp != null) {
            if (temp.studentInfo.getName().equals(name)) {
                return true;
            }
            temp = temp.nextVertex;
        }
        return false;
    }

    /**
     * Get the total number of entering edge for the vertex named "name"
     * @param name student's name
     * @return total number of entering edge for the vertex with Student object named "name"
     * <br> -1 if vertex named "name" is not exist
     */
    public int getIndeg(String name) {
        if (hasVertex(name)) {
            StudentVertex temp = head;
            while (temp != null) {
                if (temp.studentInfo.getName().equals(name)) {
                    return temp.indeg;
                }
                temp = temp.nextVertex;
            }
        }
        return -1;
    }

    /**
     * Get the total number of exiting edge for the vertex named "name"
     * @param name student's name
     * @return total number of exiting edge for the vertex with Student object named "name"
     * <br> -1 if vertex named "name" is not exist
     */
    public int getOutdeg(String name) {
        if (hasVertex(name)) {
            StudentVertex temp = head;
            while (temp != null) {
                if (temp.studentInfo.getName().equals(name)) {
                    return temp.outdeg;
                }
                temp = temp.nextVertex;
            }
        }
        return -1;
    }

    /**
     * Check if there's an edge from vertex srcName to vertex adjName (direction-wise). Also known as check
     * if vertex srcName has some rep points relative to vertex adjName
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @return true if from vertex srcName to vertex adjName contains an edge (direction-wise), otherwise false
     */
    public boolean hasDirectedEdge(String srcName, String adjName) {
        if (head == null) {
            return false;
        } else if (!hasVertex(srcName) || !hasVertex(adjName)) {
            return false;
        }
        StudentVertex srcVertex = head;
        while (srcVertex != null) {
            if (srcVertex.studentInfo.getName().equals(srcName)) {
                RelationshipEdge currentEdge = srcVertex.firstEdge;
                while (currentEdge != null) {
                    if (currentEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                        return true;
                    }
                    currentEdge = currentEdge.nextEdge;
                }
                break;
            }
            srcVertex = srcVertex.nextVertex;
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
     * Get the index of vertex named "name" in the graph.
     * @param name student's name
     * @return index position of vertex named "name" in the graph
     * <br> -1 if vertex named "name" is not exist
     */
    public int getIndex(String name) {
        StudentVertex temp = head;
        int pos = 0;
        while (temp != null) {
            if (temp.studentInfo.getName().equals(name)) {
                return pos;
            }
            temp = temp.nextVertex;
            pos++;
        }
        return -1;
    }

    /**
     * Check the relationship between vertex srcName and vertex adjName. Return the relationship if there exist one.
     * Otherwise, return null
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @return relationship type in enum
     */
    public Relationship checkRelationship(String srcName, String adjName) {
        if (hasUndirectedEdge(srcName, adjName)) {
            StudentVertex srcVertex = head;
            while (srcVertex != null) {
                if (srcVertex.studentInfo.getName().equals(srcName)) {
                    RelationshipEdge srcEdge = srcVertex.firstEdge;
                    while (srcEdge != null) {
                        if (srcEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                            return srcEdge.relationship;
                        }
                        srcEdge = srcEdge.nextEdge;
                    }
                }
                srcVertex = srcVertex.nextVertex;
            }
        }
        return null;
    }

    /**
     * Set the relationship to the two vertices srcName and adjName. A relationship can only be set
     * when there are both edges exist between them (meaning rep points are present relative to each other).
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param relationship relationship to set to both of the edges
     * @return true if the relationship is successfully set, otherwise false
     */
    public boolean setRelationship(String srcName, String adjName, Relationship relationship) {
        // Vertex srcName and adjName must have 2 edges connected together
        // They must me connected directly to each other to be friend
        if (hasUndirectedEdge(srcName, adjName)) {
            StudentVertex srcVertex = head;
            RelationshipEdge srcEdge = srcVertex.firstEdge;
            StudentVertex adjVertex = head;
            RelationshipEdge adjEdge = adjVertex.firstEdge;
            getSrcVertexNEdge:
                while (srcVertex != null) {
                    if (srcVertex.studentInfo.getName().equals(srcName)) {
                        srcEdge = srcVertex.firstEdge;
                        while (srcEdge != null) {
                            if (srcEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                                break getSrcVertexNEdge;
                            }
                            srcEdge = srcEdge.nextEdge;
                        }
                    }
                    srcVertex = srcVertex.nextVertex;
                }

            getAdjVertexNEdge:
                while (adjVertex != null) {
                    if (adjVertex.studentInfo.getName().equals(adjName)) {
                        adjEdge = adjVertex.firstEdge;
                        while (adjEdge != null) {
                            if (adjEdge.adjVertex.studentInfo.getName().equals(srcName)) {
                                break getAdjVertexNEdge;
                            }
                            adjEdge = adjEdge.nextEdge;
                        }
                    }
                    adjVertex = adjVertex.nextVertex;
                }

            if (srcVertex != null && srcEdge != null && adjVertex != null && adjEdge != null) {
                srcEdge.relationship = relationship;
                adjEdge.relationship = relationship;
                srcVertex.studentInfo.getFriends().add(adjName);
                adjVertex.studentInfo.getFriends().add(srcName);
            } else {    // This might happen if the some of the method are wrongly implemented
                throw new NullPointerException(srcName + " & " + adjName + ", they don't have a proper undirected edge connected between them");
            }
            return true;
        } else {
            System.out.println("Relationship can't be set. They must both know each other to have a relationship (having rep point relative to each other)");
            return false;
        }
    }

    /**
     * Get the Student object of vertex at index "pos" (contains overloaded version of this method)
     * @param pos index position of a vertex in the graph
     * @return Student object of vertex at index "pos"
     * <br> null if "pos" is out of bound
     */
    public Student getStudent(int pos) {
        if (pos >= size || pos < 0) {
            return null;
        }
        StudentVertex temp = head;
        for (int i = 0; i < pos; i++) {
            temp = temp.nextVertex;
        }
        return temp.studentInfo;
    }

    /**
     * Get the Student object of vertex named "name" (contains overloaded version of this method)
     * @param name student's name
     * @return Student object of vertex named "name"
     * <br> null if "name" is not exist in any of the vertex of the graph
     */
    public Student getStudent(String name) {
        if (hasVertex(name)) {
            StudentVertex temp = head;
            while (temp != null) {
                if (temp.studentInfo.getName().equals(name)) {
                    return temp.studentInfo;
                }
                temp = temp.nextVertex;
            }
        }
        return null;
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
    public double getDirectedEdgeWeight(String srcName, String adjName) {
        if (head == null) {
            throw new NoSuchElementException("No friendship");
        } else if (!hasVertex(srcName) || !hasVertex(adjName)) {
            throw new NoSuchElementException("No friendship");
        }
        StudentVertex srcVertex = head;
        while (srcVertex != null) {
            if (srcVertex.studentInfo.getName().equals(srcName)) {
                RelationshipEdge currentEdge = srcVertex.firstEdge;
                while (currentEdge != null) {
                    if (currentEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                        return currentEdge.repRelativeToAdj;
                    }
                    currentEdge = currentEdge.nextEdge;
                }
                break;
            }
            srcVertex = srcVertex.nextVertex;
        }
        throw new NoSuchElementException(srcName + " don't know " + adjName);
    }

    /**
     * Set the weight of edge from vertex srcName to vertex adjName. Also known as setting the rep point
     * of vertex srcName relative to vertex adjName. To understand this better, think of this, in the
     * opinion of vertex adjName, he thinks that the reputation point of vertex srcName is the returned value
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param newWeight weight of edge from vertex srcName to vertex adjName / rep point of vertex srcName relative to vertex adjName
     */
    public void setDirectedEdgeWeight(String srcName, String adjName, double newWeight) {
        if (head == null) {
            throw new NoSuchElementException("No friendship");
        } else if (!hasVertex(srcName) || !hasVertex(adjName)) {
            throw new NoSuchElementException("No friendship");
        }
        StudentVertex srcVertex = head;
        while (srcVertex != null) {
            if (srcVertex.studentInfo.getName().equals(srcName)) {
                RelationshipEdge currentEdge = srcVertex.firstEdge;
                while (currentEdge != null) {
                    if (currentEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                        currentEdge.repRelativeToAdj = newWeight;
                        srcVertex.studentInfo.getRepPoints().put(adjName, newWeight);       // Bug fixed (Update rep point to student object)
                        return;
                    }
                    currentEdge = currentEdge.nextEdge;
                }
                break;
            }
            srcVertex = srcVertex.nextVertex;
        }
        throw new NoSuchElementException(adjName + " don't know " + srcName);
    }

    /**
     * Create a Student object with "name" and add a new vertex using the newly created Student object. The new vertex is
     * being add at the end of the linked list of the graph.
     * @param name student's name
     * @return true if the vertex is successfully added, otherwise false
     */
    public boolean addVertex(String name) {
        if (!hasVertex(name)) {
            StudentVertex temp = head;
            StudentVertex newVertex = new StudentVertex(new Student(name), null);

            if (head == null) {
                head = newVertex;
            } else {
                StudentVertex previous = head;
                while (temp != null) {
                    previous = temp;
                    temp = temp.nextVertex;
                }
                previous.nextVertex = newVertex;
            }
            size++;
            return true;
        }
        return false;
    }

    /**
     * Add the undirected edges from srcName and adjName but with different weight. Can also be rewritten as
     * <br> - addDirectedEdge(srcName, adjName, srcRep)
     * <br> - addDirectedEdge(adjName, srcName, adjRep)
     * <br> Also note that self loop is not allowed in this graph. The relationship on both the edges are null.
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param srcRep rep point of vertex srcName relative to vertex adjName / weight of edge from vertex srcName to vertex adjName
     * @param adjRep rep point of vertex adjName relative to vertex srcName / weight of edge from vertex adjName to vertex srcName
     * @return true if both the edges is successfully added, otherwise false
     */
    public boolean addUndirectedEdge(String srcName, String adjName, double srcRep, double adjRep) {
        if (head == null) {
            return false;
        } else if (!hasVertex(srcName) || !hasVertex(adjName)) {
            return false;
        } else if (srcName.equals(adjName)) {
            System.out.println("Self loop is not allowed");
            return false;
        }
        StudentVertex srcVertex = head;
        while (srcVertex != null) {
            if (srcVertex.studentInfo.getName().equals(srcName)) {
                StudentVertex destVertex = head;
                while (destVertex != null) {
                    if (destVertex.studentInfo.getName().equals(adjName)) {
                        RelationshipEdge newSrcEdge = new RelationshipEdge(destVertex, srcRep, srcVertex.firstEdge);
                        srcVertex.firstEdge = newSrcEdge;
                        srcVertex.studentInfo.getRepPoints().put(adjName, srcRep);
//                        srcVertex.studentInfo.getFriends().add(adjName);
                        srcVertex.indeg++;
                        srcVertex.outdeg++;

                        RelationshipEdge newDestEdge = new RelationshipEdge(srcVertex, adjRep, destVertex.firstEdge);
                        destVertex.firstEdge = newDestEdge;
                        destVertex.studentInfo.getRepPoints().put(srcName, adjRep);
//                        destVertex.studentInfo.getFriends().add(srcName);
                        destVertex.indeg++;
                        destVertex.outdeg++;

                        return true;
                    }
                    destVertex = destVertex.nextVertex;
                }
            }
            srcVertex = srcVertex.nextVertex;
        }
        return false;
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
     * @param relationship relationship to set to both of the edges
     * @return true if both the edges is successfully added, otherwise false
     */
    public boolean addUndirectedEdge(String srcName, String adjName, double srcRep, double adjRep, Relationship relationship) {
        if (head == null) {
            return false;
        } else if (!hasVertex(srcName) || !hasVertex(adjName)) {
            return false;
        } else if (srcName.equals(adjName)) {
            System.out.println("Self loop is not allowed");
            return false;
        }
        StudentVertex srcVertex = head;
        while (srcVertex != null) {
            if (srcVertex.studentInfo.getName().equals(srcName)) {
                StudentVertex destVertex = head;
                while (destVertex != null) {
                    if (destVertex.studentInfo.getName().equals(adjName)) {
                        RelationshipEdge newSrcEdge = new RelationshipEdge(destVertex, srcRep, relationship, srcVertex.firstEdge);
                        srcVertex.firstEdge = newSrcEdge;
                        srcVertex.studentInfo.getRepPoints().put(adjName, srcRep);
                        srcVertex.indeg++;
                        srcVertex.outdeg++;

                        RelationshipEdge newDestEdge = new RelationshipEdge(srcVertex, adjRep, relationship, destVertex.firstEdge);
                        destVertex.firstEdge = newDestEdge;
                        destVertex.studentInfo.getRepPoints().put(srcName, adjRep);
                        destVertex.indeg++;
                        destVertex.outdeg++;

                        if (relationship == Relationship.FRIEND) {
                            srcVertex.studentInfo.getFriends().add(adjName);
                            destVertex.studentInfo.getFriends().add(srcName);
                        }

                        return true;
                    }
                    destVertex = destVertex.nextVertex;
                }
            }
            srcVertex = srcVertex.nextVertex;
        }
        return false;
    }

    /**
     * Add the directed edges from srcName and adjName with weight "srcRep". Also note that self
     * loop is not allowed in this graph. The relationship on the edge is null.
     * @param srcName student's name as source vertex
     * @param adjName student's name as adjacent vertex
     * @param srcRep rep point of vertex srcName relative to vertex adjName / weight of edge from vertex srcName to vertex adjName
     * @return true if the edge is successfully added, otherwise false
     */
    public boolean addDirectedEdge(String srcName, String adjName, double srcRep) {
        if (head == null) {
            return false;
        } else if (!hasVertex(srcName) || !hasVertex(adjName)) {
            return false;
        } else if (srcName.equals(adjName)) {
            System.out.println("Self loop is not allowed");
            return false;
        }
        StudentVertex srcVertex = head;
        while (srcVertex != null) {
            if (srcVertex.studentInfo.getName().equals(srcName)) {
                StudentVertex destVertex = head;
                while (destVertex != null) {
                    if (destVertex.studentInfo.getName().equals(adjName)) {
                        RelationshipEdge newSrcEdge = new RelationshipEdge(destVertex, srcRep, srcVertex.firstEdge);
                        srcVertex.firstEdge = newSrcEdge;
                        srcVertex.studentInfo.getRepPoints().put(adjName, srcRep);
//                        srcVertex.studentInfo.getFriends().add(adjName);
                        srcVertex.outdeg++;
                        destVertex.indeg++;
                        return true;
                    }
                    destVertex = destVertex.nextVertex;
                }
            }
            srcVertex = srcVertex.nextVertex;
        }
        return false;
    }

    /**
     * Get all the Student objects in the graph
     * @return all the Student objects in the graph in the form of ArrayList
     */
    public ArrayList<Student> getAllStudents() {
        ArrayList<Student> list = new ArrayList<>();
        StudentVertex temp = head;
        while (temp != null) {
            list.add(temp.studentInfo);
            temp = temp.nextVertex;
        }
        return list;
    }

    /**
     * Get the neighbours of vertex "name"
     * @param name student's name
     * @return Student object of vertices which is directly connected to vertex "name"
     */
    public ArrayList<Student> neighbours(String name) {
        if (!hasVertex(name)) {
            throw new NoSuchElementException("Node name is not exist");
        }
        ArrayList<Student> list = new ArrayList<>();
        StudentVertex temp = head;
        while (temp != null) {
            if (temp.studentInfo.getName().equals(name)) {
                RelationshipEdge currentEdge = temp.firstEdge;
                while (currentEdge != null) {
                    list.add(currentEdge.adjVertex.studentInfo);
                    currentEdge = currentEdge.nextEdge;
                }
            }
            temp = temp.nextVertex;
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        StudentVertex temp = head;
        while (temp != null) {
            sb.append(temp.studentInfo.getName()).append("\t=> [");
            RelationshipEdge currentEdge = temp.firstEdge;
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
            if (temp.nextVertex != null) {
                sb.append("\n");
            }
            temp = temp.nextVertex;
        }
        return sb.toString();
    }

    /**
     * Find all the path from source to destination (Edge of the 2 vertices that are not FRIEND will not be passed through)
     * @param source source vertex
     * @param destination destination vertex
     * @return a list of path from source to destination
     */
    public List<List<String>> dfs(String source, String destination) {
        this.listOfPathList = new LinkedList<>();
        Map<String, Boolean> isVisited = new HashMap<>();
        StudentVertex currentVertex = head;
        while (currentVertex != null) {
            isVisited.put(currentVertex.getStudentInfo().getName(), false);
            currentVertex = currentVertex.nextVertex;
        }

        LinkedList<String> pathList = new LinkedList<>();

        pathList.add(source);

        dfsUtil(source, destination, isVisited, pathList);
        return this.listOfPathList;
    }

    private void dfsUtil(String current, String destination, Map<String, Boolean> isVisited, List<String> localPathList) {
        if (current.equals(destination)) {
            List<String> copy = new LinkedList<>(localPathList);
            this.listOfPathList.add(copy);
            return;
        }

        isVisited.put(current, true);

        for (Student neighborObj : neighbours(current)) {
            String neighbor = neighborObj.getName();
            if (!isVisited.get(neighbor) && checkRelationship(neighbor, current) == Relationship.FRIEND) {
                localPathList.add(neighbor);

                dfsUtil(neighbor, destination, isVisited, localPathList);

                localPathList.remove(neighbor);
            }
        }
        isVisited.put(current, false);
    }

    /**
     * Static class for vertex of the graph
     */
    static class StudentVertex {
        private Student studentInfo;
        private int indeg;
        private int outdeg;
        private StudentVertex nextVertex;
        private RelationshipEdge firstEdge;

        public StudentVertex() {
            this.studentInfo = null;
            this.indeg = 0;
            this.outdeg = 0;
            this.nextVertex = null;
            this.firstEdge = null;
        }

        public StudentVertex(Student studentInfo, StudentVertex nextVertex) {
            this.studentInfo = studentInfo;
            this.indeg = 0;
            this.outdeg = 0;
            this.nextVertex = nextVertex;
            this.firstEdge = null;
        }

        public Student getStudentInfo() {
            return studentInfo;
        }
    }

    /**
     * Static class for edge of the graph
     */
    static class RelationshipEdge {
        private StudentVertex adjVertex;
        private RelationshipEdge nextEdge;

        // Also the weight of the edge
        private double repRelativeToAdj; // src's rep point in the opinion of adjVertex
        private Relationship relationship;

        public RelationshipEdge() {
            this(null, 0, null, null);
        }

        public RelationshipEdge(StudentVertex adjVertex, double repRelativeToAdj, RelationshipEdge nextEdge) {
            this(adjVertex, repRelativeToAdj, null, nextEdge);
        }

        public RelationshipEdge(StudentVertex adjVertex, double repRelativeToAdj, Relationship relationship, RelationshipEdge nextEdge) {
            this.adjVertex = adjVertex;
            this.repRelativeToAdj = repRelativeToAdj;
            this.relationship = relationship;
            this.nextEdge = nextEdge;
        }
    }
}

enum Relationship { // If relationship is null, means no relationship, but only know the person
    FRIEND, ENEMY
}

