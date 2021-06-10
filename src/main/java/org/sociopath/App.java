package org.sociopath;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

public class App extends Application {

    private static Stage primaryStage;
    public static Stack<Scene> sceneManager = new Stack<>();

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setResizable(false);

        Parent root = FXMLLoader.load(getClass().getResource("fxml/start.fxml"));

        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());

        sceneManager.push(scene);
        primaryStage.setTitle("Sociopath");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

}