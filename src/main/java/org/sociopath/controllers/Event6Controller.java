package org.sociopath.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.*;

public class Event6Controller {
    private static GraphSimulationController canvasRef = MainPageController.canvasRef;
    private static String title = "Event 6 - Friendship";
    private static Graph graph;

    public static void event6Prompt(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        canvasRef.setDefaultDialogConfig(alert);

        String descriptionTxt = "University is not only for you to get a degree but is also a place for you to meet new people, and " +
                "some of the friendships even last a lifetime! There are many ways for us to meet new friends, one " +
                "of the ways is we will make new friends out of our friends’ friends through some opportunities. For " +
                "example, A and B are friends and B and C are friends, A will eventually meet C! (Note that " +
                "friendship is undirected which means A - B is equal to B - A, A - B - C is equal to C - B - A).\n\n" +
                "You are given a list of an integer n, followed by n lines of existing friendships between 2 " +
                "individuals. You need to find the total number of unique ways the friendship can be formed. It is " +
                "guaranteed that the relationship between all individuals are connected (all individuals will " +
                "eventually meet each other).";
        String headerTxt = "Description";

        Optional<ButtonType> result = canvasRef.showDescriptionDialog(title, headerTxt, descriptionTxt);
        if(result.isPresent() && result.get() == ButtonType.OK){
            int vertexNumbers = promptInputNumOfVertices();
            if (vertexNumbers >= 3) {
                if (promptInputExistingFriendship(vertexNumbers)) {
                    event6Execution(vertexNumbers);
                }
            }
        }
    }

    private static int promptInputNumOfVertices() {
        TextInputDialog enterVertexNumDL = new TextInputDialog();
        canvasRef.setDefaultDialogConfig(enterVertexNumDL);
        enterVertexNumDL.setTitle(title);
        enterVertexNumDL.setHeaderText("Enter number of vertices");

        DialogPane dialogPane = enterVertexNumDL.getDialogPane();

        TextField textField = enterVertexNumDL.getEditor();
        textField.setPromptText("vertices");

        dialogPane.setContentText("Number of vertices");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                    boolean isNumber;
                    int vertexNum = 0;
                    try{
                        vertexNum = Integer.parseInt(textField.getText());
                        isNumber = true;
                    } catch(NumberFormatException e){
                        isNumber = false;
                    }

                    return !isNumber || vertexNum < 3;
                }
                , textField.textProperty()));

        int vertexNumbers = 0;

        Optional<String> inputResult = enterVertexNumDL.showAndWait();

        if(inputResult.isPresent()){
            vertexNumbers = Integer.parseInt(textField.getText().trim());
        }
        return vertexNumbers;
    }

    private static boolean promptInputExistingFriendship(int vertexNumbers) {
        Alert enterExistingFriendshipDL = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.NEXT, ButtonType.CANCEL);
        canvasRef.setDefaultDialogConfig(enterExistingFriendshipDL);
        enterExistingFriendshipDL.setResizable(true);
        enterExistingFriendshipDL.setTitle(title);
        enterExistingFriendshipDL.setHeaderText("Enter existing friendship");
        enterExistingFriendshipDL.getDialogPane().setMinHeight(300);
        enterExistingFriendshipDL.getDialogPane().setPrefWidth(250);

        centerButtons(enterExistingFriendshipDL.getDialogPane());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-fit-to-width: true");

        GridPane outGridPane = new GridPane();
        outGridPane.setAlignment(Pos.CENTER);

        GridPane inGridPane = new GridPane();
        inGridPane.setAlignment(Pos.CENTER);
        inGridPane.setHgap(10);
        inGridPane.setVgap(10);

        Label existingFriendshipLabel = new Label("Existing friendship");
        existingFriendshipLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14");

        outGridPane.add(existingFriendshipLabel, 0, 0);

        List<TextField> leftTextFields = new ArrayList<>();
        List<TextField> rightTextFields = new ArrayList<>();

        for (int i = 0; i < vertexNumbers; i++) {
            TextField lTextField = new TextField();
            lTextField.setPromptText("left vertex");
            leftTextFields.add(lTextField);

            TextField rTextField = new TextField();
            rTextField.setPromptText("right vertex");
            rightTextFields.add(rTextField);

            inGridPane.add(lTextField, 0, i + 1);
            inGridPane.add(rTextField, 2, i + 1);
            inGridPane.add(new Label("-"), 1, i + 1);

            GridPane.setHgrow(lTextField, Priority.ALWAYS);
            GridPane.setHgrow(rTextField, Priority.ALWAYS);
        }

        outGridPane.add(inGridPane, 0, 1);
        GridPane.setHgrow(inGridPane, Priority.ALWAYS);

        scrollPane.setContent(outGridPane);
        enterExistingFriendshipDL.getDialogPane().setContent(scrollPane);

        Button nextButton = (Button) enterExistingFriendshipDL.getDialogPane().lookupButton(ButtonType.NEXT);

        BooleanBinding booleanBinding = leftTextFields.get(0).textProperty().isEmpty();
        for (int i = 1; i < leftTextFields.size(); i++) {
            booleanBinding = Bindings.or(booleanBinding, leftTextFields.get(i).textProperty().isEmpty());
        }
        for (int i = 0; i < rightTextFields.size(); i++) {
            booleanBinding = Bindings.or(booleanBinding, rightTextFields.get(i).textProperty().isEmpty());
        }
        nextButton.disableProperty().bind(booleanBinding);

        Optional<ButtonType> clickResult = enterExistingFriendshipDL.showAndWait();
        if (clickResult.isPresent() && clickResult.get() == ButtonType.NEXT) {
            graph = new Graph(vertexNumbers + 1);
            for (int i = 0; i < vertexNumbers; i++) {
                try {
                    int src = Integer.parseInt(leftTextFields.get(i).getText());
                    int dest = Integer.parseInt(rightTextFields.get(i).getText());
                    if (src > vertexNumbers || dest > vertexNumbers) {
                        throw new NumberFormatException();
                    }
                    graph.addEdge(src, dest);
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    canvasRef.setDefaultDialogConfig(alert);
                    alert.setContentText("All inputs must be in Integer!");
                    alert.showAndWait();
                    promptInputExistingFriendship(vertexNumbers);
                    return false;
                }
            }
            return true;
        }

        return false;

    }

    private static void event6Execution(int vertexNumbers) {

        System.out.println("You can form the following friendship (s): ");
        for(int i=1; i<=vertexNumbers; i++){
            for(int j=1; j<=vertexNumbers; j++){
                if(i!=j){
                    graph.printAllPath(i,j);
                }
            }
        }

        List<ArrayList<Integer>> allPaths = graph.clearPath();

        Alert information = new Alert(Alert.AlertType.INFORMATION);
        canvasRef.setDefaultDialogConfig(information);
        information.setResizable(true);
        information.setTitle(title);
        information.setHeaderText("You can form the following friendship (s)");
        information.getDialogPane().setPrefHeight(350);
        information.getDialogPane().setPrefWidth(280);

        centerButtons(information.getDialogPane());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-fit-to-width: true");

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        for (int i = 0; i < allPaths.size(); i++) {
            TextField tf = new TextField(allPaths.get(i).toString().substring(1, allPaths.get(i).toString().length() - 1));
            tf.setEditable(false);

            Label label = new Label((i + 1) + ".");
            label.setStyle("-fx-font-weight: bold");
            gridPane.add(label, 0, i);
            gridPane.add(tf, 1, i);

            GridPane.setHgrow(tf, Priority.ALWAYS);
        }
        scrollPane.setContent(gridPane);
        information.getDialogPane().setContent(scrollPane);

        information.showAndWait();
    }

    private static void centerButtons(DialogPane dialogPane) {
        Region spacer = new Region();
        ButtonBar.setButtonData(spacer, ButtonBar.ButtonData.BIG_GAP);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        dialogPane.applyCss();
        HBox hboxDialogPane = (HBox) dialogPane.lookup(".container");
        hboxDialogPane.getChildren().add(spacer);
    }
}

class Graph {

    // No. of vertices in graph
    private int v;

    // Adjacency List
    private ArrayList<Integer>[] adjList;
    private List<ArrayList<Integer>> path = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> tempPath = new ArrayList<>();

    public Graph(int vertices){
        // initialise vertex count
        this.v = vertices;

        // initialise adjacency list
        initAdjList();
    }

    // Utility method to initialise adjacency list
    @SuppressWarnings("unchecked")
    public void initAdjList(){
        adjList = new ArrayList[v];
        for(int i=0; i<v; i++){
            adjList[i] = new ArrayList<>();
        }
    }

    public void addEdge(int u, int v){
        // Add v to u's list
        adjList[u].add(v);
        adjList[v].add(u);
    }

    // Print all paths
    public void printAllPath(int s, int d){
        boolean[] isVisited = new boolean[v];
        ArrayList<Integer> pathList = new ArrayList<>();

        // add source to path[]
        pathList.add(s);

        // Call recursive utility
        printAllPathsUtil(s,d,isVisited,pathList);
    }

    // A recursive function to print all path from 'u' to 'd'
    // isVisited[] keeps track of vertices in current path.
    // localPathList<> stores actual vertices in the current path
    private void printAllPathsUtil(Integer u, Integer d, boolean[] isVisited, List<Integer> localPathList){
        if(u.equals(d)){
            String temp1 = localPathList.toString();
            temp1 = temp1.substring(1,temp1.length()-1);
            String[] temp2 = temp1.split(", ");
            ArrayList<Integer> temp3 = new ArrayList<>();
            for(String i: temp2){
                Integer temp4 = Integer.parseInt(i);
                temp3.add(temp4);
            }
            path.add(temp3);
            tempPath.add(temp3);
            // if match found then no need to traverse more till depth
            return;
        }

        // Mark the current node
        isVisited[u] = true;

        // Recur for all the vertices adjacent to current vertex
        for(Integer i : adjList[u]){
            if(!isVisited[i]){
                // Store current node in path[]
                localPathList.add(i);
                printAllPathsUtil(i,d,isVisited,localPathList);
                // Remove current node in path[]
                localPathList.remove(i);
            }
        }
        // Mark the current node
        isVisited[u] = false;
    }

    public List<ArrayList<Integer>> clearPath(){
        for(int i=0; i<path.size(); i++){
            for(int j=0; j<path.size(); j++){
                if(i!=j){
                    int[] arr1 = new int[path.get(i).size()];
                    for(int k=0; k<path.get(i).size(); k++){
                        arr1[k] = path.get(i).get(k);
                    }
                    int[] arr2 = new int[path.get(j).size()];
                    int l=0;
                    for(int k=path.get(j).size()-1; k>=0; k--){
                        arr2[l] = path.get(j).get(k);
                        l++;
                    }
                    if(Arrays.equals(arr1,arr2)){
                        path.remove(j);
                    }
                }
            }
        }
        ArrayList<Integer> temp = new ArrayList<>();
        for(int i=0; i<path.size()-1; i++){
            for(int j=0; j<path.size()-1-i; j++){
                if(path.get(j).size()>path.get(j+1).size()){
                    temp = path.get(j);
                    path.remove(j);
                    path.add(j,path.get(j));
                    path.remove(j+1);
                    path.add(j+1,temp);
                }
            }
        }
//        String str = "";
//        for(int i=0; i<path.size(); i++){
//            str += (i+1) + ". " + path.get(i) + "\n";
//        }
//        return str;
        return path;
    }
}
