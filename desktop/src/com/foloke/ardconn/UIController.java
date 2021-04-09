package com.foloke.ardconn;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.TextArea;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UIController {
    @FXML
    public TextArea log;

    @FXML
    public Pane loadIconPane;

    @FXML
    public Pane wallIconPane;

    @FXML
    private AnchorPane upperAnchorPane;

    @FXML
    public VBox preparingVBox;

    @FXML
    public Menu comMenu;

    @FXML
    public AnchorPane hitDialog;

    @FXML
    public HitDialogController hitDialogController;

    public Manager manager;

    public AnimatedWall animatedWall;

    ParallelTransition loadParallelTransition;

    @FXML
    private void initialize() {
        Rectangle rect = new Rectangle(15, 15);
        rect.setX(2.5);
        rect.setY(2.5);
        rect.arcHeightProperty().set(20);
        rect.arcWidthProperty().set(10);
        rect.setFill(Color.GREENYELLOW);

        RotateTransition rt = new RotateTransition();
        rt.setNode(rect);

        loadIconPane.getChildren().addAll(rect);

        rt.setAxis(new Point3D(0, 1, 1));
        rt.setDuration(Duration.seconds(3));
        rt.setFromAngle(0);
        rt.setToAngle(720);
        rt.setAutoReverse(true);

        FillTransition ft = new FillTransition();
        ft.setShape(rect);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.setDuration(Duration.seconds(1.5));
        ft.setFromValue(Color.GREENYELLOW);
        ft.setToValue(Color.PINK);
        ft.play();

        loadParallelTransition = new ParallelTransition();
        loadParallelTransition.getChildren().addAll(rt, ft);
        loadParallelTransition.play();

        loadParallelTransition.setOnFinished(event -> startLoadAnimation());

        animatedWall = new AnimatedWall(wallIconPane, this);
        animatedWall.setMouseTransparent(false);

        animatedWall.setActualMouseEvent(event -> {
            animatedWall.animate();
            manager.sendCheckDistanceCommand();
            System.out.println("click");
        });

        gaussianBlur = new GaussianBlur(0);

        preparingVBox.setEffect(gaussianBlur);

        Timeline timeline = new Timeline();
        KeyValue keyValue1 = new KeyValue(gaussianBlur.radiusProperty(), 0);
        KeyValue keyValue2 = new KeyValue(gaussianBlur.radiusProperty(), 10);
        KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(0), keyValue1);
        KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(1), keyValue2);
        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);
        blurPt.getChildren().add(timeline);

//        hitButton.setOnMouseClicked(event -> {
//            FXMLLoader fxmlLoader = new FXMLLoader();
//            URL url = Launcher.class.getClassLoader().getResource("records.fxml");
//            fxmlLoader.setLocation(url);
//            try {
//                VBox mainLayout = fxmlLoader.load();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//
//            RecordsController recordsController = fxmlLoader.getController();
//
//            recordsController.manager = manager;
//
//        });
//
//        hbox.getChildren().addAll(hitButton, missButton);
//        upperAnchorPane.getChildren().addAll(blurRectangle, hbox);
//
//        hitButton.setOnMouseClicked(event -> closeHitDialog());

        //box.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));

        //hitDialog.setMouseTransparent(true);
        hitDialogController.bind(upperAnchorPane);

        reloadCoolBtnController.setAction(this::reloadCommand);
        disarmCoolBtnController.setAction(this::disarmCommand);
        shootCoolBtnController.setAction(this::shootCommand);

        reloadCoolBtnController.setColor(Color.web("#ff9400"));
        disarmCoolBtnController.setColor(Color.web("#73e82b"));
        shootCoolBtnController.setColor(Color.web("#ff2828"));

        reloadCoolBtnController.setText("RELOAD");
        disarmCoolBtnController.setText("DISARM");
        shootCoolBtnController.setText("SHOOT");

        hitDialogController.setMissAction(this::closeHitDialog);
    }

    @FXML
    CoolButtonController reloadCoolBtnController;

    @FXML
    CoolButtonController disarmCoolBtnController;

    @FXML
    CoolButtonController shootCoolBtnController;

    GaussianBlur gaussianBlur;
    ParallelTransition blurPt = new ParallelTransition();

    public void openHitDialog() {
        hitDialogController.open();
        blurPt.stop();
        blurPt.setCycleCount(1);
        blurPt.playFromStart();
    }

    public void closeHitDialog() {
        blurPt.stop();
        blurPt.setCycleCount(2);
        blurPt.setAutoReverse(true);
        hitDialogController.close();
        blurPt.playFrom(Duration.seconds(1));
    }

    public void startLoadAnimation() {
        if(block) {
            loadParallelTransition.setCycleCount(1);
            loadParallelTransition.play();
        }
    }

    public boolean block = false;

    public void block() {
        block = true;
        startLoadAnimation();
    }

    public void unblock() {
        block = false;
    }

    private void shootCommand() {
        manager.sendShootCommand();
    }

    private void reloadCommand() {
        manager.sendReloadCommand();
    }

    private void disarmCommand() {
        manager.sendDisarmCommand();
    }

    ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private void menuOpen() {
        String[] ports = manager.getPorts();

        if(ports.length > 0) {
            ObservableList<String> list = FXCollections.observableArrayList();
            Collections.addAll(list, ports);
            Platform.runLater(() -> {
                Toggle prev = toggleGroup.getSelectedToggle();
                String prevName = null;
                if(prev instanceof RadioMenuItem) {
                    prevName = ((RadioMenuItem) prev).getText();
                }
                comMenu.getItems().clear();
                toggleGroup.getToggles().clear();

                List<RadioMenuItem> items = new ArrayList<>();
                for (String port : list) {
                    RadioMenuItem radioMenuItem = new RadioMenuItem(port);
                    radioMenuItem.setToggleGroup(toggleGroup);
                    radioMenuItem.setOnAction(event -> manager.connect(radioMenuItem.getText()));
                    if(port.equals(prevName)) {
                        toggleGroup.selectToggle(radioMenuItem);
                    }
                    items.add(radioMenuItem);
                }

                comMenu.getItems().addAll(items);
            });
        }
    }
}
