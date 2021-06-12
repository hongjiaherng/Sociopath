package org.sociopath.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;


import java.awt.Point;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

public class SixDegreeController {

    private static GraphSimulationController canvasRef = MainPageController.canvasRef;
    private static SequentialTransition st = new SequentialTransition();

    public static void sixDegreePrompt(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);

        if(selectedVertex == null){
            alert.setContentText("You must select a vertex as the source for this event!");
            alert.show();
        }

        else if(sociograph.getSize() < 2){
            alert.setContentText("This event needs at least 2 people to start doing!");
            alert.show();
        }

        else{
            String srcName = selectedVertex.nameText.getText();

            String descriptionText = "Speaking of six degrees of separation, one of the demos is a huge fan of Ken Thompson. \n" +
                    "For those of you who are unaware, Ken Thompson is one of the creators of UNIX and has \n" +
                    "worked on many influential programming languages like C, C++ and Go. Use the \n" +
                    "sociopath app to calculate a path of correspondence between you and Ken Thompson \n" +
                    "that is less than or equal to 6 hops.";

            String title = "Six Degree of Ken Thompson";
            String header = "Description";

            Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, header, descriptionText);
            if(result.isPresent() && result.get() == ButtonType.OK){
                TextInputDialog enterDestVertexDL = new TextInputDialog();
                canvasRef.setDefaultDialogConfig(enterDestVertexDL);
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

                sixDegreeOfKenThompson(sociograph, srcName, destVertexName);

            }
        }
    }

    public static void sixDegreeOfKenThompson(Sociograph sociograph, String srcName, String destName){
        Random rd = new Random();
        st.getChildren().clear();

        if(destName.equals("Ken Thompson")){
            List<Student> allVertices = sociograph.getAllStudents();
            int randomNum = rd.nextInt(allVertices.size());

            Student student = allVertices.get(randomNum);
            GraphSimulationController.VertexFX studentVertex = canvasRef.getVertexFX(student.getName());

            Point coordinates = studentVertex.coordinate;
            GraphSimulationController.VertexFX newVertex = canvasRef.createVertexFX(coordinates.x + 30, coordinates.y + 30, 1.2, "Ken Thompson");

            String srcRep = String.valueOf((double)Math.round(rd.nextDouble() * 100 ) / 10);
            String destRep = String.valueOf((double)Math.round(rd.nextDouble() * 100 ) / 10);
            canvasRef.createNewUndirectedEdgeFX(studentVertex, newVertex,srcRep, destRep, Relationship.FRIEND);

        }

        StringBuilder sb = new StringBuilder();
        List<List<String>> allPaths = sociograph.bfs(srcName, destName);

        List<String> shortestPath = shortestPath(allPaths);
        if(shortestPath!=null) {
            for (String name : shortestPath) {
                FillTransition ft = new FillTransition(Duration.millis(1000), canvasRef.getVertexFX(name), Color.BLACK, Color.RED);
                st.getChildren().add(ft);
            }
        }

        st.setOnFinished(event -> {
            int size = 0;
            if(shortestPath == null)
                size = 1;
            else
                size = shortestPath.size();

            PauseTransition pt = new PauseTransition(Duration.millis(1000 * size * 2));
            pt.play();

            sb.append("All the paths from ").append(srcName).append(" to ").append(destName).append(" are : \n");
            sb.append(allPaths).append("\n\n");



            sb.append("The shortest path is : \n");
            sb.append(shortestPath).append("\n\n");

            if(shortestPath!=null) {
                for (String name : shortestPath) {
                    FillTransition ft = new FillTransition(Duration.millis(1000), canvasRef.getVertexFX(name), Color.RED, Color.BLACK);
                    ft.play();
                }
            }

            sb.append("And the relationships are : \n");
            sb.append(printOutAllRelation(shortestPath, sociograph));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            canvasRef.setDefaultDialogConfig(alert);
            alert.setTitle("Six Degree of " + destName);
            alert.setContentText(sb.toString());
            alert.setResizable(true);
            alert.show();
        });

        st.onFinishedProperty();
        st.play();

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

    private static String printOutAllRelation(List<String> shortestPath, Sociograph sociograph){
        if(shortestPath == null)
            return "No shortest path";

        StringBuilder sb = new StringBuilder();
        IntStream.range(0, shortestPath.size() - 1)
                .boxed()
                .forEach(index -> {
                    String srcName = shortestPath.get(index);
                    String destName = shortestPath.get(index+1);
                    Relationship rel = sociograph.checkRelationship(srcName, destName);
                    sb.append(srcName).append(" is ").append(rel).append(" to ").append(destName).append("\n");
                });

        return sb.toString();
    }
}
