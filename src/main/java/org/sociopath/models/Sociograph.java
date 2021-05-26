package org.sociopath.models;

import java.util.ArrayList;
import java.util.List;

public class Sociograph {

    private List<Vertex> vertices;
    private int size;

    public Sociograph() {
        this.vertices = new ArrayList<>();
        this.size = 0;
    }

    public int getSize() {
        return size;
    }

    public boolean hasVertex(String name) {

    }

    public int getOutdeg(String name) {

    }

    public int getIndeg(String name) {

    }

    public boolean addVertex(String name) {

    }

    public boolean addUndirectedEdge(String srcName, String adjName, double srcRep, double adjRep, Relationship rel) {

    }

    public boolean addDirectedEdge(String srcName, String adjName, double srcRep) {

    }

    public List<Student> getAllStudents() {

    }

    public List<Student> neighbours(String name) {

    }

    @Override
    public String toString() {

    }

    static class Vertex {
        private Student studentInfo;
        private int indeg;
        private int outdeg;
        private Edge firstEdge;

        public Vertex() {
            this.studentInfo = null;
            this.indeg = 0;
            this.outdeg = 0;
            this.firstEdge = null;
        }

        public Vertex(Student studentInfo) {
            this.studentInfo = studentInfo;
            this.indeg = 0;
            this.outdeg = 0;
            this.firstEdge = null;
        }
    }

    static class Edge {
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