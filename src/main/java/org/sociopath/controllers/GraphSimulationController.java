package org.sociopath.controllers;

import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.Pair;
import org.sociopath.App;
import org.sociopath.models.Relationship;
import org.sociopath.models.Sociograph;
import org.sociopath.models.Student;

import java.awt.*;
import java.io.IOException;
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

    private Sociograph sociograph = new Sociograph();
    private List<VertexFX> allCircles = new ArrayList<>();
    private VertexFX selectedVertex = null;


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
    public void canvasHandler(MouseEvent mouseEvent) throws IOException {
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
                Stage enterStudentDetailsWindow = openDialogWindow("../fxml/enter_name_dialog.fxml", "Enter student details");
                enterStudentDetailsWindow.setOnHiding(event -> {
                    if (EnterNameDialogController.studentName == null) {
                        EnterNameDialogController.stopAddStudent = true;
                    } else {
                        EnterNameDialogController.stopAddStudent = false;
                    }
                });
                enterStudentDetailsWindow.showAndWait();

                if (EnterNameDialogController.stopAddStudent) {
                    return;
                }

                String obtainedName = EnterNameDialogController.studentName;
                if (!sociograph.hasVertex(obtainedName)) {
                    VertexFX circle = new VertexFX(mouseEvent.getX(), mouseEvent.getY(), 1.2, obtainedName);
                    circle.showVertex();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(obtainedName + " is already exist in the graph!");
                    DialogPane alertDialog = alert.getDialogPane();
                    alertDialog.getStyleClass().add("dialog");
                    alertDialog.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());
                    alert.initStyle(StageStyle.UTILITY);
                    alert.show();
                }
            }
        }

    }

    public void studentInfoCard(VertexFX vertex) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Student information card");
        dialog.setHeaderText("Student " + vertex.nameText.getText());
        dialog.initStyle(StageStyle.UTILITY);
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
        List<String> labelNameList = getStudentInfoAllFieldNames();

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
        dialog.getDialogPane().getStyleClass().add("dialog");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());


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
        confirmDialog.setGraphic(null);
        confirmDialog.setHeaderText("Are you sure ?");
        DialogPane confirmDialogPane = confirmDialog.getDialogPane();
        confirmDialogPane.getStyleClass().add("dialog");
        confirmDialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());
        confirmDialog.initStyle(StageStyle.UTILITY);

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            System.out.println("Remove");
            allCircles.remove(vertex);
            sociograph.deleteVertex(vertex.nameText.getText());     // this will delete vertex and also connected edges
            canvasGroup.getChildren().remove(vertex.vertexHolder);

            // Delete edges that use this vertex as srcVertex
            List<Group> edgeFXHolders = new ArrayList<>();
            vertex.connectedEdges.forEach(edge -> edgeFXHolders.add(edge.edgeHolder));
            System.out.println("Remove edges use this as source: " + canvasGroup.getChildren().removeAll(edgeFXHolders));

            // Delete the remaining edges that use this vertex as destVertex
            for (VertexFX vertexFX : allCircles) {
                List<EdgeFX> list = vertexFX.connectedEdges;
                list.removeIf(edge -> {
                    if (edge.destVertex.nameText.getText().equals(vertex.nameText.getText())) {
                        boolean removeresult = canvasGroup.getChildren().remove(edge.edgeHolder);
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
        } else {
            System.out.println("return");
            return;
        }
    }

    public void changeVertexFXName(VertexFX vertex) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setGraphic(null);
        dialog.setTitle("Change student name");
        dialog.setHeaderText("Enter new name");
        dialog.setContentText("Name");
        dialog.initStyle(StageStyle.UTILITY);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("dialog");
        dialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());

        TextField textField = dialog.getEditor();
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

        okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            textField.getText().trim().isEmpty() || textField.getText().trim().contains(" ") || sociograph.hasVertex(textField.getText().trim())
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

        String contextStr = "Are you sure you want to delete this edge (" + edge.srcVertex.nameText.getText() + arrow + edge.destVertex.nameText.getText() +") ?";
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, contextStr, ButtonType.YES, ButtonType.NO);
        confirmDialog.setGraphic(null);
        confirmDialog.setHeaderText("Are you sure ?");
        DialogPane confirmDialogPane = confirmDialog.getDialogPane();
        confirmDialogPane.getStyleClass().add("dialog");
        confirmDialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());
        confirmDialog.initStyle(StageStyle.UTILITY);

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (!edge.isDirected) {
                System.out.println(sociograph.removeEdge(edge.destVertex.nameText.getText(), edge.srcVertex.nameText.getText()));
            }
            System.out.println(sociograph.removeEdge(edge.srcVertex.nameText.getText(), edge.destVertex.nameText.getText()));
            System.out.println(allCircles.get(allCircles.indexOf(edge.srcVertex)).connectedEdges.remove(edge));
            System.out.println(canvasGroup.getChildren().remove(edge.edgeHolder));

        } else {
            return;
        }
    }

    public void changeRepOrRelationTypeFX(EdgeFX edge) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.getDialogPane().setPrefWidth(300);
        dialog.setGraphic(null);
        dialog.setHeaderText("Enter new properties between student " + edge.srcVertex.nameText.getText() + " and " + edge.destVertex.nameText.getText());
        dialog.initStyle(StageStyle.UTILITY);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("dialog");
        dialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 10, 30, 10));

        if (edge.isDirected) {  // Directed edge pane
            dialog.getDialogPane().setPrefHeight(200);
            dialog.setTitle("Modify directed edge's properties");

            TextField prevRepTF = new TextField(edge.srcRepPointText.getText());
            prevRepTF.setEditable(false);
            prevRepTF.setPrefWidth(100);
            TextField newRepTF = new TextField();
            newRepTF.setPromptText(edge.srcVertex.nameText.getText() + "'s rep pts");
            newRepTF.setPrefWidth(100);

            gridPane.add(new Label("Original reputation"), 0, 0);
            gridPane.add(prevRepTF, 1, 0);
            gridPane.add(new Label("New reputation"), 0, 1);
            gridPane.add(newRepTF, 1, 1);

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
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                srcRepStr = newRepTF.getText().trim();
            }

            if (srcRepStr != null) {
                // Change rep points
                edge.changeSrcRepPoint(srcRepStr);
                sociograph.setSrcRepRelativeToAdj(edge.srcVertex.nameText.getText(), edge.destVertex.nameText.getText(), Double.parseDouble(srcRepStr));
            }

        } else {    // Undirected edge pane
            dialog.getDialogPane().setPrefHeight(400);
            dialog.setTitle("Modify undirected edge's properties");

            TextField prevSrcRepTF = new TextField(edge.srcRepPointText.getText());
            prevSrcRepTF.setEditable(false);
            prevSrcRepTF.setPrefWidth(100);
            TextField prevDestRepTF = new TextField(edge.destRepPointText.getText());
            prevDestRepTF.setEditable(false);
            prevDestRepTF.setPrefWidth(100);
            TextField prevRelTypeTF = new TextField(edge.rel + "");
            prevRelTypeTF.setEditable(false);
            prevRelTypeTF.setPrefWidth(100);

            TextField newSrcRepTF = new TextField();
            newSrcRepTF.setPromptText(edge.srcVertex.nameText.getText() + "'s rep pts");
            newSrcRepTF.setPrefWidth(100);
            TextField newDestRepTF = new TextField();
            newDestRepTF.setPromptText(edge.destVertex.nameText.getText() + "'s rep pts");
            newDestRepTF.setPrefWidth(100);
            ComboBox<Relationship> newRelTypeCB = new ComboBox<>(FXCollections.observableArrayList(Relationship.NONE, Relationship.FRIEND, Relationship.ENEMY));
            newRelTypeCB.setValue(Relationship.NONE);
            newRelTypeCB.setPrefWidth(100);

            Label originalLabel = new Label("Original properties");
            originalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            gridPane.add(originalLabel, 0, 0);
            gridPane.add(new Label("Original " + edge.srcVertex.nameText.getText() + "'s reputation"), 0, 1);
            gridPane.add(new Label("Original " + edge.destVertex.nameText.getText() + "'s reputation"), 0, 2);
            gridPane.add(new Label("Original relationship"), 0, 3);

            Label newLabel = new Label("New properties");
            newLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            gridPane.add(newLabel, 0, 5);
            gridPane.add(new Label("New " + edge.srcVertex.nameText.getText() + "'s reputation"), 0, 6);
            gridPane.add(new Label("New " + edge.destVertex.nameText.getText() + "'s reputation"), 0, 7);
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
                edge.changeSrcRepPoint(srcRepStr);
                edge.changeDestRepPoint(destRepStr);
                sociograph.setSrcRepRelativeToAdj(edge.srcVertex.nameText.getText(), edge.destVertex.nameText.getText(), Double.parseDouble(srcRepStr));
                sociograph.setSrcRepRelativeToAdj(edge.destVertex.nameText.getText(), edge.srcVertex.nameText.getText(), Double.parseDouble(destRepStr));

                // Change relation
                sociograph.setRelationshipOnEdge(edge.srcVertex.nameText.getText(), edge.destVertex.nameText.getText(), relType);
                edge.setEdgeRelation(relType);
            }
        }
    }

    EventHandler<MouseEvent> mouseHandler = event -> {

        VertexFX destVertexFX = (VertexFX) event.getSource();
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.getButton() == MouseButton.PRIMARY) {
            if (!destVertexFX.isSelected) {

                if (selectedVertex != null) {
                    VertexFX srcVertexFX = selectedVertex;
                    String srcVertexName = srcVertexFX.nameText.getText();
                    String destVertexName = destVertexFX.nameText.getText();

                    if (addRepBtn.isSelected() && sociograph.hasDirectedEdge(destVertexName, srcVertexName) &&
                            !sociograph.hasDirectedEdge(srcVertexName, destVertexName)) {

                        TextInputDialog dialog = new TextInputDialog();
                        dialog.getDialogPane().setPrefHeight(300);
                        dialog.getDialogPane().setPrefWidth(350);
                        dialog.setGraphic(null);
                        dialog.setTitle("Convert to undirected edge");
                        dialog.setHeaderText(srcVertexName + " and " + destVertexName + " can have a relation when they know each other, what do you want their relationship to be?");
                        dialog.initStyle(StageStyle.UTILITY);
                        DialogPane dialogPane = dialog.getDialogPane();
                        dialogPane.getStyleClass().add("dialog");
                        dialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());

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
                        ComboBox<Relationship> relationCB = new ComboBox<>(FXCollections.observableArrayList(Relationship.NONE, Relationship.FRIEND, Relationship.ENEMY));
                        relationCB.setValue(Relationship.NONE);
                        relationCB.setPrefWidth(100);

                        gridPane.add(new Label(srcVertexName + "'s rep relative to " + destVertexName), 0, 0);
                        gridPane.add(new Label(destVertexName + "'s rep relative to " + srcVertexName), 0, 1);
                        gridPane.add(new Label("Relationship type"), 0, 2);
                        gridPane.add(srcRepTF, 1, 0);
                        gridPane.add(destRepTF, 1, 1);
                        gridPane.add(relationCB, 1, 2);

                        dialog.getDialogPane().setContent(gridPane);

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
                            relType = relationCB.getValue();
                        }

                        if (srcRepStr != null && destRepStr != null && relType != null) {
                            // Delete directed edge from dest to src in allEdges and canvasGroup
                            LinkedList<EdgeFX> connectedEdges = allCircles.get(allCircles.indexOf(destVertexFX)).connectedEdges;
                            System.out.println(connectedEdges + "\n");
                            connectedEdges.removeIf(edge -> {
                                if (edge.destVertex.nameText.getText().equals(srcVertexName)) {
                                    return canvasGroup.getChildren().remove(edge.edgeHolder);
                                }
                                return false;
                            });
                            System.out.println(connectedEdges);

                            // Delete directed edge from dest to src in sociograph
                            sociograph.removeEdge(destVertexName, srcVertexName);

                            // create a new EdgeFX object to represent the new undirected edge
                            EdgeFX newUndirectedEdge = new EdgeFX(srcVertexFX, destVertexFX, relType, srcRepStr, destRepStr);
                            newUndirectedEdge.showEdge();
                        }

                    } else if (addRepBtn.isSelected() && !sociograph.hasDirectedEdge(srcVertexName, destVertexName)) {

                        TextInputDialog dialog = new TextInputDialog();
                        dialog.getDialogPane().setPrefHeight(200);
                        dialog.getDialogPane().setPrefWidth(300);
                        dialog.setGraphic(null);
                        dialog.setTitle("Add directed edge");
                        dialog.setHeaderText("Enter student " + srcVertexName + " reputation point relative to " + destVertexName);
                        dialog.setContentText("Reputation pts");
                        dialog.initStyle(StageStyle.UTILITY);
                        DialogPane dialogPane = dialog.getDialogPane();
                        dialogPane.getStyleClass().add("dialog");
                        dialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());

                        TextField textField = dialog.getEditor();
                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

                        okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                                    boolean isNumber;
                                    try {
                                        double rep = Double.parseDouble(textField.getText());
                                        isNumber = true;
                                    } catch (NumberFormatException e) {
                                        isNumber = false;
                                    }
                                    return !isNumber;
                                }, textField.textProperty()
                        ));

                        String repStr = null;
                        Optional<String> result = dialog.showAndWait();
                        if (result.isPresent()) {
                            repStr = result.get();
                        }

                        if (repStr != null) {
                            EdgeFX directedEdge = new EdgeFX(srcVertexFX, destVertexFX, repStr);
                            directedEdge.showEdge();
                        }

                    } else if (addRelationBtn.isSelected() && !sociograph.hasDirectedEdge(srcVertexName, destVertexName) &&
                            !sociograph.hasDirectedEdge(destVertexName, srcVertexName)) {

                        TextInputDialog dialog = new TextInputDialog();
                        dialog.getDialogPane().setPrefHeight(200);
                        dialog.getDialogPane().setPrefWidth(300);
                        dialog.setGraphic(null);
                        dialog.setTitle("Add undirected edge");
                        dialog.setHeaderText("Enter details between student " + srcVertexName + " and " + destVertexName);
                        dialog.initStyle(StageStyle.UTILITY);
                        DialogPane dialogPane = dialog.getDialogPane();
                        dialogPane.getStyleClass().add("dialog");
                        dialogPane.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());

                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10);
                        gridPane.setVgap(10);
                        gridPane.setPadding(new Insets(20, 10, 30, 10));

                        TextField srcRepTF = new TextField();
                        srcRepTF.setPromptText(srcVertexName + "'s rep pts");
                        srcRepTF.setPrefWidth(100);
                        TextField destRepTF = new TextField();
                        destRepTF.setPromptText(destVertexName + "'s rep pts");
                        destRepTF.setPrefWidth(100);
                        ComboBox<Relationship> relationCB = new ComboBox<>(FXCollections.observableArrayList(Relationship.NONE, Relationship.FRIEND, Relationship.ENEMY));
                        relationCB.setValue(Relationship.NONE);
                        relationCB.setPrefWidth(100);

                        gridPane.add(new Label(srcVertexName + "'s rep relative to " + destVertexName), 0, 0);
                        gridPane.add(new Label(destVertexName + "'s rep relative to " + srcVertexName), 0, 1);
                        gridPane.add(new Label("Relationship type"), 0, 2);
                        gridPane.add(srcRepTF, 1, 0);
                        gridPane.add(destRepTF, 1, 1);
                        gridPane.add(relationCB, 1, 2);

                        dialog.getDialogPane().setContent(gridPane);

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
                            EdgeFX undirectedEdge = new EdgeFX(srcVertexFX, destVertexFX, relType, srcRepStr, destRepStr);
                            undirectedEdge.showEdge();
                        }

                    } else if (addRelationBtn.isSelected() || addRepBtn.isSelected()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText(srcVertexName + " and " + destVertexName + " are already connected!");
                        DialogPane alertDialog = alert.getDialogPane();
                        alertDialog.getStyleClass().add("dialog");
                        alertDialog.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());
                        alert.initStyle(StageStyle.UTILITY);
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
        }
    };

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

            System.out.println(sociograph + "\n");
        }

        public void showVertex() {
            vertexHolder.getChildren().addAll(nameText, this);
            canvasGroup.getChildren().addAll(vertexHolder);

            ScaleTransition tr = new ScaleTransition(Duration.millis(100), this);
            tr.setByX(10f);
            tr.setByY(10f);
            tr.setInterpolator(Interpolator.EASE_OUT);
            tr.play();
        }

        public void setNameText(String name) {
            nameText.setText(name);
            nameText.setX(this.coordinate.x);
            nameText.setY(this.coordinate.y);
            nameText.setX(nameText.getX() - nameText.getLayoutBounds().getWidth() / 2);
            nameText.setY(nameText.getY() + nameText.getLayoutBounds().getHeight() / 4);
        }
    }

    public class EdgeFX extends Path {

        static final int ARROWHEADSIZE = 7;
        VertexFX srcVertex, destVertex;
        Text srcRepPointText;
        Text destRepPointText;
        Relationship rel;
        Group edgeHolder;
        double srcX, srcY, destX, destY;
        boolean isDirected;

        public EdgeFX(VertexFX srcVertex, VertexFX destVertex, Relationship rel) {
            super();

            this.edgeHolder = new Group();

            this.srcVertex = srcVertex;
            this.destVertex = destVertex;
            this.setEdgeRelation(rel);
            this.computeSrcDestCoordinates(srcVertex.coordinate.x, srcVertex.coordinate.y, destVertex.coordinate.x, destVertex.coordinate.y);
            this.setId("edge");
            this.setOpacity(0.5);

            this.strokeProperty().bind(fillProperty());

            RightClickMenu rt = new RightClickMenu(this);
            ContextMenu menu = rt.getMenu();
            edgeHolder.setOnContextMenuRequested(e -> {
                menu.show(this, e.getScreenX(), e.getScreenY());
            });
        }

        // Directed edge
        public EdgeFX(VertexFX srcVertex, VertexFX destVertex, String srcRep) {
            this(srcVertex, destVertex, Relationship.NONE);

            this.isDirected = true;
            this.srcRepPointText = new Text(srcRep);
            this.srcRepPointText.setStyle("-fx-font-weight: bold");
            this.setRepPointText(srcRepPointText, srcRep, srcX, srcY, destX, destY);

            // Line
            getElements().add(new MoveTo(srcX, srcY));
            getElements().add(new LineTo(destX, destY));

            // Arrow head
            drawArrowHead(srcX, srcY, destX, destY);

            edgeHolder.getChildren().addAll(this, srcRepPointText);
            sociograph.addDirectedEdge(srcVertex.nameText.getText(), destVertex.nameText.getText(), Double.parseDouble(srcRep));
            srcVertex.connectedEdges.add(this);

            System.out.println(sociograph + "\n");
        }

        // Undirected edge
        public EdgeFX(VertexFX srcVertex, VertexFX destVertex, Relationship rel, String srcRep, String destRep) {
            this(srcVertex, destVertex, rel);

            this.isDirected = false;
            this.srcRepPointText = new Text(srcRep);
            this.srcRepPointText.setStyle("-fx-font-weight: bold");
            setRepPointText(srcRepPointText, srcRep, srcX, srcY, destX, destY);

            this.destRepPointText = new Text(destRep);
            this.destRepPointText.setStyle("-fx-font-weight: bold");
            setRepPointText(destRepPointText, destRep, destX, destY, srcX, srcY);

            // Line
            getElements().add(new MoveTo(srcX, srcY));
            getElements().add(new LineTo(destX, destY));

            // Arrow head at dest point
            drawArrowHead(srcX, srcY, destX, destY);

            // Move to src point
            getElements().add(new MoveTo(srcX, srcY));

            // Arrow head at src point
            drawArrowHead(destX, destY, srcX, srcY);

            edgeHolder.getChildren().addAll(this, srcRepPointText, destRepPointText);
            sociograph.addUndirectedEdge(srcVertex.nameText.getText(), destVertex.nameText.getText(), Double.parseDouble(srcRep), Double.parseDouble(destRep), rel);
            srcVertex.connectedEdges.add(this);

            System.out.println(sociograph + "\n");
        }

        public void showEdge() {
            canvasGroup.getChildren().add(edgeHolder);
        }

        public void setEdgeRelation(Relationship relationship) {
            this.rel = relationship;
            switch (relationship) {
                case FRIEND:
                    setFill(Color.BLUE);
                    break;
                case NONE:
                    setFill(Color.BLACK);
                    break;
                case ENEMY:
                    setFill(Color.RED);
                    break;
            }
        }

        public void changeSrcRepPoint(String newRepStr) {
            double repDouble = Double.parseDouble(newRepStr);
            this.srcRepPointText.setText("" + repDouble);
        }

        public void changeDestRepPoint(String newRepStr) {
            double repDouble = Double.parseDouble(newRepStr);
            this.destRepPointText.setText("" + repDouble);
        }

        private void setRepPointText(Text repPointText, String repStr, double x1, double y1, double x2, double y2) {
            double repDouble = Double.parseDouble(repStr);
            repPointText.setText("" + repDouble);
            // Math formula to find coordinate between two points, ratio (0.2 to 0.8)
            // (x, y) = (((m * x2) + (n * x1)) / (m + n) , ((m * y2) + (n * y1)) / (m + n))
            double resultX = ((0.2 * x2) + (0.8 * x1));
            double resultY = ((0.2 * y2) + (0.8 * y1));
            repPointText.setX((int) resultX);
            repPointText.setY((int) resultY);
        }

        private void computeSrcDestCoordinates(double srcX, double srcY, double destX, double destY) {
            double distance = Math.sqrt(Math.pow(destX - srcX , 2) + Math.pow(destY - srcY , 2));
            double m = 12;
            double n = distance - m;
            this.srcX = (m * destX + n * srcX) / distance;
            this.srcY = (m * destY + n * srcY) / distance;
            this.destX = (m * srcX + n * destX) / distance;
            this.destY = (m * srcY + n * destY) / distance;
        }

        private void drawArrowHead(double srcX, double srcY, double destX, double destY) {
            // Arrow head at dest point
            double angle = Math.atan2((destY - srcY), (destX - srcX)) - Math.PI / 2.0;
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);

            // point 1
            double x1 = (- 1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * ARROWHEADSIZE + destX;
            double y1 = (- 1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * ARROWHEADSIZE + destY;

            // point 2
            double x2 = (1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * ARROWHEADSIZE + destX;
            double y2 = (1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * ARROWHEADSIZE + destY;

            getElements().add(new LineTo(x1, y1));
            getElements().add(new LineTo(x2, y2));
            getElements().add(new LineTo(destX, destY));
        }
    }

    private List<Object> convertStudentInfoToTextFields(Student student) {
        String name = student.getName();
        double dive = student.getDive();
        LocalTime[] lunchStartArr = student.getLunchStart();
        int[] lunchPeriodArr = student.getLunchPeriod();
        LocalTime estimatedLunchEnd = student.getEstimatedLunchEnd();
        Map<String, Double> repPointsMap = student.getRepPoints();
        Set<Student> friendsSet = student.getFriends();
        Set<Student> enemiesSet = student.getEnemies();
        Set<Student> nonesSet = student.getNones();

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

        TextField nameTF = new TextField(name);
        TextField diveTF = new TextField(dive + "");
        TextField lunchStartTF = new TextField(lunchStart.toString());
        TextField lunchPeriodTF = new TextField(lunchPeriod.toString());
        TextField estimatedLunchEndTF = new TextField((estimatedLunchEnd == null) ? "Not calculated yet" : estimatedLunchEnd.toString());
        TextField friendsTF = new TextField(friends);
        TextField enemiesTF = new TextField(enemies);
        TextField nonesTF = new TextField(nones);
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

        return textFieldList;
    }

    private List<String> getStudentInfoAllFieldNames() {
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
        return labelNameList;
    }

    private Stage openDialogWindow(String filepath, String title) throws IOException {
        Stage newWindow = new Stage();
        Parent newRoot = FXMLLoader.load(getClass().getResource(filepath));
        Scene newScene = new Scene(newRoot);
        newScene.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());
        newWindow.setTitle(title);
        newWindow.setScene(newScene);
        newWindow.alwaysOnTopProperty();
        newWindow.initModality(Modality.APPLICATION_MODAL);
        newWindow.initStyle(StageStyle.UTILITY);
        return newWindow;
    }
}
