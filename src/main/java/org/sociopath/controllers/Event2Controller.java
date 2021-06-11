package org.sociopath.controllers;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class Event2Controller {

    private static GraphSimulationController canvasRef = MainPageController.canvasRef;
    private static SequentialTransition st = new SequentialTransition();

    public static void event2Prompt(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);
        if (selectedVertex == null) {
            alert.setContentText("Please select a student!");
            alert.show();
        } else if (sociograph.getSize() < 3) {
            alert.setContentText("This event needs at least 3 students in your graph!");
            alert.show();
        } else {

            String hostName = selectedVertex.nameText.getText();

            String descriptionTxt = "For sure your new friend will have a chit-chatting session with his or her friends. If it’s a good " +
                    "message, this increases your rep points relative to them 1.5 times by your rep points relative to your new " +
                    "friend. Otherwise, if it’s a bad message, this person will share the same negative rep points " +
                    "that your new friend owns about you. Later, your new friend’s friends might tell their friends, so it " +
                    "will multiply and propagate your rep.";

            Alert description = new Alert(Alert.AlertType.INFORMATION, descriptionTxt, ButtonType.NEXT);
            canvasRef.setDefaultDialogConfig(description);
            description.getDialogPane().setPrefWidth(400);
            description.getDialogPane().setPrefHeight(350);
            description.setTitle("Event 2 - Chit Chat");
            description.setHeaderText("Description");

            Optional<ButtonType> result = description.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.NEXT) {
                TextInputDialog enterNewFriendDL = new TextInputDialog();
                canvasRef.setDefaultDialogConfig(enterNewFriendDL);
                enterNewFriendDL.setTitle("Event 2 - Chit Chat");
                enterNewFriendDL.setHeaderText("Enter new friend details");

                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(20, 10, 30, 10));

                DialogPane dialogPane = enterNewFriendDL.getDialogPane();

                TextField newFriendTF = new TextField();
                newFriendTF.setPromptText("Name");
                newFriendTF.setPrefWidth(100);

                TextField srcRepTF = new TextField();
                srcRepTF.setPromptText(hostName + "'s rep");
                srcRepTF.setPrefWidth(100);

                TextField destRepTF = new TextField();
                destRepTF.setPromptText("New friend's rep");
                destRepTF.setPrefWidth(100);

                gridPane.add(new Label("New friend's name"), 0, 0);
                gridPane.add(newFriendTF, 1, 0);
                gridPane.add(new Label(hostName + "'s rep"), 0, 1);
                gridPane.add(srcRepTF, 1, 1);
                gridPane.add(new Label("New friend's rep"), 0, 2);
                gridPane.add(destRepTF, 1, 2);

                dialogPane.setContent(gridPane);

                Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                            boolean isNumber;
                            try {
                                double rep = Double.parseDouble(srcRepTF.getText().trim());
                                rep = Double.parseDouble(destRepTF.getText().trim());
                                isNumber = true;
                            } catch (NumberFormatException e) {
                                isNumber = false;
                            }
                            return !isNumber || newFriendTF.getText().trim().isEmpty() ||
                                    newFriendTF.getText().trim().contains(" ") ||
                                    !sociograph.hasVertex(newFriendTF.getText().trim()) ||
                                    newFriendTF.getText().trim().equals(hostName);
                        }
                        , srcRepTF.textProperty(), destRepTF.textProperty(), newFriendTF.textProperty()));


                String newFriendName = null;
                String srcRep = null;
                String destRep = null;

                Optional<String> inputResult = enterNewFriendDL.showAndWait();

                if (inputResult.isPresent()) {
                    newFriendName = newFriendTF.getText().trim();
                    srcRep = srcRepTF.getText().trim();
                    destRep = destRepTF.getText().trim();
                } else {
                    return;
                }

                GraphSimulationController.VertexFX hostFX = selectedVertex;
                GraphSimulationController.VertexFX newFriendFX = canvasRef.getVertexFX(newFriendName);

                // If there's any relationship previously between host and newFriend, delete them and create new Friendship directly
                GraphSimulationController.EdgeFX existingEdgeFX = canvasRef.getEdgeFX(hostName, newFriendName);
                if (existingEdgeFX != null) {
                    canvasRef.deleteEdgeFXWithoutPrompt(existingEdgeFX);
                }

                // Clear all transition to avoid any leftover transition from the past event execution
                st.getChildren().clear();

                FillTransition ft = new FillTransition(Duration.millis(500), hostFX, Color.BLACK, Color.YELLOW);
                st.getChildren().add(ft);

                GraphSimulationController.EdgeFX newUndirectedEdge = canvasRef.createNewUndirectedEdgeFX(hostFX, newFriendFX, srcRep, destRep, Relationship.FRIEND);

                FadeTransition ft1 = new FadeTransition(Duration.millis(500), newUndirectedEdge);
                ft1.setFromValue(0);
                ft1.setToValue(10);
                st.getChildren().add(ft1);

                // Light up new friend
                FillTransition ft2 = new FillTransition(Duration.millis(500), newFriendFX, Color.BLACK, Color.YELLOW);
                st.getChildren().add(ft2);

                event2Execution(sociograph, hostName, newFriendName);
            }
        }
    }

    private static void event2Execution(Sociograph sociograph, String hostName, String newFriendName) {
        HashSet<Student> visitedRecord = new HashSet<>();
        visitedRecord.add(sociograph.getStudent(newFriendName));
        visitedRecord.add(sociograph.getStudent(hostName));

        // Start propagating
        event2Recur(sociograph, hostName, newFriendName, visitedRecord);

        // End propagating
        st.setOnFinished(event -> {
            PauseTransition pt = new PauseTransition(Duration.millis(4000));
            pt.play();

            visitedRecord.remove(sociograph.getStudent(newFriendName));
            visitedRecord.remove(sociograph.getStudent(hostName));

            Alert summary = new Alert(Alert.AlertType.INFORMATION);
            canvasRef.setDefaultDialogConfig(summary);
            summary.setHeaderText("Event ended");
            summary.getDialogPane().setPrefHeight(300);
            summary.getDialogPane().setPrefWidth(400);

            if (visitedRecord.size() != 0) {
                StringBuilder newlyKnownPplSB = new StringBuilder();
                visitedRecord.forEach(student -> newlyKnownPplSB.append(student.getName()).append(", "));
                String newlyKnownPpl = newlyKnownPplSB.substring(0, newlyKnownPplSB.length() - 2);
                summary.setContentText("Your new friend " + newFriendName + "'s friends and others knew about you now! They are " + newlyKnownPpl + ".");
            } else {
                summary.setContentText("No any student hear about you from your new friend");
            }
            summary.show();

            // Changing all the light up vertex back to original color
            for (Student visited : visitedRecord) {
                FillTransition ft = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(visited.getName()), Color.YELLOW, Color.BLACK);
                ft.play();
            }
            FillTransition ftHost = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(hostName), Color.YELLOW, Color.BLACK);
            ftHost.play();
            FillTransition ftFriend = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(newFriendName), Color.YELLOW, Color.BLACK);
            ftFriend.play();

            canvasRef.markEventEnded();
        });
        st.onFinishedProperty();    // Mark the end
        st.play();

    }

    private static void event2Recur(Sociograph sociograph, String hostName, String newFriendName, HashSet<Student> visitedRecord) {

        // Get the friend & couple of Student named 'newFriendName'
        Student thisStudent = sociograph.getStudent(newFriendName);
        List<Student> friendsOfNewFriend = new ArrayList<>(thisStudent.getFriends());
        if (thisStudent.getTheOtherHalf() != null) {
            friendsOfNewFriend.add(thisStudent.getTheOtherHalf());
        }

        // Removed the previous visited vertex from the list of friendsOfNewFriend
        friendsOfNewFriend.removeAll(visitedRecord);

        // If there's no more friend of Student named 'newFriendName', the propagation of speech is ended
        if (friendsOfNewFriend.isEmpty()) {
            return;
        } else {        // If there are still friends, propagate the speech to each them
            for (Student friend : friendsOfNewFriend) {

                // At the process of propagating (recursion), the friend in the friendsOfNewFriend list might already known the thing from other, in this case, skip this person
                if (visitedRecord.contains(friend)) {
                    continue;
                }

                // Light up newly propagated friend
                FillTransition ft = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(friend.getName()), Color.BLACK, Color.YELLOW);
                st.getChildren().add(ft);

                // Calculate the rep of the friend on host
                double hostRepRelativeToFriend = 0;
                if (Math.random() < 0.5) {  // if talk bad
                    hostRepRelativeToFriend -= Math.abs(sociograph.getSrcRepRelativeToAdj(hostName, newFriendName));
                } else {    // if talk good
                    hostRepRelativeToFriend += (sociograph.getSrcRepRelativeToAdj(hostName, newFriendName) / 2.0);
                }

                // Update graph (update student's properties, add edge)
                // If there's already an edge, just increment the rep point
                GraphSimulationController.EdgeFX newOrExistingEdge;

                if (sociograph.hasDirectedEdge(hostName, friend.getName())) {
                    hostRepRelativeToFriend += sociograph.getSrcRepRelativeToAdj(hostName, friend.getName());

                    sociograph.setSrcRepRelativeToAdj(hostName, friend.getName(), hostRepRelativeToFriend);     // Change rep in sociograph
                    canvasRef.changeSrcRepRelativeToAdjFX(hostName, friend.getName(), hostRepRelativeToFriend);     // Change rep for displaying in EdgeFX

                    newOrExistingEdge = canvasRef.getEdgeFX(hostName, friend.getName());

                    // A little fade effect for existing edge
                    FadeTransition ft1 = new FadeTransition(Duration.millis(500), newOrExistingEdge);
                    ft1.setFromValue(5);
                    ft1.setToValue(10);
                    st.getChildren().add(ft1);

                } else {    // If there's no edge at first, create a new directed edge
                    newOrExistingEdge = canvasRef.createNewDirectedEdgeFX(canvasRef.getVertexFX(hostName), canvasRef.getVertexFX(friend.getName()), hostRepRelativeToFriend + "", Relationship.NONE);

                    // Fading in effect for new edge
                    FadeTransition ft1 = new FadeTransition(Duration.millis(500), newOrExistingEdge);
                    ft1.setFromValue(0);
                    ft1.setToValue(10);
                    st.getChildren().add(ft1);
                }

                // Mark this friend as visited
                visitedRecord.add(friend);

                // Wait for a while before proceeding to next student
                PauseTransition pt = new PauseTransition(Duration.millis(3000));
                st.getChildren().add(pt);

                // Propagate the speech to the other friend of current 'friend'
                event2Recur(sociograph, hostName, friend.getName(), visitedRecord);
            }
        }
    }

}
