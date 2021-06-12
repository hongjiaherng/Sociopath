package org.sociopath.controllers;

import javafx.animation.FillTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;

import java.util.Optional;
import java.util.Random;

public class Event1Controller {

    // TODO: I changed this, which I think is a better way
    private static GraphSimulationController canvasRef = MainPageController.canvasRef;

    // TODO : Try to see whether can improve the animation
    public static void event1prompt(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);

        // check whether a vertex is selected
        if(selectedVertex == null){
            alert.setContentText("Please select a student as the student that wanted to teach lab questions!");
            alert.show();
        }

        // Too less vertex in graph
        else if(sociograph.getSize() < 2) {
            alert.setContentText("This event needs at least 2 people in your graph!");
            alert.show();
        }

        else{
            String teacher = selectedVertex.nameText.getText();

            String descriptionText = "Here’s a stranger who is seeking your help to teach him/her how to solve the Data Structure \n" +
                    "course’s lab question. As a kind person, you will always help them out.\n" +
                    "If you did something good to a person you increase your rep points relative to that person by 10. \n" +
                    "The stranger becomes your friend now. He or she might tell his/her friends about you.\n" +
                    "But if you are bad at programming, the learning experience with you might not be pleasant, you \n" +
                    "will still be friends with him, but your rep points relative to that person will be 2 instead (Note that \n" +
                    "you were strangers before this, now you guys are friends and your rep point relative to that person \n" +
                    "is 2)";
            String title = "Event 1 - Teaching a stranger a lab question";
            String headerText = "Description";

            Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, headerText, descriptionText);
            if(result.isPresent() && result.get() == ButtonType.OK){
                TextInputDialog enterStudentDL = new TextInputDialog();
                canvasRef.setDefaultDialogConfig(enterStudentDL);
                enterStudentDL.setTitle("Event 1 - Teaching a stranger a lab question");

                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(20, 10, 30, 10));

                DialogPane dialogPane = enterStudentDL.getDialogPane();

                TextField studentTF = new TextField();
                studentTF.setPromptText("Name of the student");
                studentTF.setPrefWidth(100);

                gridPane.add(new Label("Student being taught"), 0, 0);
                gridPane.add(studentTF, 1, 0);

                dialogPane.setContent(gridPane);

                Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->

                        // check does the graph have the student
                        !sociograph.hasVertex(studentTF.getText().trim()) ||

                        // check whether they have relationship
                        sociograph.hasDirectedEdge(teacher, studentTF.getText().trim()) ||
                        sociograph.hasDirectedEdge(studentTF.getText().trim(), teacher) ||
                        sociograph.hasUndirectedEdge(teacher, studentTF.getText().trim()) ||

                        // check whether the entered text is same as the teacher
                        studentTF.getText().trim().equals(teacher)

                , studentTF.textProperty()));

                String studentName ;

                Optional<String> inputResult = enterStudentDL.showAndWait();

                if(inputResult.isPresent()){
                    studentName = studentTF.getText().trim();
                }

                else{
                    return;
                }

                GraphSimulationController.VertexFX teacherVertex = selectedVertex;
                GraphSimulationController.VertexFX studentVertex = canvasRef.getVertexFX(studentName);

                event1Execution(sociograph, teacherVertex, studentVertex);
            }
        }

    }

    private static void event1Execution(Sociograph sociograph, GraphSimulationController.VertexFX teacherVertex, GraphSimulationController.VertexFX studentVertex) {
        Random rd = new Random();

        String student = studentVertex.nameText.getText();
        String teacher = teacherVertex.nameText.getText();

        double repSrc = rd.nextDouble() < 0.5 ? 2 : 10;
        double repDest = rd.nextInt(10) + 1;
        GraphSimulationController.EdgeFX friendEdgeFX = canvasRef.createNewUndirectedEdgeFX(teacherVertex, studentVertex, String.valueOf(repSrc), String.valueOf(repDest), Relationship.FRIEND);
        FillTransition teacherFT = new FillTransition();
        teacherFT.setDuration(Duration.millis(500));
        teacherFT.setFromValue(Color.GREY);
        teacherFT.setToValue(Color.RED);
        teacherFT.setShape(teacherVertex);
        teacherFT.play();

        FillTransition studentFT = new FillTransition();
        studentFT.setDuration(Duration.millis(500));
        studentFT.setFromValue(Color.GREY);
        studentFT.setToValue(Color.RED);
        studentFT.setShape(studentVertex);
        studentFT.play();

        boolean checkSuccess = repSrc == 10;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        canvasRef.setDefaultDialogConfig(alert);
        if (!checkSuccess) {

            boolean isEnemyRandom = !(rd.nextDouble() < 0.3);

            if (isEnemyRandom) {
                alert.setContentText("UH OH! Your friend has become your enemy! \uD83D\uDE1E (Both of the rep points will now become negative!)");
                alert.show();

                double srcRep = sociograph.getSrcRepRelativeToAdj(teacher, student);
                double destRep = sociograph.getSrcRepRelativeToAdj(student, teacher);
                canvasRef.deleteEdgeFXWithoutPrompt(friendEdgeFX);
                canvasRef.createNewUndirectedEdgeFX(teacherVertex, studentVertex, String.valueOf(-srcRep), String.valueOf(-destRep), Relationship.ENEMY);

            } else {
                alert.setContentText("Fortunately, you're great enough and he/she is still your friend! YAY!");
                alert.show();
            }
        }

        else{
            alert.setContentText("You two are now friends because you had taught good!");
            alert.show();
        }
        System.out.println(sociograph);
        System.out.println();

    }
}
