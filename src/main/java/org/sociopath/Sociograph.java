package org.sociopath;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Sociograph {
    private StudentNode head;
    private int size;

    public Sociograph() {
        this.head = null;
        this.size = 0;
    }

    public int getSize() {
        return size;
    }

    public boolean hasNode(String name) {
        if (head == null) {
            return false;
        }
        StudentNode temp = head;
        while (temp != null) {
            if (temp.studentInfo.getName().equals(name)) {
                return true;
            }
            temp = temp.nextNode;
        }
        return false;
    }

    public int getDegree(String name) {
        if (hasNode(name)) {
            StudentNode temp = head;
            while (temp != null) {
                if (temp.studentInfo.getName().equals(name)) {
                    return temp.degree;
                }
                temp = temp.nextNode;
            }
        }
        return -1;
    }

    public boolean hasEdge(String srcName, String destName) {
        if (head == null) {
            return false;
        } else if (!hasNode(srcName) || !hasNode(destName)) {
            return false;
        }
        StudentNode srcNode = head;
        while (srcNode != null) {
            if (srcNode.studentInfo.getName().equals(srcName)) {
                FriendshipEdge currentEdge = srcNode.firstEdge;
                while (currentEdge != null) {
                    if (currentEdge.adjNode.studentInfo.getName().equals(srcName)) {
                        return true;
                    }
                    currentEdge = currentEdge.nextEdge;
                }
            }
            srcNode = srcNode.nextNode;
        }
        return false;
    }

    public int getIndex(String name) {
        StudentNode temp = head;
        int pos = 0;
        while (temp != null) {
            if (temp.studentInfo.getName().equals(name)) {
                return pos;
            }
            temp = temp.nextNode;
            pos++;
        }
        return -1;
    }

    public Student getNode(int pos) {
        if (pos >= size || pos < 0) {
            return null;
        }
        StudentNode temp = head;
        for (int i = 0; i < pos; i++) {
            temp = temp.nextNode;
        }
        return temp.studentInfo;
    }

    public int getSrcEdgeWeight(String srcName, String adjName) {
        if (head == null) {
            return -1;
        } else if (!hasNode(srcName) || !hasNode(adjName)) {
            return -1;
        }
        StudentNode srcNode = head;
        while (srcNode != null) {
            if (srcNode.studentInfo.getName().equals(srcName)) {
                FriendshipEdge currentEdge = srcNode.firstEdge;
                while (currentEdge != null) {
                    if (currentEdge.adjNode.studentInfo.getName().equals(adjName)) {
                        return currentEdge.repRelativeToAdj;
                    }
                    currentEdge = currentEdge.nextEdge;
                }
            }
            srcNode = srcNode.nextNode;
        }
        return -1;
    }

    public boolean addNode(String name) {
        if (!hasNode(name)) {
            StudentNode temp = head;
            StudentNode newNode = new StudentNode(new Student(name), null);

            if (head == null) {
                head = newNode;
            } else {
                StudentNode previous = head;
                while (temp != null) {
                    previous = temp;
                    temp = temp.nextNode;
                }
                previous.nextNode = newNode;
            }
            size++;
            return true;
        }
        return false;
    }

    public boolean addEdge(String srcName, String destName, int srcRep, int destRep) {
        if (head == null) {
            return false;
        } else if (!hasNode(srcName) || !hasNode(destName)) {
            return false;
        }
        StudentNode srcNode = head;
        while (srcNode != null) {
            if (srcNode.studentInfo.getName().equals(srcName)) {
                StudentNode destNode = head;
                while (destNode != null) {
                    if (destNode.studentInfo.getName().equals(destName)) {
                        FriendshipEdge newSrcEdge = new FriendshipEdge(destNode, srcRep, srcNode.firstEdge);
                        srcNode.firstEdge = newSrcEdge;
                        srcNode.studentInfo.getRepPoints().put(destName, srcRep);
                        srcNode.studentInfo.getFriends().add(destName);
                        srcNode.degree++;

                        FriendshipEdge newDestEdge = new FriendshipEdge(srcNode, destRep, destNode.firstEdge);
                        destNode.firstEdge = newDestEdge;
                        destNode.studentInfo.getRepPoints().put(srcName, destRep);
                        destNode.studentInfo.getFriends().add(srcName);
                        destNode.degree++;

                        return true;
                    }
                    destNode = destNode.nextNode;
                }
            }
            srcNode = srcNode.nextNode;
        }
        return false;
    }

    public ArrayList<Student> getAllVertices() {
        ArrayList<Student> list = new ArrayList<>();
        StudentNode temp = head;
        while (temp != null) {
            list.add(temp.studentInfo);
            temp = temp.nextNode;
        }
        return list;
    }

    public ArrayList<Student> neighbours(String name) {
        if (!hasNode(name)) {
            throw new NoSuchElementException("Node name is not exist");
        }
        ArrayList<Student> list = new ArrayList<>();
        StudentNode temp = head;
        while (temp != null) {
            if (temp.studentInfo.getName().equals(name)) {
                FriendshipEdge currentEdge = temp.firstEdge;
                while (currentEdge != null) {
                    list.add(currentEdge.adjNode.studentInfo);
                    currentEdge = currentEdge.nextEdge;
                }
            }
            temp = temp.nextNode;
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        StudentNode temp = head;
        while (temp != null) {
            sb.append(temp.studentInfo.getName()).append("\t=> [");
            FriendshipEdge currentEdge = temp.firstEdge;
            while (currentEdge != null) {
                sb.append("(").append(currentEdge.adjNode.studentInfo.getName()).append(" | ");
                sb.append("rep:").append(currentEdge.repRelativeToAdj).append(")");
                if (currentEdge.nextEdge != null) {
                    sb.append(", ");
                }
                currentEdge = currentEdge.nextEdge;
            }
            sb.append("]");
            if (temp.nextNode != null) {
                sb.append("\n");
            }
            temp = temp.nextNode;
        }
        return sb.toString();
    }

    static class StudentNode {
        private Student studentInfo;
        private int degree;
        private StudentNode nextNode;
        private FriendshipEdge firstEdge;

        public StudentNode() {
            this.studentInfo = null;
            this.degree = 0;
            this.nextNode = null;
            this.firstEdge = null;
        }

        public StudentNode(Student studentInfo, StudentNode nextNode) {
            this.studentInfo = studentInfo;
            this.degree = 0;
            this.nextNode = nextNode;
            this.firstEdge = null;
        }

        public Student getStudentInfo() {
            return studentInfo;
        }
    }

    static class FriendshipEdge {
        private StudentNode adjNode;
        private FriendshipEdge nextEdge;
        private int repRelativeToAdj; // src's rep point in the opinion of adjNode

        public FriendshipEdge() {
            this.adjNode = null;
            this.nextEdge = null;
            this.repRelativeToAdj = 0;
        }

        public FriendshipEdge(StudentNode adjNode, int repRelativeToAdj, FriendshipEdge nextEdge) {
            this.adjNode = adjNode;
            this.repRelativeToAdj = repRelativeToAdj;
            this.nextEdge = nextEdge;
        }
    }
}


