package org.sociopath.controllers;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.sociopath.App;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    private MediaView backgroundMedia;

    private final Stage primaryStage = App.getPrimaryStage();
    public static GraphSimulationController canvasRef;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playVideo("src/main/resources/org/sociopath/style/minecraft-background.mp4");
    }

    public void startHandler(ActionEvent actionEvent) throws IOException {
        Parent mainMenuRoot = FXMLLoader.load(getClass().getResource("../fxml/mainMenu.fxml"));
        Scene mainMenuScene = new Scene(mainMenuRoot);
        mainMenuScene.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());
        App.sceneManager.push(mainMenuScene);
        primaryStage.setScene(mainMenuScene);
    }

    public void quitHandler(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void loadSampleHandler(ActionEvent actionEvent) throws IOException {
        startSimulation();
    }

    public void startFromScratchHandler(ActionEvent actionEvent) throws IOException {
        startSimulation();
    }

    public void backToStartHandler(ActionEvent actionEvent) {
        App.sceneManager.pop();
        primaryStage.setScene(App.sceneManager.peek());
    }

    private void playVideo(String fileLocation) {
        Media media = new Media(new File(fileLocation).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        if (backgroundMedia == null) {
            backgroundMedia = new MediaView(mediaPlayer);
        }
        backgroundMedia.setPreserveRatio(false);
        backgroundMedia.fitWidthProperty().bind(stackPane.widthProperty());
        backgroundMedia.fitHeightProperty().bind(stackPane.heightProperty());
        backgroundMedia.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
    }

    private void startSimulation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/graph_simulation.fxml"));
        Parent simulationRoot = loader.load();
        Scene simulationScene = new Scene(simulationRoot);
        simulationScene.getStylesheets().add(getClass().getResource("../style/style.css").toExternalForm());

        canvasRef = loader.getController();

        App.sceneManager.push(simulationScene);
        primaryStage.setScene(simulationScene);
    }


}
