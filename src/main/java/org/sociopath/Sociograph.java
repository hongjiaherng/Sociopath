package org.sociopath;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Graph object to simulate friendship
 */
public class Sociograph {

    private StudentVertex head;   // First vertex of the graph
    private int size;           // Total vertices in the graph

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
                FriendshipEdge currentEdge = srcVertex.firstEdge;
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
                FriendshipEdge currentEdge = srcVertex.firstEdge;
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
        throw new NoSuchElementException("No friendship");
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
                FriendshipEdge currentEdge = srcVertex.firstEdge;
                while (currentEdge != null) {
                    if (currentEdge.adjVertex.studentInfo.getName().equals(adjName)) {
                        currentEdge.repRelativeToAdj = newWeight;
                        return;
                    }
                    currentEdge = currentEdge.nextEdge;
                }
                break;
            }
            srcVertex = srcVertex.nextVertex;
        }
        throw new NoSuchElementException("No friendship");
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
     * <br> Also note that self loop is not allowed in this graph.
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
                        FriendshipEdge newSrcEdge = new FriendshipEdge(destVertex, srcRep, srcVertex.firstEdge);
                        srcVertex.firstEdge = newSrcEdge;
                        srcVertex.studentInfo.getRepPoints().put(adjName, srcRep);
                        srcVertex.studentInfo.getFriends().add(adjName);
                        srcVertex.indeg++;
                        srcVertex.outdeg++;

                        FriendshipEdge newDestEdge = new FriendshipEdge(srcVertex, adjRep, destVertex.firstEdge);
                        destVertex.firstEdge = newDestEdge;
                        destVertex.studentInfo.getRepPoints().put(srcName, adjRep);
                        destVertex.studentInfo.getFriends().add(srcName);
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
     * Add the directed edges from srcName and adjName with weight "srcRep". Also note that self
     * loop is not allowed in this graph.
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
                        FriendshipEdge newSrcEdge = new FriendshipEdge(destVertex, srcRep, srcVertex.firstEdge);
                        srcVertex.firstEdge = newSrcEdge;
                        srcVertex.studentInfo.getRepPoints().put(adjName, srcRep);
                        srcVertex.studentInfo.getFriends().add(adjName);
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
                FriendshipEdge currentEdge = temp.firstEdge;
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
            FriendshipEdge currentEdge = temp.firstEdge;
            while (currentEdge != null) {
                sb.append("(").append(currentEdge.adjVertex.studentInfo.getName()).append(" | ");
                sb.append("rep:").append(currentEdge.repRelativeToAdj).append(")");
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
     * Static class for vertex of the graph
     */
    static class StudentVertex {
        private Student studentInfo;
        private int indeg;
        private int outdeg;
        private StudentVertex nextVertex;
        private FriendshipEdge firstEdge;

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
    static class FriendshipEdge {
        private StudentVertex adjVertex;
        private FriendshipEdge nextEdge;

        // Also the weight of the edge
        private double repRelativeToAdj; // src's rep point in the opinion of adjVertex

        public FriendshipEdge() {
            this.adjVertex = null;
            this.nextEdge = null;
            this.repRelativeToAdj = 0;
        }

        public FriendshipEdge(StudentVertex adjVertex, double repRelativeToAdj, FriendshipEdge nextEdge) {
            this.adjVertex = adjVertex;
            this.repRelativeToAdj = repRelativeToAdj;
            this.nextEdge = nextEdge;
        }
    }
}


