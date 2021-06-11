package org.sociopath.controllers;

import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class SixDegreeController {

    public static void sixDegreePrompt(GraphSimulationController controller, Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex){

    }

    public static void sixDegreeOfKenThompson(GraphSimulationController controller,Sociograph sociograph){
        Random rd = new Random();

        System.out.print("Which node to start first ? ");
        String srcName = "";

        System.out.println("Which node to end? ");
        System.out.println("Or do you want to add Ken Thompson to the graph? (Type 'Ken Thompson' and it will add it!)");
        String destName = "";

        if(destName.equals("Ken Thompson")){
            List<Student> allVertices = sociograph.getAllStudents();
            int randomNum = rd.nextInt(allVertices.size());
            sociograph.addVertex(destName);

            Student student = allVertices.get(randomNum);
            sociograph.addUndirectedEdge(student.getName(), destName, rd.nextDouble() * 10 , rd.nextDouble() * 10, Relationship.FRIEND );
        }

        List<List<String>> allPaths = sociograph.bfs(srcName, destName);
        System.out.println("All the paths are : ");
        System.out.println(allPaths);
        List<String> shortestPath = shortestPath(allPaths);

        System.out.println("The shortest path is : ");
        System.out.println(shortestPath);

        System.out.println("\nAnd the relationships are : ");
        printOutAllRelation(shortestPath, sociograph);
    }

    private static List<String> shortestPath(List<List<String>> paths){

        int minIndex = IntStream.range(0, paths.size())
                .boxed()
                .min(Comparator.comparingInt(i -> paths.get(i).size()))
                .orElse(0);

        if(paths.size() == 0)
            return null;

        return paths.get(minIndex);
    }

    private static void printOutAllRelation(List<String> shortestPath, Sociograph sociograph){
        if(shortestPath == null) {
            System.out.println("No shortest path");
            return;
        }

        IntStream.range(0, shortestPath.size() - 1)
                .boxed()
                .forEach(index -> {
                    String srcName = shortestPath.get(index);
                    String destName = shortestPath.get(index+1);
                    Relationship rel = sociograph.checkRelationship(srcName, destName);
                    System.out.println(srcName + " is " + rel + " to " + destName);
                });
    }
}
