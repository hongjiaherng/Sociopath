package org.sociopath.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.time.LocalTime;
import java.util.*;

public class Event3Controller {
    /**
     * 1. estimate lunchEnd
     * 2. display a list of possible lunch mates with their lunch details
     * 3. computer your lunch schedule
     * 4. Display the lunch schedule
     * 5. Ask if really wanna have lunch with them
     */
    private static GraphSimulationController canvasRef = MainPageController.canvasRef;
    private static SequentialTransition st = new SequentialTransition();

    // List to keep all the students that has intersection with host's lunch time
    private static List<Student> potentialLunchMates = new ArrayList<>();      // didn't consider the number of ppl that can have lunch with in parallel way (3 persons)
    private static List<Student> actualLunchMates = new ArrayList<>();         // after considering the number of ppl that can simultaneously to have lunch with
    private static ArrayList<String>[] timeslot;
    private static Student host;
    private static int totalRepObtained;

    public static void event3Prompt(Sociograph sociograph, GraphSimulationController.VertexFX hostVertexFX) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);
        alert.setOnHidden(e -> {    // Important! to make sure the state is changed
            canvasRef.markEventEnded();
        });

        if (hostVertexFX == null) {
            alert.setContentText("Please select a student!");
            alert.show();
        } else if (sociograph.getSize() < 2) {
            alert.setContentText("This event needs at least 2 students in your graph!");
            alert.show();
        } else {
            String descriptionTxt = "Who doesn’t want to feel respected? As a university student, you are ready to build " +
                    "up your reputation. One of the cores of human relationships is food. People like food, and if they usually " +
                    "see you when there is food, they are more likely to like you. If you want to build a good relationship with " +
                    "someone, try to have lunch with them. But you want to be efficient. You don’t want to befriend any assignment " +
                    "diver. You want to maximize the amount of reputation you can gain by befriending people with high reliability. " +
                    "To do this, you will estimate their lunch starting and ending time. Use these to find the maximum reputation you can " +
                    "obtain in any day given that you can only have lunch with at most 3 person at one time. Each person that had lunch " +
                    "with you will increase your rep with them by 1 point.";

            String title = "Event 3 - Your road to glory (Parallel farming)";
            String headerText = "Description";
            Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, headerText, descriptionTxt);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                potentialLunchMates.clear();
                actualLunchMates.clear();
                timeslot = null;
                totalRepObtained = 0;
                st.getChildren().clear();
                host = sociograph.getStudent(hostVertexFX.nameText.getText());
                event2Execution(sociograph);
            }
            canvasRef.markEventEnded();
            System.out.println("Ended");
        }
    }

    private static void event2Execution(Sociograph sociograph) {

        if (estimateLunchEnd(sociograph)) {
            if (arrangeLunchSchedule(sociograph)) {
                startLunch(sociograph);

                st.setOnFinished(event -> {
                    PauseTransition pt = new PauseTransition(Duration.millis(4000));
                    pt.play();

                    Alert summary = new Alert(Alert.AlertType.INFORMATION);
                    canvasRef.setDefaultDialogConfig(summary);
                    summary.setHeaderText("Event ended");
                    summary.getDialogPane().setPrefHeight(300);
                    summary.getDialogPane().setPrefWidth(400);

                    StringBuilder sb = new StringBuilder();
                    sb.append("Your obtained total ").append(totalRepObtained).append(" reputation points after having lunch with ");
                    for (int i = 0; i < actualLunchMates.size(); i++) {
                        if (actualLunchMates.size() == 2) {
                            sb.append(actualLunchMates.get(0).getName()).append(" and ").append(actualLunchMates.get(1).getName());
                            break;
                        } else if (i == actualLunchMates.size() - 1) {
                            sb.append("and ").append(actualLunchMates.get(i).getName());
                        } else {
                            sb.append(actualLunchMates.get(i).getName()).append(", ");
                        }
                    }

                    summary.setContentText(sb.toString());
                    summary.show();

                    for (Student lunchMate : actualLunchMates) {
                        FillTransition ft = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(lunchMate.getName()), Color.YELLOW, Color.BLACK);
                        ft.play();
                    }
                    FillTransition ft = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(host.getName()), Color.YELLOW, Color.BLACK);
                    ft.play();
                });
                st.onFinishedProperty();
                st.play();
            }
        } else {
            if (potentialLunchMates.size() == 0) {
                // TODO: Dialog to tell no potential lunch mate
                Alert noLunchMateAlert = new Alert(Alert.AlertType.ERROR);
                canvasRef.setDefaultDialogConfig(noLunchMateAlert);
                noLunchMateAlert.setContentText("You don't have people to have lunch with due to time constraint and their high diving rate");
                noLunchMateAlert.show();
            }

        }
        canvasRef.markEventEnded();
        System.out.println("Ended");
    }

    private static boolean estimateLunchEnd(Sociograph sociograph) {
        host.estimateLunchEnd();

        // Timeslot to keep who are having lunch with host at a certain minute (row array - minute; col arraylist - Student)
        timeslot = new ArrayList[host.getAvgLunchPeriod()];
        for (int i = 0; i < timeslot.length; i++) {
            timeslot[i] = new ArrayList<>();
        }

        // Add the student who has lunch time that intersect with host's lunch time to the list
        // Filter out those students who have high dive rate (>50)
        // Estimate the lunchEnd of everyone
        for (Student mate : sociograph.getAllStudents()) {
            if (mate.getName().equals(host.getName())) {
                continue;
            }
            mate.estimateLunchEnd();
            if (mate.getAvgLunchStart().isBefore(host.getLunchEnd()) &&
                    mate.getLunchEnd().isAfter(host.getAvgLunchStart()) &&
                    mate.getDive() <= 50) {    // Turn diving rate filter off first
                potentialLunchMates.add(mate);
            }
        }

        // The method stops if there's no ppl to have lunch with
        if (potentialLunchMates.size() == 0) {
            return false;
        }

        // Sort the student with avgLunchStart, if avgLunchStart is same, use estimatedLunchEnd instead (ascending)
        potentialLunchMates.sort((mate1, mate2) -> {
            if (mate1.getAvgLunchStart().isBefore(mate2.getAvgLunchStart())) {
                return -1;
            } else if (mate1.getAvgLunchStart().isAfter(mate2.getAvgLunchStart())) {
                return 1;
            } else {
                if (mate1.getLunchEnd().isBefore(mate2.getLunchEnd())) {
                    return -1;
                } else if (mate1.getLunchEnd().isAfter(mate2.getLunchEnd())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        Alert estLunchDialog = new Alert(Alert.AlertType.INFORMATION);
        estLunchDialog.setTitle("Event 3 - Your road to glory (Parallel farming)");
        estLunchDialog.setHeaderText("Lunch time for all potential lunch mates");
        canvasRef.setDefaultDialogConfig(estLunchDialog);
        DialogPane dialogPane = estLunchDialog.getDialogPane();
        dialogPane.setPrefWidth(700);
        dialogPane.setPrefHeight(400);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TableView<Student> tableView = new TableView<>();

        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, LocalTime> avgLunchStartColumn = new TableColumn<>("Avg Lunch Start");
        avgLunchStartColumn.setCellValueFactory(new PropertyValueFactory<>("avgLunchStart"));

        TableColumn<Student, Integer> avgLunchPeriodColumn = new TableColumn<>("Avg Lunch Period");
        avgLunchPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("avgLunchPeriod"));

        TableColumn<Student, LocalTime> estLunchEndColumn = new TableColumn<>("Estimated Lunch End");
        estLunchEndColumn.setCellValueFactory(new PropertyValueFactory<>("lunchEnd"));

        TableColumn<Student, Double> diveRateColumn = new TableColumn<>("Diving Rate (<=50)");
        diveRateColumn.setCellValueFactory(new PropertyValueFactory<>("dive"));

        nameColumn.prefWidthProperty().bind(tableView.widthProperty().divide(10));
        avgLunchStartColumn.prefWidthProperty().bind(tableView.widthProperty().divide(5));
        avgLunchPeriodColumn.prefWidthProperty().bind(tableView.widthProperty().divide(5));
        estLunchEndColumn.prefWidthProperty().bind(tableView.widthProperty().divide(4.286));
        diveRateColumn.prefWidthProperty().bind(tableView.widthProperty().divide(3.75));

        tableView.getColumns().addAll(nameColumn, avgLunchStartColumn, avgLunchPeriodColumn, estLunchEndColumn, diveRateColumn);
        tableView.getColumns().forEach(col -> col.setSortable(false));
        ObservableList<Student> tableData = FXCollections.observableArrayList();
        tableData.add(host);
        tableData.addAll(potentialLunchMates);
        tableView.setItems(tableData);

        Label note = new Label("Note: First row of record is belongs to selected student");
        note.setStyle("-fx-font-weight: bold");

        gridPane.add(tableView, 0, 0);
        gridPane.add(note, 0, 1);

        gridPane.getChildren().forEach(child -> {
            GridPane.setHgrow(child, Priority.ALWAYS);
            GridPane.setVgrow(child, Priority.ALWAYS);
        });

        dialogPane.setContent(gridPane);

        Optional<ButtonType> result =  estLunchDialog.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;

    }

    private static boolean arrangeLunchSchedule(Sociograph sociograph) {
        // Add all the satisfied mate to the timeslot by considering the number of ppl currently in the slot
        for (Student mate : potentialLunchMates) {
            for (int i = computeNthMinute(host.getAvgLunchStart(), mate.getAvgLunchStart());
                 i < computeNthMinute(host.getAvgLunchStart(), mate.getLunchEnd());
                 i++) {
                if (i < timeslot.length && timeslot[i].size() < 3) {
                    timeslot[i].add(mate.getName());
                    if (!actualLunchMates.contains(mate)) {
                        actualLunchMates.add(mate);
                    }
                }
            }
        }

        Map<LocalTime, List<String>> scheduleContainer = new LinkedHashMap<>();
        LocalTime time = host.getAvgLunchStart();
        for (int i = 0; i < timeslot.length; i++) {
            // Check if timeslot[i] contains same thing as previous
            // If same, do nothing
            // If different, track down new time and new people
            if (!isContainSamePplAsPrevious(timeslot, i)) {
                scheduleContainer.put(time, timeslot[i]);
            }
            time = time.plusMinutes(1);
        }

        // TODO: Display the schedule in table form
        Alert lunchScheduleDialog = new Alert(Alert.AlertType.CONFIRMATION);
        canvasRef.setDefaultDialogConfig(lunchScheduleDialog);
        lunchScheduleDialog.setHeaderText("Lunch schedule of " + host.getName() + "\nDo you want to have lunch with all of them?");
        lunchScheduleDialog.getDialogPane().setPrefHeight(300);
        lunchScheduleDialog.getDialogPane().setPrefWidth(350);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-fit-to-width: true");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        List<LocalTime> listOfTime = new ArrayList<>(scheduleContainer.keySet());
        List<Label> timeLabels = new ArrayList<>();
        List<TextField> peopleLabels = new ArrayList<>();
        for (int i = 0; i < listOfTime.size(); i++) {
            if (i != listOfTime.size() - 1) {
                if (listOfTime.get(i).equals(listOfTime.get(i + 1).minusMinutes(1))) {
                    timeLabels.add(new Label(listOfTime.get(i) + ""));
                } else {
                    timeLabels.add(new Label(listOfTime.get(i) + " to " + listOfTime.get(i + 1).minusMinutes(1)));
                }
            } else {
                if (listOfTime.get(i).equals(host.getLunchEnd().minusMinutes(1))) {
                    timeLabels.add(new Label(listOfTime.get(i) + ""));
                } else {
                    timeLabels.add(new Label(listOfTime.get(i) + " to " + host.getLunchEnd().minusMinutes(1)));
                }
            }
            String contentTF = scheduleContainer.get(listOfTime.get(i)).toString().substring(1, scheduleContainer.get(listOfTime.get(i)).toString().length() - 1);
            if (contentTF.equals("")) {
                contentTF = "Empty";
            }
            TextField textField = new TextField(contentTF);
            textField.setEditable(false);
            peopleLabels.add(textField);
        }

        Label label1 = new Label("Time");
        Label label2 = new Label("People to have lunch with");
        label1.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
        label2.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
        gridPane.add(label1, 0, 0);
        gridPane.add(label2, 1, 0);
        for (int i = 0; i < timeLabels.size(); i++) {
            gridPane.add(timeLabels.get(i), 0, i + 1);
            gridPane.add(peopleLabels.get(i), 1, i + 1);
        }

        gridPane.getChildren().forEach(child -> GridPane.setHgrow(child, Priority.ALWAYS));

        gridPane.setAlignment(Pos.CENTER);
        scrollPane.setContent(gridPane);
        lunchScheduleDialog.getDialogPane().setContent(scrollPane);

        Optional<ButtonType> result = lunchScheduleDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    private static void startLunch(Sociograph sociograph) {
        // Add edge (rep point) to the host after having lunch with those ppl
        FillTransition ftHost = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(host.getName()), Color.BLACK, Color.YELLOW);
        st.getChildren().add(ftHost);

        PauseTransition pt1 = new PauseTransition(Duration.millis(1000));
        st.getChildren().add(pt1);

        for (Student actualMate : actualLunchMates) {
            GraphSimulationController.EdgeFX newOrExistingEdge;
            if (sociograph.hasDirectedEdge(host.getName(), actualMate.getName())) {
                double newRep = sociograph.getSrcRepRelativeToAdj(host.getName(), actualMate.getName()) + 1;
                sociograph.setSrcRepRelativeToAdj(host.getName(), actualMate.getName(), newRep);
                canvasRef.changeSrcRepRelativeToAdjFX(host.getName(), actualMate.getName(), newRep);

                newOrExistingEdge = canvasRef.getEdgeFX(host.getName(), actualMate.getName());

                // A little fade effect for existing edge
                FadeTransition ft = new FadeTransition(Duration.millis(500), newOrExistingEdge);
                ft.setFromValue(5);
                ft.setToValue(10);
                st.getChildren().add(ft);
            } else {
                newOrExistingEdge = canvasRef.createNewDirectedEdgeFX(canvasRef.getVertexFX(host.getName()), canvasRef.getVertexFX(actualMate.getName()), 1 + "", Relationship.NONE);

                // Fading in effect for new edge
                FadeTransition ft = new FadeTransition(Duration.millis(500), newOrExistingEdge);
                ft.setFromValue(0);
                ft.setToValue(10);
                st.getChildren().add(ft);
            }
            pt1 = new PauseTransition(Duration.millis(1000));
            st.getChildren().add(pt1);

            FillTransition fillTransition = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(actualMate.getName()), Color.BLACK, Color.YELLOW);
            st.getChildren().add(fillTransition);

            PauseTransition pt2 = new PauseTransition(Duration.millis(1000));
            st.getChildren().add(pt2);

            totalRepObtained++;
        }

    }

    private static int computeNthMinute(LocalTime hostTime, LocalTime mateTime) {
        if (mateTime.isBefore(hostTime)) {
            return 0;
        }
        LocalTime resultDuration = mateTime.minusSeconds(hostTime.toSecondOfDay());
        int minute = resultDuration.getMinute();
        int hour = resultDuration.getHour() * 60;
        int nthMinute = minute + hour;
        return nthMinute;
    }

    private static boolean isContainSamePplAsPrevious(ArrayList<String>[] timeslot, int currentIndex) {
        if (currentIndex == 0) {
            return false;
        }
        if (timeslot[currentIndex].containsAll(timeslot[currentIndex - 1]) && timeslot[currentIndex].size() == timeslot[currentIndex - 1].size()) {
            return true;
        }
        return false;
    }

}
