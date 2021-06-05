package org.sociopath.controllers;

import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.sociopath.App;
import org.sociopath.models.Sociograph;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphSimulationController {
    private final Stage primaryStage = App.getPrimaryStage();
    public ToggleButton addStudentBtn;
    public Group canvasGroup;

    public Sociograph sociograph = new Sociograph();
    public List<VertexFX> allCircles = new ArrayList<>();
    public Pane viewer;

    @FXML
    public void backToMenuHandler(ActionEvent actionEvent) {
        App.sceneManager.pop();
        primaryStage.setScene(App.sceneManager.peek());
    }

    @FXML
    public void canvasHandler(MouseEvent mouseEvent) throws IOException {
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

    EventHandler<MouseEvent> mouseHandler = event -> {

        VertexFX circle = (VertexFX) event.getSource();
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.getButton() == MouseButton.PRIMARY) {
            FillTransition fillTransition = new FillTransition(Duration.millis(300), circle, Color.RED, Color.BLACK);
            fillTransition.play();
        }
    };

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

    public class VertexFX extends Circle {
        Point coordinate;
        Label nameLabel;

        public VertexFX(double x, double y, double radius, String name) {
            super(x, y, radius);

            this.coordinate = new Point((int) x, (int) y);
            this.nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-weight: bold");
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
            this.setOnContextMenuRequested(e -> {
                menu.show(this, e.getScreenX(), e.getScreenY());
            });

            System.out.println(sociograph + "\n");
        }

        public void showVertex() {
            StackPane stackPane = new StackPane();
            stackPane.setLayoutX(this.coordinate.x);
            stackPane.setLayoutY(this.coordinate.y);
            stackPane.getChildren().addAll(nameLabel, this);
            canvasGroup.getChildren().add(stackPane);

            ScaleTransition tr = new ScaleTransition(Duration.millis(100), this);
            tr.setByX(10f);
            tr.setByY(10f);
            tr.setInterpolator(Interpolator.EASE_OUT);
            tr.play();
        }
    }

}
