package org.sociopath.controllers;

import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.sociopath.App;
import org.sociopath.dao.GraphDao;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;
import org.sociopath.utils.DBConnect;

import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.ogm.exception.ConnectionException;

import java.awt.*;
import java.net.URL;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

public class GraphSimulationController implements Initializable {
    private final Stage primaryStage = App.getPrimaryStage();

    public Group canvasGroup;
    public ToggleButton addStudentBtn;
    public ToggleButton addRelationBtn;
    public ToggleButton addRepBtn;
    public ToggleGroup toggleGroup;
    public Pane viewer;

    public boolean isEventRunning = false;

    private Sociograph sociograph = new Sociograph();                    
    public List<VertexFX> allCircles = new ArrayList<>();
    private VertexFX selectedVertex = null;
    private static boolean isSaved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (allCircles.size() < 2) {
            addRepBtn.setDisable(true);
            addRelationBtn.setDisable(true);
            addRepBtn.setSelected(false);
            addRelationBtn.setSelected(false);
        }
    }

    @FXML
    public void backToMenuHandler(ActionEvent actionEvent) {
        App.sceneManager.pop();
        primaryStage.setScene(App.sceneManager.peek());
    }

    @FXML
    public void canvasHandler(MouseEvent mouseEvent) {
        if (allCircles.size() < 2) {
            addRelationBtn.setDisable(true);
            addRepBtn.setDisable(true);
            addRepBtn.setSelected(false);
            addRelationBtn.setSelected(false);
        } else {
            addRelationBtn.setDisable(false);
            addRepBtn.setDisable(false);
        }
        if (!mouseEvent.getSource().equals(canvasGroup)) {
            if (addStudentBtn.isSelected() && mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED && mouseEvent.getButton() == MouseButton.PRIMARY) {
                TextInputDialog dialog = new TextInputDialog();
                setDefaultDialogConfig(dialog);
                dialog.getDialogPane().setPrefWidth(250);
                dialog.getDialogPane().setPrefHeight(100);
                dialog.setTitle("Create new student");
                dialog.setHeaderText("Enter student name");
                dialog.setContentText("Name");

                TextField textField = dialog.getEditor();
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

                okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        textField.getText().trim().isEmpty() ||
                                textField.getText().trim().contains(" ") ||
                                sociograph.hasVertex(textField.getText().trim())
                        , textField.textProperty()));

                String newStudentName = null;
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()) {
                    newStudentName = result.get().trim();
                }

                if (newStudentName == null) {
                    return;
                } else {
                    VertexFX circle = new VertexFX(mouseEvent.getX(), mouseEvent.getY(), 1.2, newStudentName);
                    circle.showVertex();
                }
            }
        }
    }

    public void saveGraphFX(ActionEvent actionEvent) {
        DBConnect.startCon();

        // the graph is empty, will not save
        if(sociograph.getAllStudents().isEmpty()) {
            Alert alertDialog = new Alert(Alert.AlertType.WARNING);
            setDefaultDialogConfig(alertDialog);
            alertDialog.setContentText("No vertex and edge can be saved!");
            alertDialog.show();
            return;
        }

        try {
            if (!isSaved) {
                GraphDao.deleteGraph();
            }

            GraphDao.saveGraph(sociograph);
            Alert alertDialog = new Alert(Alert.AlertType.WARNING);
            setDefaultDialogConfig(alertDialog);
            alertDialog.setContentText("Save Successfully!");
            alertDialog.show();
            isSaved = true;

        } catch(ClientException e){
            Alert alertDialog = new Alert(Alert.AlertType.WARNING);
            setDefaultDialogConfig(alertDialog);
            alertDialog.setContentText("Your username or password might have been type in incorrectly. Please try it again!\n You may want to restart the database!");
            alertDialog.show();
        } catch (ConnectionException e){
            Alert alertDialog = new Alert(Alert.AlertType.WARNING);
            setDefaultDialogConfig(alertDialog);
            alertDialog.setContentText("Error connecting to the database!");
            alertDialog.show();
        }


        DBConnect.closeCon();
    }

    public void loadGraphFX(ActionEvent actionEvent) {
        DBConnect.startCon();

        if(!sociograph.getAllStudents().isEmpty()){
            Alert alertDialog = new Alert(Alert.AlertType.WARNING);
            setDefaultDialogConfig(alertDialog);
            alertDialog.setContentText("Cannot load a graph, there are vertices (and edges) on the canvas.");
            alertDialog.show();
        }

        else{
            try{
                List<Student> allStudents = GraphDao.db_getGraph().getAllStudents();
                if(allStudents.isEmpty()){
                    Alert alertDialog = new Alert(Alert.AlertType.WARNING);
                    setDefaultDialogConfig(alertDialog);
                    alertDialog.setContentText("There is no graph in the database!");
                    alertDialog.show();
                }

                else{
                    Sociograph newSociograph = GraphDao.db_getGraph();
                    HashMap<String, Boolean> isCreated = new HashMap<>();

                    for(Student student : newSociograph.getAllStudents())
                        isCreated.put(student.getName(), false);

                    drawAllVertexAndEdge(newSociograph, isCreated);
                }
            } catch (ConnectionException e){
                Alert alertDialog = new Alert(Alert.AlertType.WARNING);
                setDefaultDialogConfig(alertDialog);
                alertDialog.setContentText("Error connecting to the database!");
                alertDialog.show();

            } catch (ClientException e){
                Alert alertDialog = new Alert(Alert.AlertType.WARNING);
                setDefaultDialogConfig(alertDialog);
                alertDialog.setContentText("Your username or password might have been type in incorrectly. Please try it again!\n You may want to restart the database!");
                alertDialog.show();
            }
        }

        DBConnect.closeCon();
    }

    public void studentInfoCard(VertexFX vertex) {
        Dialog<ButtonType> dialog = new Dialog<>();
        setDefaultDialogConfig(dialog);

        dialog.setTitle("Student information card");
        dialog.setHeaderText("Student " + vertex.nameText.getText());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.NEXT, ButtonType.PREVIOUS, ButtonType.CLOSE);

        Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.managedProperty().bind(closeButton.visibleProperty());
        closeButton.setVisible(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        Student student = sociograph.getStudent(vertex.nameText.getText());

        List<Object> textFieldList = convertStudentInfoToTextFields(student);
        List<String> labelNameList = getStudentInfoAllFieldNames(student);

        for (int i = 0, rowpos = 0; i < textFieldList.size(); i++) {
            grid.add(new Label(labelNameList.get(i)), 0, rowpos);
            if (labelNameList.get(i).equals("Reputations")) {
                @SuppressWarnings("unchecked")
                List<TextField> fields = (List<TextField>) textFieldList.get(i);
                if (fields.isEmpty()) {
                    TextField field = new TextField("-");
                    field.setEditable(false);
                    field.setMinWidth(200);
                    field.setStyle("-fx-font-weight: bold");
                    grid.add(field, 1, rowpos++);
                } else {
                    for (TextField field : fields) {
                        field.setEditable(false);
                        field.setMinWidth(200);
                        field.setStyle("-fx-font-weight: bold");
                        grid.add(field, 1, rowpos++);
                    }
                }
            } else {
                TextField field = (TextField) textFieldList.get(i);
                field.setEditable(false);
                field.setMinWidth(200);
                field.setStyle("-fx-font-weight: bold");
                grid.add(field, 1, rowpos++);
            }
        }

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.NEXT) {
            int firstVertexIndex = allCircles.indexOf(vertex);
            if (firstVertexIndex < allCircles.size() - 1) {
                firstVertexIndex++;
            } else {
                firstVertexIndex = 0;
            }
            studentInfoCard(allCircles.get(firstVertexIndex));
        } else if (result.isPresent() && result.get() == ButtonType.PREVIOUS) {
            int firstVertexIndex = allCircles.indexOf(vertex);
            if (firstVertexIndex > 0) {
                firstVertexIndex--;
            } else {
                firstVertexIndex = allCircles.size() - 1;
            }
            studentInfoCard(allCircles.get(firstVertexIndex));
        } else {
            return;
        }
    }

    public void deleteVertexFX(VertexFX vertex) {
        String contextStr = "Are you sure you want to delete vertex " + vertex.nameText.getText() + " ? All the underlying edges will be removed too.";
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, contextStr, ButtonType.YES, ButtonType.NO);
        confirmDialog.setHeaderText("Are you sure ?");
        setDefaultDialogConfig(confirmDialog);

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            System.out.println("Remove");
            allCircles.remove(vertex);
            sociograph.deleteVertex(vertex.nameText.getText());     // this will delete vertex and also connected edges
            canvasGroup.getChildren().remove(vertex.vertexHolder);

            // Delete edges that use this vertex as srcVertex
            List<Group> edgeFXHolders = new ArrayList<>(vertex.connectedEdges);
            System.out.println("Remove edges use this as source: " + canvasGroup.getChildren().removeAll(edgeFXHolders));

            // Delete the remaining edges that use this vertex as destVertex
            for (VertexFX vertexFX : allCircles) {
                List<EdgeFX> list = vertexFX.connectedEdges;
                list.removeIf(edge -> {
                    if (edge.endVertex.nameText.getText().equals(vertex.nameText.getText())) {
                        boolean removeresult = canvasGroup.getChildren().remove(edge);
                        System.out.println("Remove edges use this as destination: " + removeresult);
                        return removeresult;
                    }
                    return false;
                });
            }

            if (allCircles.size() < 2) {
                addRepBtn.setDisable(true);
                addRelationBtn.setDisable(true);
                addRepBtn.setSelected(false);
                addRelationBtn.setSelected(false);
            }

            // Make sure the selectedVertex is cleared, because this vertex is already deleted
            selectedVertex = null;
            System.out.println(canvasGroup.getChildren().size());
        } else {
            System.out.println("return");
            return;
        }
    }

    public void changeVertexFXName(VertexFX vertex) {
        TextInputDialog dialog = new TextInputDialog();
        setDefaultDialogConfig(dialog);
        dialog.getDialogPane().setPrefWidth(250);
        dialog.getDialogPane().setPrefHeight(100);
        dialog.setTitle("Change student name");
        dialog.setHeaderText("Enter new name");
        dialog.setContentText("Name");

        TextField textField = dialog.getEditor();
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            textField.getText().trim().isEmpty() ||
                    textField.getText().trim().contains(" ") ||
                    sociograph.hasVertex(textField.getText().trim())
                , textField.textProperty()
        ));

        String nameToChange = null;
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            nameToChange = result.get().trim();
        }

        if (nameToChange == null) {
            return;
        } else {
            sociograph.changeVertexName(vertex.nameText.getText(), nameToChange);
            allCircles.get(allCircles.indexOf(vertex)).setNameText(nameToChange);
            System.out.println(sociograph);
        }
    }

    public void deleteEdgeFX(EdgeFX edge) {
        String arrow = "";
        if (edge.isDirected) {
            arrow = " -> ";
        } else {
            arrow = " <-> ";
        }

        String contextStr = "Are you sure you want to delete this edge (" + edge.srcVertex.nameText.getText() + arrow + edge.endVertex.nameText.getText() + ") ?";
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, contextStr, ButtonType.YES, ButtonType.NO);
        setDefaultDialogConfig(confirmDialog);
        confirmDialog.setHeaderText("Are you sure ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {

            if (!edge.isDirected) {
                System.out.println(sociograph.removeEdge(edge.endVertex.nameText.getText(), edge.srcVertex.nameText.getText()));
            }
            System.out.println(sociograph.removeEdge(edge.srcVertex.nameText.getText(), edge.endVertex.nameText.getText()));
            System.out.println(allCircles.get(allCircles.indexOf(edge.endVertex)).connectedEdges.remove(edge));
            System.out.println(allCircles.get(allCircles.indexOf(edge.srcVertex)).connectedEdges.remove(edge));
            System.out.println(canvasGroup.getChildren().remove(edge));
            System.out.println(sociograph.getStudent(edge.endVertex.nameText.getText()));
            System.out.println(sociograph.getStudent(edge.srcVertex.nameText.getText()));
            System.out.println(sociograph);

        } else {
            return;
        }

    }

    public void changeRepOrRelationTypeFX(EdgeFX edge) {
        TextInputDialog dialog = new TextInputDialog();
        setDefaultDialogConfig(dialog);
        dialog.getDialogPane().setPrefWidth(350);
        dialog.setHeaderText("Enter new properties between student " + edge.srcVertex.nameText.getText() + " and " + edge.endVertex.nameText.getText());

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 10, 30, 10));

        DialogPane dialogPane = dialog.getDialogPane();

        if (edge.isDirected) {  // Directed edge pane
            dialogPane.setPrefHeight(200);
            dialog.setTitle("Modify directed edge's properties");

            TextField prevRepTF = new TextField(edge.srcRepText.getText());
            prevRepTF.setEditable(false);
            prevRepTF.setPrefWidth(150);
            TextField newRepTF = new TextField();
            newRepTF.setPromptText(edge.srcVertex.nameText.getText() + "'s rep pts");
            newRepTF.setPrefWidth(150);
            TextField prevEdgeRelTF = new TextField((edge.rel == Relationship.NONE) ? "No" : "Yes");
            prevEdgeRelTF.setEditable(false);
            prevEdgeRelTF.setPrefWidth(150);
            ComboBox<String> newEdgeRelCB = new ComboBox<>(FXCollections.observableArrayList("Yes", "No"));
            newEdgeRelCB.setValue(prevEdgeRelTF.getText());
            newEdgeRelCB.setPrefWidth(150);

            gridPane.add(new Label("Original reputation"), 0, 0);
            gridPane.add(prevRepTF, 1, 0);
            gridPane.add(new Label("Original does " + edge.endVertex.nameText.getText() + " like " + edge.srcVertex.nameText.getText() + " ?"), 0, 1);
            gridPane.add(prevEdgeRelTF, 1, 1);
            gridPane.add(new Label("New reputation"), 0, 2);
            gridPane.add(newRepTF, 1, 2);
            gridPane.add(new Label("For now, does " + edge.endVertex.nameText.getText() + " like " + edge.srcVertex.nameText.getText() + " ?"), 0, 3);
            gridPane.add(newEdgeRelCB, 1, 3);

            dialogPane.setContent(gridPane);

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                        boolean isNumber;
                        try {
                            double rep = Double.parseDouble(newRepTF.getText().trim());
                            isNumber = true;
                        } catch (NumberFormatException e) {
                            isNumber = false;
                        }
                        return !isNumber;
                    }
                    , newRepTF.textProperty()));

            String srcRepStr = null;
            Relationship newRel = null;
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                srcRepStr = newRepTF.getText().trim();
                newRel = (newEdgeRelCB.getValue().equals("Yes")) ? Relationship.ADMIRED_BY : Relationship.NONE;
            }

            if (srcRepStr != null) {
                // Change rep points
                edge.changeSrcRepText(srcRepStr);
                edge.changeRel(newRel);
                sociograph.setSrcRepRelativeToAdj(edge.srcVertex.nameText.getText(), edge.endVertex.nameText.getText(), Double.parseDouble(srcRepStr));
                sociograph.setDirectedRelationshipOnEdge(edge.srcVertex.nameText.getText(), edge.endVertex.nameText.getText(), newRel);
            }

        } else {    // Undirected edge pane
            dialog.setTitle("Modify undirected edge's properties");

            TextField prevSrcRepTF = new TextField(edge.srcRepText.getText());
            prevSrcRepTF.setEditable(false);
            prevSrcRepTF.setPrefWidth(150);
            TextField prevDestRepTF = new TextField(edge.endRepText.getText());
            prevDestRepTF.setEditable(false);
            prevDestRepTF.setPrefWidth(150);
            TextField prevRelTypeTF = new TextField(edge.rel + "");
            prevRelTypeTF.setEditable(false);
            prevRelTypeTF.setPrefWidth(150);

            TextField newSrcRepTF = new TextField();
            newSrcRepTF.setPromptText(edge.srcVertex.nameText.getText() + "'s rep pts");
            newSrcRepTF.setPrefWidth(150);
            TextField newDestRepTF = new TextField();
            newDestRepTF.setPromptText(edge.endVertex.nameText.getText() + "'s rep pts");
            newDestRepTF.setPrefWidth(150);
            ComboBox<Relationship> newRelTypeCB = new ComboBox<>(FXCollections.observableArrayList(Relationship.NONE, Relationship.FRIEND, Relationship.ENEMY, Relationship.THE_OTHER_HALF));
            newRelTypeCB.setValue(Relationship.NONE);
            newRelTypeCB.setPrefWidth(150);

            Label originalLabel = new Label("Original properties");
            originalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            gridPane.add(originalLabel, 0, 0);
            gridPane.add(new Label("Original " + edge.srcVertex.nameText.getText() + "'s reputation"), 0, 1);
            gridPane.add(new Label("Original " + edge.endVertex.nameText.getText() + "'s reputation"), 0, 2);
            gridPane.add(new Label("Original relationship"), 0, 3);

            Label newLabel = new Label("New properties");
            newLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            gridPane.add(newLabel, 0, 5);
            gridPane.add(new Label("New " + edge.srcVertex.nameText.getText() + "'s reputation"), 0, 6);
            gridPane.add(new Label("New " + edge.endVertex.nameText.getText() + "'s reputation"), 0, 7);
            gridPane.add(new Label("New relationship"), 0, 8);

            gridPane.add(prevSrcRepTF, 1, 1);
            gridPane.add(prevDestRepTF, 1, 2);
            gridPane.add(prevRelTypeTF, 1, 3);
            gridPane.add(newSrcRepTF, 1, 6);
            gridPane.add(newDestRepTF, 1, 7);
            gridPane.add(newRelTypeCB, 1, 8);

            dialogPane.setContent(gridPane);

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                        boolean isNumber;
                        try {
                            double rep = Double.parseDouble(newSrcRepTF.getText().trim());
                            rep = Double.parseDouble(newDestRepTF.getText().trim());
                            isNumber = true;
                        } catch (NumberFormatException e) {
                            isNumber = false;
                        }
                        return !isNumber || newRelTypeCB.getSelectionModel().isEmpty();
                    }
                    , newSrcRepTF.textProperty(), newDestRepTF.textProperty(), newRelTypeCB.buttonCellProperty()));

            String srcRepStr = null;
            String destRepStr = null;
            Relationship relType = null;
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                srcRepStr = newSrcRepTF.getText().trim();
                destRepStr = newDestRepTF.getText().trim();
                relType = newRelTypeCB.getValue();
            }

            if (srcRepStr != null && relType != null && destRepStr != null) {
                // Change rep points
                edge.changeSrcRepText(srcRepStr);
                edge.changeEndRepText(destRepStr);
                sociograph.setSrcRepRelativeToAdj(edge.srcVertex.nameText.getText(), edge.endVertex.nameText.getText(), Double.parseDouble(srcRepStr));
                sociograph.setSrcRepRelativeToAdj(edge.endVertex.nameText.getText(), edge.srcVertex.nameText.getText(), Double.parseDouble(destRepStr));

                // Change relation
                sociograph.setUndirectedRelationshipOnEdge(edge.srcVertex.nameText.getText(), edge.endVertex.nameText.getText(), relType);
                edge.changeRel(relType);
            }
        }
    }

    public void clearGraphFX(ActionEvent event) {
        isSaved = false;

        // Remove all vertices from sociograph
        this.sociograph.clear();

        List<Node> allNodesOnCanvas = new ArrayList<>();
        for (VertexFX vertex : allCircles) {
            allNodesOnCanvas.add(vertex.vertexHolder);
            allNodesOnCanvas.addAll(vertex.connectedEdges);     // Here might contains same edge (e.g.: A connect B with edge X, connectedEdges of A & B both contain edge X)
        }

        // Remove all VertexFX from allCircles
        this.allCircles.clear();

        // Remove all VertexFX's holder & EdgeFX's holder from canvasGroup
        this.canvasGroup.getChildren().removeAll(allNodesOnCanvas);

        // Clear the selectedVertex to make sure no vertex remained
        this.selectedVertex = null;

        // Disable add relation & add reputation button
        addRelationBtn.setDisable(true);
        addRepBtn.setDisable(true);
        addRepBtn.setSelected(false);
        addRelationBtn.setSelected(false);

        allNodesOnCanvas.clear();

    }

    EventHandler<MouseEvent> mouseHandler = event -> {

        // If any event is running, don't execute click event on vertex
        if (isEventRunning) {
            System.out.println("event is running, you can't do this");
            return;
        }

        VertexFX destVertexFX = (VertexFX) event.getSource();
        if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseButton.PRIMARY) {
            if (!destVertexFX.isSelected) {

                if (selectedVertex != null) {
                    VertexFX srcVertexFX = selectedVertex;
                    String srcVertexName = srcVertexFX.nameText.getText();
                    String destVertexName = destVertexFX.nameText.getText();

                    // Add directed edge to an existing connection (convert directed edge into undirected edge)
                    if (addRepBtn.isSelected() && sociograph.hasDirectedEdge(destVertexName, srcVertexName) &&
                            !sociograph.hasDirectedEdge(srcVertexName, destVertexName)) {

                        TextInputDialog dialog = new TextInputDialog();
                        setDefaultDialogConfig(dialog);

                        DialogPane dialogPane = dialog.getDialogPane();
                        dialogPane.setPrefHeight(300);
                        dialogPane.setPrefWidth(350);

                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10);
                        gridPane.setVgap(10);
                        gridPane.setPadding(new Insets(20, 10, 30, 10));

                        TextField srcRepTF = new TextField();
                        srcRepTF.setPromptText(srcVertexName + "'s rep pts");
                        srcRepTF.setPrefWidth(100);

                        TextField destRepTF = new TextField();
                        destRepTF.setText("" + sociograph.getSrcRepRelativeToAdj(destVertexName, srcVertexName));
                        destRepTF.setEditable(false);
                        destRepTF.setPrefWidth(100);

                        ComboBox relationCB = new ComboBox();
                        relationCB.setPrefWidth(100);

                        boolean isTryingToFormLove = false;     // A flag to tell which condition is entered

                        // If dest is admired by src, ask if dest also admire src, if yes, they form THE_OTHER_HALF relationship, else, they become enemy.
                        // Make sure both of them don't have a girlfriend / boyfriend when enter this
                        if (sociograph.isAdmiredBy(destVertexName, srcVertexName) &&
                                sociograph.getStudent(destVertexName).getTheOtherHalf() == null &&
                                sociograph.getStudent(srcVertexName).getTheOtherHalf() == null) {

                            isTryingToFormLove = true;

                            dialog.setTitle("Does " + destVertexName + " like " + srcVertexName + " back ?");
                            dialog.setHeaderText(srcVertexName + " is admiring " + destVertexName + ", if " + destVertexName + " likes " + srcVertexName + " back, they can be the other half of each other");

                            relationCB.setItems(FXCollections.observableArrayList("Love " + srcVertexName, "Hate " + srcVertexName));
                            relationCB.setValue("Love " + srcVertexName);

                        } else {        // If dest to src is a NONE relationship, we are going to make a new relationship among src and dest
                            isTryingToFormLove = false;

                            dialog.setTitle("Convert to undirected edge");
                            dialog.setHeaderText(srcVertexName + " and " + destVertexName + " can have a relation when they know each other, what do you want their relationship to be?");

                            relationCB.setItems(FXCollections.observableArrayList(Relationship.NONE, Relationship.FRIEND, Relationship.ENEMY));
                            relationCB.setValue(Relationship.NONE);

                        }

                        gridPane.add(new Label(srcVertexName + "'s rep relative to " + destVertexName), 0, 0);
                        gridPane.add(new Label(destVertexName + "'s rep relative to " + srcVertexName), 0, 1);
                        gridPane.add(new Label("Relationship type"), 0, 2);
                        gridPane.add(srcRepTF, 1, 0);
                        gridPane.add(destRepTF, 1, 1);
                        gridPane.add(relationCB, 1, 2);

                        dialogPane.setContent(gridPane);
                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                                    boolean isNumber;
                                    try {
                                        double rep = Double.parseDouble(srcRepTF.getText());
                                        isNumber = true;
                                    } catch (NumberFormatException e) {
                                        isNumber = false;
                                    }
                                    return !isNumber || relationCB.getSelectionModel().isEmpty();
                                }
                                , srcRepTF.textProperty(), relationCB.buttonCellProperty()));

                        String srcRepStr = null;
                        String destRepStr = null;
                        Relationship relType = null;
                        Optional<String> result = dialog.showAndWait();

                        if (result.isPresent()) {
                            srcRepStr = srcRepTF.getText();
                            destRepStr = destRepTF.getText();

                            if (isTryingToFormLove) {
                                relType = relationCB.getValue().equals("Love " + srcVertexName) ? Relationship.THE_OTHER_HALF : Relationship.ENEMY;
                            } else {
                                relType = (Relationship) relationCB.getValue();
                            }
                        }

                        // Delete the previous directed edge and add a new undirected edge
                        if (srcRepStr != null && destRepStr != null && relType != null) {
                            // Delete directed edge from dest to src in allEdges and canvasGroup
                            LinkedList<EdgeFX> destConnectedEdges = allCircles.get(allCircles.indexOf(destVertexFX)).connectedEdges;
                            LinkedList<EdgeFX> srcConnectedEdges = allCircles.get(allCircles.indexOf(srcVertexFX)).connectedEdges;

                            srcConnectedEdges.removeIf(edge ->
                                edge.srcVertex.nameText.getText().equals(destVertexName) &&
                                        edge.endVertex.nameText.getText().equals(srcVertexName)
                            );
                            destConnectedEdges.removeIf(edge -> {
                                    if (edge.srcVertex.nameText.getText().equals(destVertexName) &&
                                            edge.endVertex.nameText.getText().equals(srcVertexName)) {
                                        return canvasGroup.getChildren().remove(edge);
                                    }
                                    return false;
                            });

                            // Delete directed edge from dest to src in sociograph
                            sociograph.removeEdge(destVertexName, srcVertexName);

                            // create a new EdgeFX object to represent the new undirected edge
                            EdgeFX newUndirectedEdge = new EdgeFX(srcVertexFX, destVertexFX, srcRepStr, destRepStr, relType);
                            newUndirectedEdge.showEdge();
                        }

                    } else if (addRepBtn.isSelected() && !sociograph.hasDirectedEdge(srcVertexName, destVertexName)) {      // Add directed edge (rep point) from src to dest

                        TextInputDialog dialog = new TextInputDialog();
                        setDefaultDialogConfig(dialog);
                        dialog.getDialogPane().setPrefHeight(200);
                        dialog.getDialogPane().setPrefWidth(300);
                        dialog.setTitle("Add directed edge");
                        dialog.setHeaderText("Enter student " + srcVertexName + " reputation point relative to " + destVertexName);

                        DialogPane dialogPane = dialog.getDialogPane();

                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10);
                        gridPane.setVgap(10);
                        gridPane.setPadding(new Insets(20, 10, 30, 10));

                        TextField repTF = new TextField();
                        repTF.setPromptText(srcVertexName + "'s rep");
                        repTF.setPrefWidth(100);

                        ComboBox<String> relCB = new ComboBox<>(FXCollections.observableArrayList("No", "Yes"));
                        relCB.setValue("No");
                        relCB.setPrefWidth(100);

                        gridPane.add(new Label(srcVertexName + "'s rep relative to " + destVertexName), 0, 0);
                        gridPane.add(repTF, 1, 0);
                        gridPane.add(new Label("Does " + destVertexName + " like " + srcVertexName + " ?"), 0, 1);
                        gridPane.add(relCB, 1, 1);


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
                                    return !isNumber;
                                }, repTF.textProperty()
                        ));

                        String repStr = null;
                        Relationship rel = null;
                        Optional<String> result = dialog.showAndWait();

                        if (result.isPresent()) {
                            repStr = repTF.getText();
                            rel = relCB.getValue().equals("No") ? Relationship.NONE : Relationship.ADMIRED_BY;

                        }

                        if (repStr != null && rel != null) {
                            EdgeFX directedEdge = new EdgeFX(srcVertexFX, destVertexFX, repStr, rel);
                            directedEdge.showEdge();
                        }

                    } else if (addRelationBtn.isSelected() && !sociograph.hasDirectedEdge(srcVertexName, destVertexName) &&
                            !sociograph.hasDirectedEdge(destVertexName, srcVertexName)) {

                        TextInputDialog dialog = new TextInputDialog();
                        setDefaultDialogConfig(dialog);
                        dialog.getDialogPane().setPrefHeight(200);
                        dialog.getDialogPane().setPrefWidth(340);
                        dialog.setTitle("Add undirected edge");
                        dialog.setHeaderText("Enter details between student " + srcVertexName + " and " + destVertexName);

                        DialogPane dialogPane = dialog.getDialogPane();

                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10);
                        gridPane.setVgap(10);
                        gridPane.setPadding(new Insets(20, 10, 30, 10));

                        TextField srcRepTF = new TextField();
                        srcRepTF.setPromptText(srcVertexName + "'s rep pts");
                        srcRepTF.setPrefWidth(150);

                        TextField destRepTF = new TextField();
                        destRepTF.setPromptText(destVertexName + "'s rep pts");
                        destRepTF.setPrefWidth(150);

                        ComboBox<Relationship> relationCB = new ComboBox<>();
                        ObservableList<Relationship> relationships = FXCollections.observableArrayList(Relationship.NONE, Relationship.FRIEND, Relationship.ENEMY, Relationship.THE_OTHER_HALF);
                        if (sociograph.getStudent(srcVertexName).getTheOtherHalf() != null || sociograph.getStudent(destVertexName).getTheOtherHalf() != null) {
                            relationships.remove(Relationship.THE_OTHER_HALF);
                        }
                        relationCB.setItems(relationships);
                        relationCB.setValue(Relationship.NONE);
                        relationCB.setPrefWidth(150);

                        gridPane.add(new Label(srcVertexName + "'s rep relative to " + destVertexName), 0, 0);
                        gridPane.add(new Label(destVertexName + "'s rep relative to " + srcVertexName), 0, 1);
                        gridPane.add(new Label("Relationship type"), 0, 2);
                        gridPane.add(srcRepTF, 1, 0);
                        gridPane.add(destRepTF, 1, 1);
                        gridPane.add(relationCB, 1, 2);

                        dialogPane.setContent(gridPane);

                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                            boolean isNumber;
                            try {
                                double rep = Double.parseDouble(srcRepTF.getText());
                                rep = Double.parseDouble(destRepTF.getText());
                                isNumber = true;
                            } catch (NumberFormatException e) {
                                isNumber = false;
                            }
                            return !isNumber || relationCB.getSelectionModel().isEmpty();
                            }
                                , srcRepTF.textProperty(), destRepTF.textProperty(), relationCB.buttonCellProperty()));

                        String srcRepStr = null;
                        String destRepStr = null;
                        Relationship relType = null;
                        Optional<String> result = dialog.showAndWait();

                        if (result.isPresent()) {
                            srcRepStr = srcRepTF.getText();
                            destRepStr = destRepTF.getText();
                            relType = relationCB.getValue();
                        }

                        if (srcRepStr != null && destRepStr != null && relType != null) {
                            EdgeFX undirectedEdge = new EdgeFX(srcVertexFX, destVertexFX, srcRepStr, destRepStr, relType);
                            undirectedEdge.showEdge();
                        }

                    } else if (addRelationBtn.isSelected() || addRepBtn.isSelected()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        setDefaultDialogConfig(alert);
                        alert.setContentText(srcVertexName + " and " + destVertexName + " are already connected!");
                        alert.show();
                    }

                    selectedVertex.isSelected = false;
                    FillTransition ft1 = new FillTransition(Duration.millis(300), selectedVertex, Color.RED, Color.BLACK);
                    ft1.play();
                    selectedVertex = null;
                    return;
                }

                FillTransition ftSelect = new FillTransition(Duration.millis(300), destVertexFX, Color.BLACK, Color.RED);
                ftSelect.play();
                destVertexFX.isSelected = true;
                selectedVertex = destVertexFX;

            } else {
                FillTransition ftUnselect = new FillTransition(Duration.millis(300), destVertexFX, Color.RED, Color.BLACK);
                ftUnselect.play();
                destVertexFX.isSelected = false;
                selectedVertex = null;
            }
        } else if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.getButton() == MouseButton.PRIMARY) {
            destVertexFX.vertexHolder.setCursor(Cursor.CLOSED_HAND);
            FillTransition ftDrag = new FillTransition(Duration.millis(300), destVertexFX, Color.BLACK, Color.RED);
            ftDrag.play();

        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED && event.getButton() == MouseButton.PRIMARY) {
            destVertexFX.vertexHolder.setCursor(Cursor.DEFAULT);
            FillTransition ftDrag = new FillTransition(Duration.millis(300), destVertexFX, Color.RED, Color.BLACK);
            ftDrag.play();

        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && event.getButton() == MouseButton.PRIMARY) {
            destVertexFX.dragVertex((int) event.getX(), (int) event.getY());
        }
    };

    public void event1Handler(ActionEvent event) {
        if (isEventRunning) {
            return;
        }
//        markEventRunning();
        Event1Controller.event1prompt(sociograph, selectedVertex);
        // Event ended must be called inside the event method when it ends
    }

    public void event2Handler(ActionEvent event) {
        if (isEventRunning) {
            return;
        }

        markEventRunning();
        Event2Controller.event2Prompt(sociograph, selectedVertex);

        // Event ended must be called inside the event method when it ends
    }

    public void event3Handler(ActionEvent event) {
        if (isEventRunning) {
            return;
        }

        markEventRunning();
        Event3Controller.event3Prompt(sociograph, selectedVertex);

        // Event ended must be called inside the event method when it ends
    }

    public void event4Handler(ActionEvent event) {
        if (isEventRunning) {
            return;
        }

//        markEventRunning();
//        Event2Controller.event2Prompt(sociograph, selectedVertex);

        // Event ended must be called inside the event method when it ends
    }

    public void event5Handler(ActionEvent event) {
        if (isEventRunning) {
            return;
        }
        Event5Controller.event5Prompt(sociograph, selectedVertex);
//        markEventRunning();
//        Event2Controller.event2Prompt(sociograph, selectedVertex);

        // Event ended must be called inside the event method when it ends
    }

    public void event6Handler(ActionEvent event) {
        if (isEventRunning) {
            return;
        }

//        markEventRunning();
//        Event2Controller.event2Prompt(sociograph, selectedVertex);

        // Event ended must be called inside the event method when it ends
    }

    public void sixDegreeHandler(ActionEvent event){
        SixDegreeController.sixDegreePrompt(sociograph,selectedVertex);
    }

    public class VertexFX extends Circle {
        Point coordinate;
        Text nameText;
        Group vertexHolder;
        boolean isSelected;
        LinkedList<EdgeFX> connectedEdges;

        public VertexFX(double x, double y, double radius, String name) {
            super(x, y, radius);

            vertexHolder = new Group();
            this.connectedEdges = new LinkedList<>();
            this.coordinate = new Point((int) x, (int) y);
            this.nameText = new Text(name);
            nameText.setStyle("-fx-font-weight: bold");
            setNameText(name);

            this.setOpacity(0.5);
            this.setBlendMode(BlendMode.MULTIPLY);
            this.setId("vertex");

            this.setOnMouseClicked(mouseHandler);
            this.setOnMouseReleased(mouseHandler);
            this.setOnMouseDragged(mouseHandler);
            this.setOnMousePressed(mouseHandler);
            this.setOnMouseEntered(mouseHandler);
            this.setOnMouseExited(mouseHandler);

            sociograph.addVertex(name);
            allCircles.add(this);

            RightClickMenu rightClickMenu = new RightClickMenu(this);
            ContextMenu menu = rightClickMenu.getMenu();
            vertexHolder.setOnContextMenuRequested(e -> {
                menu.show(this, e.getScreenX(), e.getScreenY());
            });

            vertexHolder.getChildren().addAll(nameText, this);

            ScaleTransition tr = new ScaleTransition(Duration.millis(100), this);
            tr.setByX(10f);
            tr.setByY(10f);
            tr.setInterpolator(Interpolator.EASE_OUT);
            tr.play();

            System.out.println(sociograph + "\n");
        }

        public void showVertex() {
            canvasGroup.getChildren().addAll(vertexHolder);
        }

        public void setNameText(String name) {
            nameText.setText(name);
            nameText.setX(this.coordinate.x);
            nameText.setY(this.coordinate.y);
            nameText.setX(nameText.getX() - nameText.getLayoutBounds().getWidth() / 2);
            nameText.setY(nameText.getY() + nameText.getLayoutBounds().getHeight() / 4);
        }

        public void dragVertex(int x, int y) {
            this.coordinate.x = x;
            this.coordinate.y = y;

            nameText.setX(this.coordinate.x);
            nameText.setY(this.coordinate.y);
            nameText.setX(nameText.getX() - nameText.getLayoutBounds().getWidth() / 2);
            nameText.setY(nameText.getY() + nameText.getLayoutBounds().getHeight() / 4);

            this.setCenterX(x);
            this.setCenterY(y);

            for (EdgeFX edge : connectedEdges) {
                edge.update();
            }

        }
    }

    public class EdgeFX extends Group {
        // Line
        // Arrow
        // Reputation text
        Line line;
        Polygon arrowSrc;
        Polygon arrowEnd;
        Text srcRepText;
        Text endRepText;

        VertexFX srcVertex;
        VertexFX endVertex;
        Relationship rel;
        boolean isDirected;

        private EdgeFX(VertexFX srcVertex, VertexFX endVertex, Relationship rel) {
            double[] arrowPoints = {0.0, 5.0, -5.0, -5.0, 5.0, -5.0};

            this.srcVertex = srcVertex;
            this.endVertex = endVertex;
            this.rel = rel;

            this.line = new Line();
            this.arrowSrc = new Polygon(arrowPoints);
            this.arrowEnd = new Polygon(arrowPoints);
            this.srcRepText = new Text();
            this.endRepText = new Text();

            this.line.setOpacity(0.5);
            this.arrowSrc.setOpacity(0.8);
            this.arrowEnd.setOpacity(0.8);
            this.srcRepText.setStyle("-fx-font-weight: bold");
            this.endRepText.setStyle("-fx-font-weight: bold");
            changeRel(rel);

            RightClickMenu rt = new RightClickMenu(this);
            ContextMenu menu = rt.getMenu();
            this.setOnContextMenuRequested(e -> {
                menu.show(this, e.getScreenX(), e.getScreenY());
            });
        }

        public EdgeFX(VertexFX srcVertex, VertexFX endVertex, String srcRep, Relationship rel) {
            this(srcVertex, endVertex, rel);

            this.isDirected = true;
            this.srcRepText.setText(Double.parseDouble(srcRep) + "");

            initializeLine();
            initializeArrow();
            initializeRep();
            this.getChildren().addAll(line, arrowEnd, srcRepText);

            srcVertex.connectedEdges.add(this);
            endVertex.connectedEdges.add(this);
            sociograph.addDirectedEdge(srcVertex.nameText.getText(), endVertex.nameText.getText(), Double.parseDouble(srcRep), rel);

            System.out.println(sociograph + "\n");
        }

        public EdgeFX(VertexFX srcVertex, VertexFX endVertex, String srcRep, String endRep, Relationship rel) {
            this(srcVertex, endVertex, rel);

            this.isDirected = false;
            this.srcRepText.setText(Double.parseDouble(srcRep) + "");
            this.endRepText.setText(Double.parseDouble(endRep) + "");

            initializeLine();
            initializeArrow();
            initializeRep();
            this.getChildren().addAll(line, arrowSrc, arrowEnd, srcRepText, endRepText);

            srcVertex.connectedEdges.add(this);
            endVertex.connectedEdges.add(this);
            sociograph.addUndirectedEdge(srcVertex.nameText.getText(), endVertex.nameText.getText(), Double.parseDouble(srcRep), Double.parseDouble(endRep), rel);

            System.out.println(sociograph + "\n");
        }

        public void showEdge() {
            canvasGroup.getChildren().add(this);
        }

        public void changeRel(Relationship rel) {
            this.rel = rel;
            switch (rel) {
                case NONE:
                    this.line.setStroke(Color.BLACK);
                    this.arrowEnd.setStyle("-fx-fill: BLACK");
                    this.arrowSrc.setStyle("-fx-fill: BLACK");
                    break;
                case FRIEND:
                    this.line.setStroke(Color.web("#1b2389"));
                    this.arrowEnd.setStyle("-fx-fill: #1b2389");
                    this.arrowSrc.setStyle("-fx-fill: #1b2389");
                    break;
                case ENEMY:
                    this.line.setStroke(Color.web("#ff0c0c"));
                    this.arrowEnd.setStyle("-fx-fill: #ff0c0c");
                    this.arrowSrc.setStyle("-fx-fill: #ff0c0c");
                    break;
                case ADMIRED_BY:
                    this.line.setStroke(Color.web("#c843ff"));
                    this.arrowEnd.setStyle("-fx-fill: #c843ff");
                    this.arrowSrc.setStyle("-fx-fill: #c843ff");
                    break;
                case THE_OTHER_HALF:
                    this.line.setStroke(Color.web("#e0890c"));
                    this.arrowEnd.setStyle("-fx-fill: #e0890c");
                    this.arrowSrc.setStyle("-fx-fill: #e0890c");
                    break;
            }
        }

        public void changeSrcRepText(String srcRepStr) {
            this.srcRepText.setText(Double.parseDouble(srcRepStr) + "");
        }

        public void changeEndRepText(String endRepStr) {
            this.endRepText.setText(Double.parseDouble(endRepStr) + "");
        }

        public void update() {
            initializeLine();
            initializeArrow();
            initializeRep();
        }

        private void initializeLine() {
            double distance = Math.sqrt(Math.pow(endVertex.getCenterX() - srcVertex.getCenterX(), 2) + Math.pow(endVertex.getCenterY() - srcVertex.getCenterY(), 2));
            double m = 12;
            double n = distance - m;
            line.setStartX((m * endVertex.getCenterX() + n * srcVertex.getCenterX()) / distance);
            line.setStartY((m * endVertex.getCenterY() + n * srcVertex.getCenterY()) / distance);
            line.setEndX((m * srcVertex.getCenterX() + n * endVertex.getCenterX()) / distance);
            line.setEndY((m * srcVertex.getCenterY() + n * endVertex.getCenterY()) / distance);
        }

        private void initializeArrow() {
            double angle, height, width, length, subtractWidth, subtractHeight;
            if (!isDirected) {  // Also set arrow at src if undirected
                angle = Math.atan2(line.getStartY() - line.getEndY(), line.getStartX() - line.getEndX()) * 180 / 3.14;

                height = line.getStartY() - line.getEndY();
                width = line.getStartX() - line.getEndX();
                length = Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2));

                subtractWidth = 5 * width / length;
                subtractHeight = 5 * height / length;

                arrowSrc.setRotate(angle - 90);
                arrowSrc.setTranslateX(line.getEndX());
                arrowSrc.setTranslateY(line.getEndY());
                arrowSrc.setTranslateX(line.getStartX() - subtractWidth);
                arrowSrc.setTranslateY(line.getStartY() - subtractHeight);
            }
            // Set arrow at end no matter directed or undirected
            angle = Math.atan2(line.getEndY() - line.getStartY(), line.getEndX() - line.getStartX()) * 180 / 3.14;

            height = line.getEndY() - line.getStartY();
            width = line.getEndX() - line.getStartX();
            length = Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2));

            subtractWidth = 5 * width / length;
            subtractHeight = 5 * height / length;

            arrowEnd.setRotate(angle - 90);
            arrowEnd.setTranslateX(line.getStartX());
            arrowEnd.setTranslateY(line.getStartY());
            arrowEnd.setTranslateX(line.getEndX() - subtractWidth);
            arrowEnd.setTranslateY(line.getEndY() - subtractHeight);
        }

        private void initializeRep() {
            double resultX, resultY;
            if (!isDirected) {
                resultX = ((0.2 * srcVertex.getCenterX()) + (0.8 * endVertex.getCenterX()));
                resultY = ((0.2 * srcVertex.getCenterY()) + (0.8 * endVertex.getCenterY()));
                endRepText.setX(resultX);
                endRepText.setY(resultY);
            }
            resultX = ((0.2 * endVertex.getCenterX()) + (0.8 * srcVertex.getCenterX()));
            resultY = ((0.2 * endVertex.getCenterY()) + (0.8 * srcVertex.getCenterY()));
            srcRepText.setX(resultX);
            srcRepText.setY(resultY);

        }
    }

    private List<Object> convertStudentInfoToTextFields(Student student) {
        String name = student.getName();
        double dive = student.getDive();
        LocalTime[] lunchStartArr = student.getLunchStart();
        int[] lunchPeriodArr = student.getLunchPeriod();
        LocalTime estimatedLunchEnd = student.getLunchEnd();
        Map<String, Double> repPointsMap = student.getRepPoints();
        Set<Student> friendsSet = student.getFriends();
        Set<Student> enemiesSet = student.getEnemies();
        Set<Student> nonesSet = student.getNones();
        Set<Student> admirersSet = student.getAdmirers();
        Student theOtherHalfObj = student.getTheOtherHalf();

        StringBuilder lunchStart = new StringBuilder();
        for (int i = 0; i < lunchStartArr.length; i++) {
            lunchStart.append(lunchStartArr[i]);
            if (i != lunchStartArr.length - 1) {
                lunchStart.append(", ");
            }
        }

        StringBuilder lunchPeriod = new StringBuilder();
        for (int i = 0; i < lunchPeriodArr.length; i++) {
            lunchPeriod.append(lunchPeriodArr[i]);
            if (i != lunchPeriodArr.length - 1) {
                lunchPeriod.append(", ");
            }
        }

        StringBuilder friendsSB = new StringBuilder();
        friendsSet.forEach(v -> {
            friendsSB.append(v.getName()).append(", ");
        });
        String friends = friendsSet.size() == 0 ? "-" : friendsSB.substring(0, friendsSB.length() - 2);

        StringBuilder enemiesSB = new StringBuilder();
        enemiesSet.forEach(v -> {
            enemiesSB.append(v.getName()).append(", ");
        });
        String enemies = enemiesSet.size() == 0 ? "-" : enemiesSB.substring(0, enemiesSB.length() - 2);

        StringBuilder nonesSB = new StringBuilder();
        nonesSet.forEach(v -> {
            nonesSB.append(v.getName()).append(", ");
        });
        String nones = nonesSet.size() == 0 ? "-" : nonesSB.substring(0, nonesSB.length() - 2);

        StringBuilder admirersSB = new StringBuilder();
        admirersSet.forEach(v -> {
            admirersSB.append(v.getName()).append(", ");
        });
        String admirers = admirersSet.size() == 0 ? "-" : admirersSB.substring(0, admirersSB.length() - 2);

        String theOtherHalf = theOtherHalfObj == null ? "-" : theOtherHalfObj.getName();

        TextField nameTF = new TextField(name);
        TextField diveTF = new TextField(dive + "");
        TextField lunchStartTF = new TextField(lunchStart.toString());
        TextField lunchPeriodTF = new TextField(lunchPeriod.toString());
        TextField estimatedLunchEndTF = new TextField((estimatedLunchEnd == null) ? "Not calculated yet" : estimatedLunchEnd.toString());
        TextField friendsTF = new TextField(friends);
        TextField enemiesTF = new TextField(enemies);
        TextField nonesTF = new TextField(nones);
        TextField admirersTF = new TextField(admirers);
        TextField theOtherHalfTF = new TextField(theOtherHalf);
        List<TextField> repPointsTFs = new ArrayList<>();
        for (String key : repPointsMap.keySet()) {
            repPointsTFs.add(new TextField(repPointsMap.get(key) + " pts relative to " + key));
        }

        List<Object> textFieldList = new ArrayList<>();
        textFieldList.add(nameTF);
        textFieldList.add(diveTF);
        textFieldList.add(lunchStartTF);
        textFieldList.add(lunchPeriodTF);
        textFieldList.add(estimatedLunchEndTF);
        textFieldList.add(repPointsTFs);
        textFieldList.add(friendsTF);
        textFieldList.add(enemiesTF);
        textFieldList.add(nonesTF);
        textFieldList.add(admirersTF);
        textFieldList.add(theOtherHalfTF);

        return textFieldList;
    }

    private List<String> getStudentInfoAllFieldNames(Student student) {
        List<String> labelNameList = new ArrayList<>();
        labelNameList.add("Name");
        labelNameList.add("Diving rate (%)");
        labelNameList.add("Lunch start (hours)");
        labelNameList.add("Lunch period (minutes)");
        labelNameList.add("Estimated lunch end (hours)");
        labelNameList.add("Reputations");
        labelNameList.add("Friends");
        labelNameList.add("Enemies");
        labelNameList.add("No relationship");
        labelNameList.add("Who likes " + student.getName());
        labelNameList.add("The other half of " + student.getName());
        return labelNameList;
    }

    private void drawAllVertexAndEdge(Sociograph newSociograph, HashMap<String, Boolean> isCreated){
        int xSize = 510 / 30;
        int ySize = 570 / 30;

        Point[][] coordinates = new Point[xSize][ySize];
        boolean[][] hasNode = new boolean[xSize][ySize];
        int startY = 50;

        for(int i = 0; i< coordinates.length; i++){
            int startX = 50;
            for(int j = 0; j<coordinates[i].length; j++){
                coordinates[i][j] =  new Point(startX, startY);
                startX += 30;
            }
            startY += 30;
        }

        for(Student student : newSociograph.getAllStudents()) {
            VertexFX srcVertex = getVertex(student, isCreated, hasNode, coordinates);
            String srcName = srcVertex.nameText.getText();

            Map<String, Double> srcReps = student.getRepPoints();
            Set<Student> friends = student.getFriends();
            Set<Student> enemies = student.getEnemies();
            Set<Student> nones = student.getNones();
            Set<Student> admirers = student.getAdmirers();
            Set<Student> theOtherHalf = new HashSet<>();
            theOtherHalf.add(student.getTheOtherHalf());
            drawRelationship(srcVertex, srcReps, srcName, isCreated, friends, Relationship.FRIEND, coordinates, hasNode);
            drawRelationship(srcVertex, srcReps, srcName, isCreated, enemies, Relationship.ENEMY, coordinates, hasNode);
            drawRelationship(srcVertex, srcReps, srcName, isCreated, nones, Relationship.NONE, coordinates, hasNode);
            drawRelationship(srcVertex, srcReps, srcName, isCreated, admirers, Relationship.ADMIRED_BY, coordinates, hasNode);
            drawRelationship(srcVertex, srcReps, srcName, isCreated, theOtherHalf, Relationship.THE_OTHER_HALF,coordinates, hasNode);

        }
    }

    private VertexFX getVertex(Student student, HashMap<String, Boolean> isCreated, boolean[][] hasNode, Point[][] coordinates){
        String name = student.getName();

        VertexFX vertex = null;
        if(!isCreated.get(name)) {
            int xRandom;
            int yRandom;
            do {
                xRandom = (int) (Math.random() * coordinates.length);
                yRandom = (int) (Math.random() * coordinates[xRandom].length);
            } while(hasNode[xRandom][yRandom]);

            hasNode[xRandom][yRandom] = true;
            Point coordinate = coordinates[xRandom][yRandom];
            vertex = new VertexFX(coordinate.x, coordinate.y  , 1.2, name);
            isCreated.put(name, true);
            vertex.showVertex();
        }

        else{
            for (VertexFX allCircle : allCircles)
                if (allCircle.nameText.getText().equals(name))
                    vertex = allCircle;
        }

        return vertex;
    }

    private void drawRelationship(VertexFX srcVertex,Map<String, Double> srcReps,String srcName,HashMap<String, Boolean> isCreated,Set<Student> haveRelationStudents, Relationship relationship, Point[][] coordinates, boolean[][] hasNode){
        for (Student dest : haveRelationStudents) {
            if (dest != null) {
                VertexFX destVertex = getVertex(dest, isCreated, hasNode, coordinates);
                String destName = destVertex.nameText.getText();

                HashMap<String, Double> destReps = dest.getRepPoints();
                String srcRep = String.valueOf(srcReps.get(destName));
                String destRep = String.valueOf(destReps.get(srcName));

                boolean checkUndirectedRelationship = relationship == Relationship.ENEMY || relationship == Relationship.FRIEND || relationship == Relationship.THE_OTHER_HALF;
                if (!sociograph.hasUndirectedEdge(srcName, destName) && checkUndirectedRelationship) {
                    EdgeFX edgeFX = new EdgeFX(srcVertex, destVertex, srcRep, destRep, relationship);
                    edgeFX.showEdge();
                } else if (!sociograph.hasDirectedEdge(srcName, destName) && !checkUndirectedRelationship) {
                    if (relationship == Relationship.ADMIRED_BY) {
                        EdgeFX edgeFX = new EdgeFX(srcVertex, destVertex, srcRep, Relationship.ADMIRED_BY);
                        edgeFX.showEdge();
                    } else if (relationship == Relationship.NONE) {
                        EdgeFX edgeFX = new EdgeFX(srcVertex, destVertex, srcRep, Relationship.NONE);
                        edgeFX.showEdge();
                    }
                }
            }
        }
    }

    public void setDefaultDialogConfig(Dialog dialog) {
        dialog.setGraphic(null);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("dialog");
        dialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());
    }

    public VertexFX getVertexFX(String name) {
        for (VertexFX vertexFX : allCircles) {
            if (vertexFX.nameText.getText().equals(name)) {
                return vertexFX;
            }
        }
        return null;
    }

    public EdgeFX getEdgeFX(String srcName, String destName) {  // Might return directed or undirected edgeFX
        for (VertexFX vertexFX : allCircles) {
            List<EdgeFX> listOfEdgeFX = vertexFX.connectedEdges;
            for (EdgeFX edgeFX : listOfEdgeFX) {
                if ((edgeFX.srcVertex.nameText.getText().equals(srcName) && edgeFX.endVertex.nameText.getText().equals(destName)) ||
                        edgeFX.srcVertex.nameText.getText().equals(destName) && edgeFX.endVertex.nameText.getText().equals(srcName)) {
                    return edgeFX;
                }
            }
        }
        return null;
    }

    public EdgeFX createNewUndirectedEdgeFX(VertexFX srcVertex, VertexFX endVertex, String srcRep, String endRep, Relationship rel) {
        EdgeFX newUndirectedEdge = new EdgeFX(srcVertex, endVertex, srcRep, endRep, rel);
        newUndirectedEdge.showEdge();
        return newUndirectedEdge;
    }

    public EdgeFX createNewDirectedEdgeFX(VertexFX srcVertex, VertexFX endVertex, String srcRep, Relationship rel) {
        EdgeFX newDirectedEdge = new EdgeFX(srcVertex, endVertex, srcRep, rel);
        newDirectedEdge.showEdge();
        return newDirectedEdge;
    }

    public void changeSrcRepRelativeToAdjFX(String srcName, String adjName, double newSrcRep) {
        EdgeFX edgeToChange = getEdgeFX(srcName, adjName);

        if (edgeToChange != null) {

            // If the edge is directed, have to make sure if the src and adj is specified correctly
            if (edgeToChange.isDirected) {
                if (edgeToChange.srcVertex.nameText.getText().equals(srcName) && edgeToChange.endVertex.nameText.getText().equals(adjName)) {
                    edgeToChange.changeSrcRepText(newSrcRep + "");
                    return;
                }
            } else {    // If the edge is undirected, have to make sure which rep is going to be changed
                if (edgeToChange.srcVertex.nameText.getText().equals(srcName) && edgeToChange.endVertex.nameText.getText().equals(adjName)) {
                    edgeToChange.changeSrcRepText(newSrcRep + "");
                } else {
                    edgeToChange.changeEndRepText(newSrcRep + "");
                }
                return;
            }
        }
        throw new IllegalArgumentException("This edge is not exist (" + srcName + " -> " + adjName + ") or (" + srcName + " <-> " + adjName + ")");
    }

    public void markEventRunning() {
        this.isEventRunning = true;
    }

    public void markEventEnded() {
        this.isEventRunning = false;
    }

    public void deleteEdgeFXWithoutPrompt(EdgeFX edge){
        if (!edge.isDirected) {
            System.out.println(sociograph.removeEdge(edge.endVertex.nameText.getText(), edge.srcVertex.nameText.getText()));
        }
        System.out.println(sociograph.removeEdge(edge.srcVertex.nameText.getText(), edge.endVertex.nameText.getText()));
        System.out.println(allCircles.get(allCircles.indexOf(edge.endVertex)).connectedEdges.remove(edge));
        System.out.println(allCircles.get(allCircles.indexOf(edge.srcVertex)).connectedEdges.remove(edge));
        System.out.println(canvasGroup.getChildren().remove(edge));
        System.out.println(sociograph.getStudent(edge.endVertex.nameText.getText()));
        System.out.println(sociograph.getStudent(edge.srcVertex.nameText.getText()));
        System.out.println(sociograph);
    }

    public Optional<ButtonType> showDescriptionDialog(String title, String headerText, String descriptionText) {
        Alert description = new Alert(Alert.AlertType.INFORMATION);
        description.setTitle(title);
        description.setHeaderText(headerText);
        TextArea area = new TextArea(descriptionText);
        area.setWrapText(true);
        area.setEditable(false);
        description.getDialogPane().setPrefWidth(400);
        description.getDialogPane().setPrefHeight(350);
        description.getDialogPane().setContent(area);
        description.setResizable(true);
        setDefaultDialogConfig(description);
        return description.showAndWait();
    }

    public VertexFX createVertexFX(double x, double y, double radius, String name){
        VertexFX newVertex = new VertexFX(x, y, radius, name);
        newVertex.showVertex();
        return newVertex;
    }

}
