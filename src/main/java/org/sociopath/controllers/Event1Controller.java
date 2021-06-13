package org.sociopath.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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

    private static GraphSimulationController canvasRef = MainPageController.canvasRef;
    private static SequentialTransition st = new SequentialTransition();

    // Basic Output for event 1
    public static void event1prompt(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);

        // check whether a vertex is selected
        if(selectedVertex == null){
            alert.setContentText("Please select a vertex as the teacher that wanted to teach lab questions!");
            alert.show();
        }

        // Too less vertex in graph
        else if(sociograph.getSize() < 2) {
            alert.setContentText("This event needs at least 2 people in your graph!");
            alert.show();
        }

        // Start the event with some user inputs and description
        else{
            String teacher = selectedVertex.nameText.getText();

            String descriptionText = "Here’s a stranger who is seeking your help to teach him/her how to solve the Data Structure " +
                    "course’s lab question. As a kind person, you will always help them out.\n" +
                    "If you did something good to a person you increase your rep points relative to that person by 10. The stranger becomes your friend now. " +
                    "He or she might tell his/her friends about you. But if you are bad at programming, the learning experience with you might not be pleasant, you " +
                    "will still be friends with him, but your rep points relative to that person will be 2 instead (Note that " +
                    "you were strangers before this, now you guys are friends and your rep point relative to that person is 2)";
            String title = "Event 1 - Teaching a stranger a lab question";
            String headerText = "Description";

            // Check whether the user had clicked on the OK button
            Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, headerText, descriptionText);
            if(result.isPresent() && result.get() == ButtonType.OK){

                // A input pop up window for the user to enter the
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
                studentTF.setPrefWidth(150);

                gridPane.add(new Label("Student's name"), 0, 0);
                gridPane.add(studentTF, 1, 0);

                dialogPane.setContent(gridPane);
                dialogPane.setPrefWidth(300);

                // Disable the OK button if any of these conditions happen
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

                String student ;

                Optional<String> inputResult = enterStudentDL.showAndWait();

                if(inputResult.isPresent()){
                    student = studentTF.getText().trim();
                }

                else{
                    return;
                }

                event1Execution(sociograph,teacher, student);
            }
        }

    }

    private static void event1Execution(Sociograph sociograph,String teacher, String student) {
        Random rd = new Random();

        // Clear previously added animations
        st.getChildren().clear();

        double repSrc = rd.nextDouble() < 0.5 ? 2 : 10;
        double repDest = rd.nextInt(10) + 1;

        // Create a new edge (FRIENDS) between them at the starting
        // Fade for the edge and Fill for the vertices that are being involved
        GraphSimulationController.EdgeFX friendEdgeFX = canvasRef.createNewUndirectedEdgeFX(canvasRef.getVertexFX(teacher), canvasRef.getVertexFX(student), String.valueOf(repSrc), String.valueOf(repDest), Relationship.FRIEND);
        FadeTransition friendFT = new FadeTransition(Duration.millis(1000),friendEdgeFX);
        friendFT.setFromValue(0);
        friendFT.setToValue(10);

        // FillTransition for both the teacherVertex and the StudentVertex
        FillTransition teacherFT = new FillTransition(Duration.millis(1000), canvasRef.getVertexFX(teacher), Color.BLACK, Color.YELLOW);
        FillTransition studentFT = new FillTransition(Duration.millis(1000), canvasRef.getVertexFX(student), Color.BLACK, Color.YELLOW);

        st.getChildren().add(teacherFT);
        st.getChildren().add(studentFT);
        st.getChildren().add(friendFT);

        boolean checkSuccess = repSrc == 10;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        canvasRef.setDefaultDialogConfig(alert);
        StringBuilder sb = new StringBuilder();

        // If the teaching is not success, a percentage to become an enemy
        if (!checkSuccess) {
            boolean isEnemyRandom = !(rd.nextDouble() < 0.3);

            // The percentage had hit the target to become an enemy
            if (isEnemyRandom) {
                sb.append("UH OH! Your friend has become your enemy! \uD83D\uDE1E (Both of the rep points will now become negative!) \n");

                // delete the edge
                canvasRef.deleteEdgeFXWithoutPrompt(friendEdgeFX);

                // create the new ENEMY edge and show on the graph
                GraphSimulationController.EdgeFX enemyEdgeFX = canvasRef.createNewUndirectedEdgeFX(canvasRef.getVertexFX(teacher), canvasRef.getVertexFX(student),
                        String.valueOf(-repSrc), String.valueOf(-repDest), Relationship.ENEMY);

                // The animation for the new enemy edge
                FadeTransition enemyFT = new FadeTransition(Duration.millis(1000),enemyEdgeFX);
                enemyFT.setFromValue(0);
                enemyFT.setToValue(10);

                st.getChildren().add(enemyFT);


            } else {
                sb.append("Fortunately, ").append(teacher).append(" is great enough and ").append(student).append(" is still your friend! YAY! \n");
            }
        }

        else{
            sb.append(teacher).append(" and ").append(student).append(" are now good friends because ").append(teacher).append(" had taught good!");
        }

        PauseTransition pt = new PauseTransition(Duration.millis(1500));
        st.getChildren().add(pt);

        System.out.println(sociograph);
        System.out.println();

        // When the events finish, then it will run this
        st.setOnFinished(event -> {
            PauseTransition pt2 = new PauseTransition(Duration.millis(4000));
            pt2.play();

            alert.setContentText(sb.toString());
            alert.getDialogPane().setPrefWidth(300);
            alert.getDialogPane().setPrefHeight(200);
            alert.show();

            // Turn back the student and teacher vertex back to the original colour
            FillTransition ftTeacher = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(teacher), Color.YELLOW, Color.BLACK);
            ftTeacher.play();

            FillTransition ftStudent = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(student), Color.YELLOW, Color.BLACK);
            ftStudent.play();
        });

        // To let the SequentialTransition knows that here is the end so that it can run anything inside the setOnAction()
        st.onFinishedProperty();

        // Play the animation
        st.play();

    }
}
