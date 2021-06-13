package org.sociopath.controllers;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.util.*;

public class Event5Controller {
    /**
     * 1. Prompt to enter crush
     * 2. Randomly pick a stranger
     * 3. If no path, end, else continue
     * 4. Day 1
     *      - Prompt that stranger will start spreading secret tmr
     *      - You can convince a person
     * 5. Day n
     *      - Prompt that stranger told person x your secret
     *      - You can convince a person
     *
     */
    private static Student you;
    private static Student crush;
    private static Student stranger;

    private static List<List<String>> allPaths;
    private static List<List<String>> pplKnewSecret;
    private static Set<String> convincedPeople;
    private static LinkedHashSet<String> spreadPeople;

    private static GraphSimulationController canvasRef = MainPageController.canvasRef;
    private static Random rd = new Random();
    private static Scanner sc = new Scanner(System.in);
    private static SequentialTransition st = new SequentialTransition();
    private static boolean reachCrush = false;
    private static boolean close = false;

    public static void event5Prompt(Sociograph sociograph, GraphSimulationController.VertexFX youVertexFX) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);

        if (youVertexFX == null) {
            alert.setContentText("Please select a student!");
            alert.show();
        } else if (sociograph.getSize() < 3) {
            alert.setContentText("This event needs at least 3 students in your graph!");
            alert.show();
        } else {
            String descriptionTxt = "While you join the volunteering program, there’s this guy/gal. He/she is really " +
                    "cute and talented.You haven’t really spoken to him/her, but the minute you saw him/her you got a " +
                    "crush on him/her. You can’t stop thinking about your crush. \n\n" +
                    "Sadly, there will be rumors out of nowhere from one of the strangers. By the nature of chit-chatting, " +
                    "you know it will be a disaster if your crush knows about the false allegation.\n\n" +
                    "So, who do you have to convince to clarify the rumor about you to prevent it from getting to your crush? " +
                    "The rumor will start in the stranger’s cluster and your crush is in another cluster. You might identify " +
                    "someone connected between these 2 clusters. Then, you can convince him/her on the rumor so that your crush " +
                    "won’t hear the rumor. If there is more than one person that needs to be convinced to prevent the rumor from " +
                    "spreading, then you can’t do it because you can only convince one person per day.";

            String title = "Event 5 - Meet your crush";
            String headerText = "Description";
            Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, headerText, descriptionTxt);

            if (result.isPresent() && result.get() == ButtonType.OK) {

                you = sociograph.getStudent(youVertexFX.nameText.getText());

                TextInputDialog enterCrushDL = new TextInputDialog();
                canvasRef.setDefaultDialogConfig(enterCrushDL);
                enterCrushDL.setTitle("Event 5 - Meet your crush");
                enterCrushDL.setHeaderText("Enter crush name");

                DialogPane dialogPane = enterCrushDL.getDialogPane();

                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(20, 10, 30, 10));

                TextField crushNameTF = new TextField();
                crushNameTF.setPromptText("Crush's name");
                crushNameTF.setPrefWidth(100);

                TextField repTF = new TextField();
                repTF.setPromptText("Crush's rep");
                repTF.setPrefWidth(100);

                gridPane.add(new Label("Crush's name"), 0, 0);
                gridPane.add(crushNameTF, 1, 0);
                gridPane.add(new Label("Crush's rep pts"), 0, 1);
                gridPane.add(repTF, 1, 1);

                dialogPane.setContent(gridPane);

                Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

                okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                            boolean isNumber;
                            try {
                                double rep = Double.parseDouble(repTF.getText());
                                isNumber = true;
                            } catch (NumberFormatException e) {
                                isNumber = false;
                            }
                            return !isNumber ||
                                    !sociograph.hasVertex(crushNameTF.getText()) ||
                                    crushNameTF.getText().equals(you.getName());
                        }, repTF.textProperty(), crushNameTF.textProperty()
                ));

                Optional<String> enterCrushResult = enterCrushDL.showAndWait();

                String crushName = null;
                double rep = 0;

                if (enterCrushResult.isPresent()) {
                    crushName = crushNameTF.getText().trim();
                    rep = Double.parseDouble(repTF.getText().trim());
                }

                if (crushName != null && rep != 0) {

                    // If there's existing edge between you and crush delete it
                    if (sociograph.hasDirectedEdge(you.getName(), crushName) || sociograph.hasDirectedEdge(crushName, you.getName())) {
                        canvasRef.deleteEdgeFXWithoutPrompt(canvasRef.getEdgeFX(you.getName(), crushName));
                    }

                    // Create a new directed crush edge
                    GraphSimulationController.EdgeFX newEdge = canvasRef.createNewDirectedEdgeFX(canvasRef.getVertexFX(crushName), youVertexFX, rep + "", Relationship.ADMIRED_BY);
                    crush = sociograph.getStudent(crushName);

                    // Get the stranger object randomly
                    ArrayList<Student> possibleStrangers = new ArrayList<>();
                    for (Student student : sociograph.getAllStudents()) {
                        if (!(student.getName().equals(you.getName()) || student.getName().equals(crush.getName()))) {
                            possibleStrangers.add(student);
                        }
                    }
                    stranger = possibleStrangers.get(rd.nextInt(possibleStrangers.size()));

                    System.out.println("That stranger: " + stranger.getName());

                    event5Execution(sociograph);


                    if (close) {
                        return;
                    }
                    System.out.println(spreadPeople);

                    st.getChildren().clear();
                    FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), newEdge);
                    fadeTransition.setFromValue(0);
                    fadeTransition.setToValue(10);
                    st.getChildren().add(fadeTransition);
                    st.getChildren().add(crushTransition(crushName));
                    st.getChildren().add(pause(1000));
                    st.getChildren().add(spreadedTransition(stranger.getName()));
                    st.getChildren().add(pause(1000));

                    Iterator<String> itr = spreadPeople.iterator();

                    //Traversing or Iterating
                    while (itr.hasNext()){
                        String person = itr.next();
                        st.getChildren().add(spreadedTransition(person));
                        st.getChildren().add(pause(1000));
                    }

                    if (reachCrush) {
                        FillTransition lastT = new FillTransition(Duration.millis(1000), canvasRef.getVertexFX(crush.getName()), Color.rgb(174, 0, 255), Color.YELLOW);
                        lastT.setCycleCount(5);
                        st.getChildren().add(lastT);
                        st.getChildren().add(pause(1000));

                    }

                    st.setOnFinished(event -> {
                        canvasRef.allCircles.forEach(circle -> {
                            FillTransition ft = new FillTransition(Duration.millis(2000), circle, Color.BLACK, Color.BLACK);
                            ft.play();
                        });
                    });
                    st.onFinishedProperty();
                    st.play();

                }
            }
        }
    }

    private static void event5Execution(Sociograph sociograph) {
        // Find all the path between stranger and crush
        reachCrush = false;
        spreadPeople = new LinkedHashSet<>();
        allPaths = sociograph.dfTraversal(stranger.getName(), crush.getName());
        allPaths.removeIf(path -> path.contains(you.getName()));
        System.out.println(allPaths + "\n");

        // Rumors can't spread if the path is empty
        // Start spreading if it's not empty
        if (!allPaths.isEmpty()) {

            convincedPeople = new HashSet<>();
            // Declare pplKnewSecret to keep the ppl who knows the secret according to their respective path
            pplKnewSecret = new ArrayList<>();
            for (int i = 0; i < allPaths.size(); i++) {
                pplKnewSecret.add(new ArrayList<>());
            }
            // Add stranger to every path of pplKnewSecret first
            pplKnewSecret.forEach(list -> list.add(stranger.getName()));

            // Remove stranger from every path to mark as visited
            allPaths.forEach(list -> list.remove(stranger.getName()));

            int day = 1;

            // Spreading process start, this process will stop if crush know the rumor or you stop the rumor successfully
            for (boolean terminate = false; !terminate; day++) {

                String pplToConvince = showDayNDialog(day);

                // If null, means reach crush
                if (pplToConvince == null) {
                    terminate = true;
                    continue;
                } else {
                    boolean allChainStopped = convince(pplToConvince);
                    // Terminate loop if all rumor chains are broken
                    if (allChainStopped) {
                        terminate = true;
                        continue;
                    }
                }
            }

        } else {    // Exit if the path is empty
            // TODO: Dialog to tell the path is empty, exit
            Alert alert = new Alert(Alert.AlertType.ERROR);
            canvasRef.setDefaultDialogConfig(alert);
            alert.setTitle("Event 5 - Meet your crush");
            alert.setHeaderText("No hurt to you");
            alert.setContentText("The stranger is " + stranger.getName() + ". No path from " + stranger.getName() + " to " + crush.getName() + ". Your crush won't know that you like her.");
            alert.show();
            close = true;
        }
    }

    private static String showDayNDialog(int day) {

        TextInputDialog dayNDialog = new TextInputDialog();
        canvasRef.setDefaultDialogConfig(dayNDialog);
        dayNDialog.setTitle("Event 5 - Meet your crush");
        dayNDialog.setHeaderText("Day " + day);

        dayNDialog.setResizable(true);

        DialogPane dialogPane = dayNDialog.getDialogPane();
        dialogPane.setPrefWidth(400);
        dialogPane.setMinHeight(180);

        GridPane gridPane = new GridPane();
        gridPane.setStyle("-fx-fit-to-width: true");
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        GridPane inputGridPane = new GridPane();
        inputGridPane.setHgap(10);
        inputGridPane.setVgap(10);
        GridPane.setHgrow(inputGridPane, Priority.ALWAYS);

        TextField pplToConvinceTF = new TextField();
        pplToConvinceTF.setPromptText("name");
        GridPane.setHgrow(pplToConvinceTF, Priority.ALWAYS);

        // If day 1, spreading process not started yet
        if (day == 1) {
            Label day1Label = new Label("Stranger " + stranger.getName() + " is going to start spreading your secret tomorrow! Get ready!");
            day1Label.setStyle("-fx-font-weight: bold");
            day1Label.setWrapText(true);

            Label whoToConvince = new Label("Who to convince");
            whoToConvince.setPrefWidth(200);
            inputGridPane.add(whoToConvince, 0, 0);
            inputGridPane.add(pplToConvinceTF, 1, 0);

            gridPane.add(day1Label, 0, 0);
            gridPane.add(inputGridPane, 0, 1);

        } else {
            // After day 1, stranger starts to spread the rumors to his neighbouring friends, his friends also will spread
            // to his own friends in the consecutive days

            // Return true if the rumor reaches crush, else false
            List<String> spreadedMessages = spreadRumor();

            // Terminate loop if reachCrush
            if (spreadedMessages == null) {
                return null;
            }

            Label pplToldLabel = new Label(stranger.getName() + " told them your secret today");
            pplToldLabel.setPrefWidth(200);
            inputGridPane.add(pplToldLabel, 0, 0);

            int i;
            for (i = 0; i < spreadedMessages.size(); i++) {
                TextField msgTF = new TextField(spreadedMessages.get(i));
                GridPane.setHgrow(msgTF, Priority.ALWAYS);
                msgTF.setEditable(false);
                msgTF.setAlignment(Pos.CENTER);
                inputGridPane.add(msgTF, 1, i);
            }

            Label whoToConvince = new Label("Who to convince");
            whoToConvince.setPrefWidth(200);
            inputGridPane.add(whoToConvince, 0, i);
            inputGridPane.add(pplToConvinceTF, 1, i);
            gridPane.add(inputGridPane, 0, 0);

        }

        dialogPane.setContent(gridPane);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

        okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                pplToConvinceTF.getText().isEmpty(), pplToConvinceTF.textProperty()
        ));

        Optional<String> result = dayNDialog.showAndWait();

        String pplToConvince = null;
        if (result.isPresent()) {
            pplToConvince = pplToConvinceTF.getText();
        } else {
            close = true;
        }
        return pplToConvince;
    }

    private static boolean convince(String pplToConvince) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        canvasRef.setDefaultDialogConfig(alert);
        alert.setTitle("Event 5 - Meet your crush");
        alert.getDialogPane().setMaxWidth(400);

        alert.setResizable(true);

        Label content = new Label();
        content.setWrapText(true);

        HashSet<String> pplKnewSecretSet = new HashSet<>();
        pplKnewSecret.forEach(list -> pplKnewSecretSet.addAll(list));

        if (allPaths.stream().noneMatch(path -> path.contains(pplToConvince))) {
            // Dialog convince wrong person
            alert.setHeaderText("Convince failed");
            content.setText("You convince the wrong person!");
            alert.getDialogPane().setPrefHeight(150);
        } else if (pplToConvince.equals(crush.getName())) {
            // Dialog cant convince crush directly
            alert.setHeaderText("Convince failed");
            content.setText("You can't convince your crush directly!");
            alert.getDialogPane().setPrefHeight(150);
        } else if (pplKnewSecretSet.contains(pplToConvince)) {
            // Dialog cant convince people who knew your secret
            alert.setHeaderText("Convince failed");
            content.setText("You can't convince the person who already know your secret!");
            alert.getDialogPane().setPrefHeight(150);
        } else if (convincedPeople.contains(pplToConvince)) {
            System.out.println("here");
            content.setText("You already convinced " + pplToConvince + ", no use to convince again!");
            alert.getDialogPane().setPrefHeight(150);
        } else {
            System.out.println("here2");
            boolean someoneConvinced = false;
            List<String> allBrokenChains = new ArrayList<>();

            for (List<String> path : allPaths) {
                if (path.contains("stop")) continue;

                if (path.contains(pplToConvince)) {
                    convincedPeople.add(pplToConvince);
                    someoneConvinced = true;
                    path.add("stop");

                    StringBuilder chain = new StringBuilder("[");
                    path.forEach(v -> {
                        if (v.equals("stop"))
                            chain.append("]");
                        else if (v.equals(crush.getName()))
                            chain.append(v);
                        else
                            chain.append(v).append(", ");
                    });
                    allBrokenChains.add(chain.toString());
                }
            }

            // Dialog to tell convince success, break which chain
            if (someoneConvinced) {

                // TODO: Change from black to red for convincing a person
                System.out.print("You convinced the right person, " + pplToConvince + "! ");
                StringBuilder allBrokenChainsSB = new StringBuilder();
                allBrokenChains.forEach(chain -> allBrokenChainsSB.append(chain).append(", "));
                String allBrokenChainStr = allBrokenChainsSB.substring(0, allBrokenChainsSB.length() - 2);

                if (allPaths.stream().allMatch(v -> v.contains("stop"))) {

                    alert.setHeaderText("Mission Success");
                    content.setText("You convinced the right person, " + pplToConvince + "! You've break all the rumors chain.\n" +
                            "Chains broken this round: " + allBrokenChainStr);

                    // Dialog to tell convince success, all chains broken
                } else {
                    alert.setHeaderText("Convince success");
                    content.setText("You convinced the right person, " + pplToConvince + "! But there's still someone to convince.\n" +
                            "Chains broken this round: " + allBrokenChainStr);
                    // Dialog to tell convince success, but still some more to go
                }
                alert.getDialogPane().setPrefHeight(250);
            }
        }
        alert.getDialogPane().setContent(content);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            return allPaths.stream().allMatch(path -> path.contains("stop"));
        } else {
            // Return true to stop directly
            close = true;
            return true;
        }
    }

    private static List<String> spreadRumor() {
        List<String> textFieldMessages = new ArrayList<>();
        Set<String> spreadedPerson = new HashSet<>();
        boolean isReachCrush = false;
        for (int i = 0; i < allPaths.size(); i++) {
            if (allPaths.get(i).contains("stop")) {
                continue;
            }
            String newlySpread = allPaths.get(i).remove(0);
            pplKnewSecret.get(i).add(newlySpread);
            if (newlySpread.equals(crush.getName())) {

                // stop here
                isReachCrush = true;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                canvasRef.setDefaultDialogConfig(alert);
                alert.setTitle("Event 5 - Meet your crush");
                alert.setHeaderText("Mission Failed");
                alert.setResizable(true);
                alert.getDialogPane().setPrefHeight(200);
                alert.getDialogPane().setPrefWidth(280);
                Label content = new Label(pplKnewSecret.get(i).get(pplKnewSecret.get(i).size() - 2) +
                        " told to your crush that you like her/him. Unfortunately, " + crush.getName() +
                        " hates you so much, you become " + crush.getName() + "'s enemy.");
                content.setWrapText(true);
                alert.getDialogPane().setContent(content);
                alert.showAndWait();

                canvasRef.deleteEdgeFXWithoutPrompt(canvasRef.getEdgeFX(crush.getName(), you.getName()));
                GraphSimulationController.EdgeFX newUndirectedEdge = canvasRef.createNewUndirectedEdgeFX(canvasRef.getVertexFX(crush.getName()), canvasRef.getVertexFX(you.getName()), -10 + "", -10 + "", Relationship.ENEMY);

                FadeTransition fadeTransition = new FadeTransition(Duration.millis(2000), newUndirectedEdge);
                fadeTransition.setFromValue(0);
                fadeTransition.setToValue(10);

                st.getChildren().add(fadeTransition);

                break;
            }

            // Make sure rumors don't go for the same person even tho 2 connecting to that person, all the spreaded person in one spread must be unique
            if (!spreadedPerson.contains(newlySpread)) {
                spreadPeople.add(newlySpread);
                spreadedPerson.add(newlySpread);
                textFieldMessages.add(pplKnewSecret.get(i).get(pplKnewSecret.get(i).size() - 2) + " told " + newlySpread + " your secret!");
            }
        }

        if (isReachCrush) {
            reachCrush = true;
            return null;
        }
        return textFieldMessages;
    }

    private static FillTransition crushTransition(String name) {
        FillTransition ft = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(name), Color.BLACK, Color.rgb(174, 0, 255));
        return ft;
    }

    private static FillTransition spreadedTransition(String name) {
        FillTransition ft = new FillTransition(Duration.millis(500), canvasRef.getVertexFX(name), Color.BLACK, Color.YELLOW);
        return ft;
    }

    private static PauseTransition pause(int time) {
        PauseTransition pt = new PauseTransition(Duration.millis(time));
        return pt;
    }


}
