package com.foloke.ardconn;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

public class Launcher extends Application implements UI {
    public static void main(String[] args) {
        Launcher.launch();
    }

    public Launcher() {

    }

    UIController uiController;
    Manager manager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = Launcher.class.getClassLoader().getResource("main.fxml");
        fxmlLoader.setLocation(url);
        VBox mainLayout = fxmlLoader.load();

        uiController = fxmlLoader.getController();

        manager = new Manager(this);
        uiController.manager = manager;

        primaryStage.setMinHeight(300);
        primaryStage.setMinWidth(300);
        primaryStage.setScene(new Scene(mainLayout));
        primaryStage.show();
        output("ready to connect\n");
    }

    public void output(String string) {
        System.out.println(string);
        Platform.runLater(() -> {
            if(uiController.log != null) {
                uiController.log.appendText(string);
            }
        });
    }

    @Override
    public void stop() {
        try {
            output("closing");
            manager.disconnect();
            Platform.exit();
        } catch (Exception e) {
            output(e.toString());
        }
    }

    @Override
    public void showOnWall(String value) {
        Platform.runLater(() -> uiController.animatedWall.setText(value));
    }

    @Override
    public void block() {
        uiController.block();
    }

    @Override
    public void unblock() {
        uiController.unblock();
    }

    @Override
    public void askForHit() {
        uiController.openHitDialog();
    }
}
