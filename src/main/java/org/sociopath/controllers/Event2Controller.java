package org.sociopath.controllers;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class Event2Controller {
    public static void event2Prompt(GraphSimulationController controller, Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        controller.setDefaultDialogConfig(alert);
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
            controller.setDefaultDialogConfig(description);
            description.getDialogPane().setPrefWidth(400);
            description.getDialogPane().setPrefHeight(350);
            description.setTitle("Event 2 - Chit Chat");
            description.setHeaderText("Description");

            Optional<ButtonType> result = description.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.NEXT) {
                TextInputDialog enterNewFriendDL = new TextInputDialog();
                controller.setDefaultDialogConfig(enterNewFriendDL);
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
                GraphSimulationController.VertexFX newFriendFX = controller.getVertexFX(newFriendName);
                controller.createNewEdgeFX(hostFX, newFriendFX, srcRep, destRep, Relationship.FRIEND);

                // TODO: Apply transition effect here

                event2Execution(controller, sociograph, hostName, newFriendName);
            }
        }
    }

    private static void event2Execution(GraphSimulationController controller, Sociograph sociograph, String hostName, String newFriendName) {
        HashSet<Student> visitedRecord = new HashSet<>();
        visitedRecord.add(sociograph.getStudent(newFriendName));
        visitedRecord.add(sociograph.getStudent(hostName));

        event2Recur(sociograph, hostName, newFriendName, visitedRecord);

        visitedRecord.remove(sociograph.getStudent(newFriendName));
        visitedRecord.remove(sociograph.getStudent(hostName));

        Alert summary = new Alert(Alert.AlertType.INFORMATION);
        controller.setDefaultDialogConfig(summary);
        summary.setHeaderText("Event ended");
        summary.getDialogPane().setPrefHeight(300);
        summary.getDialogPane().setPrefWidth(400);

        if (visitedRecord.size() != 0) {
            StringBuilder newlyKnownPplSB = new StringBuilder();
            visitedRecord.forEach(student -> newlyKnownPplSB.append(", "));
            String newlyKnownPpl = newlyKnownPplSB.substring(0, newlyKnownPplSB.length() - 2);
            summary.setContentText("Your new friend " + newFriendName + "'s friends and others knew about you now! They are " + newlyKnownPpl + ".");
        } else {
            summary.setContentText("No any student hear about you from your new friend");
        }
        summary.show();
    }

    private static void event2Recur(Sociograph sociograph, String hostName, String newFriendName, HashSet<Student> visitedRecord) {
        List<Student> friendsOfNewFriend = sociograph.neighbours(newFriendName);
        friendsOfNewFriend.forEach(student -> System.out.print(student + " "));
        System.out.println();
        friendsOfNewFriend.removeAll(visitedRecord);
        if (friendsOfNewFriend.isEmpty()) {
            return;
        } else {
            for (Student friend : friendsOfNewFriend) {
                if (visitedRecord.contains(friend)) {
                    continue;
                }

                // TODO: Transition effect here to friend

                double hostRepRelativeToFriend = 0;
                if (Math.random() < 0.5) {  // if talk bad
                    hostRepRelativeToFriend -= Math.abs(sociograph.getSrcRepRelativeToAdj(hostName, newFriendName));
                } else {    // if talk good
                    hostRepRelativeToFriend += (sociograph.getSrcRepRelativeToAdj(hostName, newFriendName) / 2.0);
                }

                // Update graph (update student's properties, add edge)
                if (sociograph.hasDirectedEdge(hostName, friend.getName())) {
                    hostRepRelativeToFriend += sociograph.getSrcRepRelativeToAdj(hostName, friend.getName());
                    sociograph.setSrcRepRelativeToAdj(hostName, friend.getName(), hostRepRelativeToFriend);     // Change rep

                    // TODO: Change rep here
                } else {
                    sociograph.addDirectedEdge(hostName, friend.getName(), hostRepRelativeToFriend, Relationship.NONE);     // Add new directed edge

                    // TODO: Add new edge here
                }

                visitedRecord.add(friend);
                System.out.println("Propagated: " + friend.getName());
//                System.out.println(sociograph);
                System.out.println();

                event2Recur(sociograph, hostName, friend.getName(), visitedRecord);
            }
        }
    }

}
