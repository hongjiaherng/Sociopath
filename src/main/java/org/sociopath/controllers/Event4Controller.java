package org.sociopath.controllers;

import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.util.*;

public class Event4Controller {

    private static GraphSimulationController canvasRef = MainPageController.canvasRef;
    private static SequentialTransition st = new SequentialTransition();
    private static String title = "Event 4 - Arranging books";
    private static int numOfBooks;
    private static Stack<Integer> stack1;
    private static Stack<Integer> stack2;
    private static int[] heights;


    public static void event4Prompt(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertexFX) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);

        if (selectedVertexFX == null) {
            alert.setContentText("Please select a student!");
            alert.show();
        } else if (sociograph.getSize() < 1) {
            alert.setContentText("This event needs at least 1 students in your graph!");
            alert.show();
        } else {
            String descriptionText = "To beat the boredom, you joined a volunteering program to help the UM library to arrange the books. The librarian has two special requests: (1) make sure the height of a row of books is in non-increasing order, and (2) you need to solve this using the stack data structure.\n" +
                    "\n" +
                    "There is only one row of books on the bookshelf. In this row of books, every book has different heights.\n" +
                    "\n" +
                    "For each round, you’ll move from left to right to pick out the book that did not meet the request. At the end of each round, you'll be on the right side of the bookshelf. You put the book(s) down because it’s heavy to carry.\n" +
                    "\n" +
                    "Then the next round starts, you start over again moving from left to right.\n" +
                    "\n" +
                    "Since the books are heavy, you plan to use a special way to meet the librarian’s request. While you’re moving from left to right, you will just check if a book is higher than the book on the left and take it out.\n" +
                    "\n" +
                    "You are given the heights of the books. Determine the number of rounds needed to make the height of the books in non-increasing order. Make your program accept the input and print the number of rounds needed for you to make the row of books in non-increasing order.";

            String headerText = "Description";
            Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, headerText, descriptionText);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                st.getChildren().clear();
                numOfBooks = 0;
                event4Execution(sociograph, selectedVertexFX);
            }
        }
    }

    private static void event4Execution(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex) {

        // Enter number of book
        numOfBooks = promptInputNumOfBooks();

        if (numOfBooks <= 0) {
            return;
        } else {
            // Enter height of books
            heights = promptInputHeightOfBooks();

            if (heights != null) {
                stack1 = new Stack<>();
                stack2 = new Stack<>();
                for (int height : heights) {
                    stack1.push(height);
                }

                // Start processing
                arrangeBooks(sociograph, selectedVertex);
            }
        }
    }

    private static int promptInputNumOfBooks() {

        TextInputDialog enterBookNum = new TextInputDialog();
        canvasRef.setDefaultDialogConfig(enterBookNum);
        enterBookNum.setResizable(true);
        enterBookNum.setTitle(title);
        enterBookNum.setHeaderText("Enter number of books available");
        enterBookNum.setContentText("Number of books");

        DialogPane dialogPane = enterBookNum.getDialogPane();

        TextField bookNumTF = enterBookNum.getEditor();

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                    boolean isNumber;
                    try {
                        int bookNum = Integer.parseInt(bookNumTF.getText().trim());
                        isNumber = true;
                    } catch (NumberFormatException e) {
                        isNumber = false;
                    }
                    return !isNumber || bookNumTF.getText().isEmpty() || Integer.parseInt(bookNumTF.getText().trim()) <= 1;
                }
                , bookNumTF.textProperty()));

        Optional<String> result = enterBookNum.showAndWait();

        if (result.isPresent()) {
            return Integer.parseInt(result.get().trim());
        }
        return 0;
    }

    private static int[] promptInputHeightOfBooks() {
        int[] heights = new int[numOfBooks];

        Alert enterBookHeight = new Alert(Alert.AlertType.CONFIRMATION);
        canvasRef.setDefaultDialogConfig(enterBookHeight);
        enterBookHeight.setResizable(true);
        enterBookHeight.setTitle(title);
        enterBookHeight.setHeaderText("Enter the height of books");
        enterBookHeight.getDialogPane().setMaxHeight(400);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-fit-to-width: true");

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        List<TextField> textFields = new ArrayList<>();

        for (int i = 0; i < numOfBooks; i++) {
            Label label = new Label("Book " + (i + 1));
            label.setStyle("-fx-font-weight: bold");

            TextField textField = new TextField();
            textField.setPromptText("Height " + (i + 1));
            textFields.add(textField);

            gridPane.add(label, 0, i);
            gridPane.add(textField, 1, i);

            GridPane.setHgrow(label, Priority.ALWAYS);
            GridPane.setHgrow(textField, Priority.ALWAYS);

        }
        scrollPane.setContent(gridPane);
        enterBookHeight.getDialogPane().setContent(scrollPane);

        Button okButton = (Button) enterBookHeight.getDialogPane().lookupButton(ButtonType.OK);

        BooleanBinding booleanBinding = textFields.get(0).textProperty().isEmpty();
        for (int i = 1; i < textFields.size(); i++) {
            booleanBinding = Bindings.or(booleanBinding, textFields.get(i).textProperty().isEmpty());
        }
        okButton.disableProperty().bind(booleanBinding);

        Optional<ButtonType> result = enterBookHeight.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (int i = 0; i < textFields.size(); i++) {
                try {
                    heights[i] = Integer.parseInt(textFields.get(i).getText().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Format error. Please enter only integer");
                }
            }
            return heights;
        } else {
            return null;
        }
    }

    private static void arrangeBooks(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertex) {
        List<String> result = new ArrayList<>();

        int a, b, round = 0;
        while(true){
            int stack1size = stack1.size();

            while(!stack1.isEmpty()) {
                a = stack1.pop();
                b = 0;

                if (!stack1.isEmpty()) {
                    b = stack1.peek();
                }

                if(a <= b || stack1.isEmpty()){
                    stack2.push(a);
                }
            }

            if(stack1size == stack2.size()){
                break;
            }

            // Push Back height of books into stack1
            while(!stack2.isEmpty()){
                stack1.push(stack2.pop());
            }
            System.out.println("Round " + (round + 1)  + ": " + stack1);
            result.add(stack1.toString());
            // increment round
            round++;
        }

        round = result.size();

        Alert displayResult = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.NEXT);
        canvasRef.setDefaultDialogConfig(displayResult);
        displayResult.setTitle(title);
        displayResult.setHeaderText("Rounds needed to arrange the book in descending order");
        displayResult.setResizable(true);
        displayResult.getDialogPane().setMinHeight(250);
        displayResult.getDialogPane().setMaxWidth(350);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-fit-to-width: true");

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label originalLabel = new Label("Original");
        originalLabel.setStyle("-fx-font-weight: bold");

        Label roundNeededLabel = new Label("Round needed");
        roundNeededLabel.setStyle("-fx-font-weight: bold");

        TextField originalTF = new TextField(Arrays.toString(heights).substring(1, Arrays.toString(heights).length() - 1));
        originalTF.setEditable(false);

        TextField roundNeededTF = new TextField(round + "");
        roundNeededTF.setEditable(false);

        GridPane.setHgrow(originalLabel, Priority.ALWAYS);
        GridPane.setHgrow(originalTF, Priority.ALWAYS);
        GridPane.setHgrow(roundNeededLabel, Priority.ALWAYS);
        GridPane.setHgrow(roundNeededTF, Priority.ALWAYS);

        gridPane.add(originalLabel, 0, 0);
        gridPane.add(originalTF, 1, 0);
        gridPane.add(roundNeededLabel, 0, 1);
        gridPane.add(roundNeededTF, 1, 1);

        for (int i = 0; i < result.size(); i++) {
            Label label = new Label("Round " + (i + 1));
            label.setStyle("-fx-font-weight: bold");

            TextField textField = new TextField(result.get(i).substring(1, result.get(i).length() - 1));
            textField.setEditable(false);

            gridPane.add(label, 0, i + 2);
            gridPane.add(textField, 1, i + 2);

            GridPane.setHgrow(label, Priority.ALWAYS);
            GridPane.setHgrow(textField, Priority.ALWAYS);

        }

        scrollPane.setContent(gridPane);
        displayResult.getDialogPane().setContent(scrollPane);

        Optional<ButtonType> clickResult = displayResult.showAndWait();

        if (clickResult.isPresent() && clickResult.get() == ButtonType.NEXT) {
            addRepRelativeToFriends(sociograph, selectedVertex);
        }
    }

    private static void addRepRelativeToFriends(Sociograph sociograph, GraphSimulationController.VertexFX selectedVertexFX) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        canvasRef.setDefaultDialogConfig(alert);
        alert.setTitle(title);
        alert.setHeaderText("Increase reputation points");

        String contentTxt = "All friends of " + selectedVertexFX.nameText.getText() + " realize that he/she is such a " +
                "helpful person after knowing he/she was is the one arranging books in library. They are going to increase " + selectedVertexFX.nameText.getText() +
                "'s reputation points relative to them, do you want to accept?";

        TextArea area = new TextArea(contentTxt);
        area.setWrapText(true);
        area.setEditable(false);
        alert.setResizable(true);
        alert.getDialogPane().setContent(area);

        Optional<ButtonType> clickResult = alert.showAndWait();

        if (clickResult.isPresent() && clickResult.get() == ButtonType.YES) {

            LinkedList<GraphSimulationController.VertexFX> friendFXs = new LinkedList<>();

            Student host = sociograph.getStudent(selectedVertexFX.nameText.getText());
            List<Student> friendsOrLover = new ArrayList<>(host.getFriends());
            if (host.getTheOtherHalf() != null) {
                friendsOrLover.add(host.getTheOtherHalf());
            }

            for (Student friend : friendsOrLover) {
                double newRep = sociograph.getSrcRepRelativeToAdj(host.getName(), friend.getName()) + 1;
                sociograph.setSrcRepRelativeToAdj(host.getName(), friend.getName(), newRep);
                canvasRef.changeSrcRepRelativeToAdjFX(host.getName(), friend.getName(), newRep);

                GraphSimulationController.VertexFX friendFX = canvasRef.getVertexFX(friend.getName());

                friendFXs.addLast(friendFX);

                FillTransition fillTransition = new FillTransition(Duration.millis(1000), friendFX, Color.BLACK, Color.CORAL);
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(1000));
                st.getChildren().add(fillTransition);
                st.getChildren().add(pauseTransition);

            }

            while (!friendFXs.isEmpty()) {

                FillTransition fillTransition = new FillTransition(Duration.millis(1000), friendFXs.removeFirst(), Color.CORAL, Color.BLACK);
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(1000));
                st.getChildren().add(fillTransition);
                st.getChildren().add(pauseTransition);
            }

            st.play();
        }
    }
}
