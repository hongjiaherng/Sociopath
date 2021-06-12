package org.sociopath.controllers;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

public class SixDegreeController {

    public static void sixDegreePrompt(GraphSimulationController controller, Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        controller.setDefaultDialogConfig(alert);

        if(selectedVertex == null){
            alert.setContentText("You must select a vertex as the source for this event!");
            alert.show();
        }

        else if(sociograph.getSize() < 3){
            alert.setContentText("This event needs at least 3 people to start doing!");
            alert.show();
        }

        else{
            String srcName = selectedVertex.nameText.getText();

            String descriptionText = "Speaking of six degrees of separation, one of the demos is a huge fan of Ken Thompson. \n" +
                    "For those of you who are unaware, Ken Thompson is one of the creators of UNIX and has \n" +
                    "worked on many influential programming languages like C, C++ and Go. Use the \n" +
                    "sociopath app to calculate a path of correspondence between you and Ken Thompson \n" +
                    "that is less than or equal to 6 hops. Present your methodology on identifying and validating \n" +
                    "this path.";

            Alert description = new Alert(Alert.AlertType.INFORMATION, descriptionText, ButtonType.NEXT);
            controller.setDefaultDialogConfig(description);
            description.getDialogPane().setPrefWidth(450);
            description.getDialogPane().setPrefHeight(400);
            description.setTitle("Six Degree of Ken Thompson");
            description.setHeaderText("Description");

            Optional<ButtonType> result = description.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.NEXT){
                TextInputDialog enterDestVertexDL = new TextInputDialog();
                controller.setDefaultDialogConfig(enterDestVertexDL);
                enterDestVertexDL.setTitle("Six Degree of Ken Thompson");

                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(20);
                gridPane.setPadding(new Insets(20, 10, 30, 10));

                DialogPane dialogPane = enterDestVertexDL.getDialogPane();
                TextField destVertexTF = new TextField();
                destVertexTF.setPromptText("Name of the Vertex");
                destVertexTF.setPrefWidth(100);

                gridPane.add(new Label("Destination Vertex"),0 ,0);
                gridPane.add(destVertexTF, 1,0);

                dialogPane.setContent(gridPane);
                Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->

                            !destVertexTF.getText().trim().equals("Ken Thompson") &&
                            !sociograph.hasVertex(destVertexTF.getText().trim())

                        , destVertexTF.textProperty()));

                String destVertexName ;

                Optional<String> inputResult = enterDestVertexDL.showAndWait();
                if(inputResult.isPresent()){
                    destVertexName = destVertexTF.getText().trim();
                }

                else{
                    return;
                }

                GraphSimulationController.VertexFX sourceVertex = selectedVertex;
                if(destVertexName.equals("Ken Thompson")){
                    GraphSimulationController.VertexFX newVertex = new GraphSimulationController.VertexFX()
                }


            }
        }
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
