package com.foloke.ardconn;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
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

import java.awt.*;
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

    public Manager manager;

    public AnimatedWall animatedWall;

    ParallelTransition loadParallelTransition;

    Rectangle blurRectangle;

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
        blurRectangle = new Rectangle(1000, 100);
        blurRectangle.widthProperty().bind(upperAnchorPane.widthProperty());
        blurRectangle.heightProperty().bind(upperAnchorPane.heightProperty());
        blurRectangle.fillProperty().set(Color.TRANSPARENT);

        preparingVBox.setEffect(gaussianBlur);

        hbox = new HBox();
        hbox.setFillHeight(true);
        hbox.setAlignment(Pos.CENTER);
        hbox.prefHeightProperty().bind(upperAnchorPane.heightProperty());
        hbox.prefWidthProperty().bind(upperAnchorPane.widthProperty());

        SimpleDoubleProperty offsetValue = new SimpleDoubleProperty();
        offsetValue.set(1);

        Button hitButton = new Button("HIT");
        hitButton.setPrefWidth(50);
        HBox.setMargin(hitButton, new Insets(0, 10, 0, 0));
        Button missButton = new Button("MISS");
        missButton.setPrefWidth(50);

        hitButton.setOnMouseClicked(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = Launcher.class.getClassLoader().getResource("main.fxml");
            fxmlLoader.setLocation(url);
            try {
                VBox mainLayout = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            RecordsController recordsController = fxmlLoader.getController();

            recordsController.manager = manager;

        });
        hbox.getChildren().addAll(hitButton, missButton);
        upperAnchorPane.getChildren().addAll(blurRectangle, hbox);

        hitButton.setOnMouseClicked(event -> closeHitDialog());

        //box.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));

        blurRectangle.setMouseTransparent(true);
        hbox.setMouseTransparent(true);

        blurPt = new ParallelTransition();

        FillTransition ft2 = new FillTransition();
        ft2.setShape(blurRectangle);
        ft2.setDuration(Duration.seconds(1));
        ft2.setFromValue(Color.TRANSPARENT);
        ft2.setToValue(new Color(1, 1, 1, 0.25));

        Timeline timeline = new Timeline();
        KeyValue keyValue1 = new KeyValue(gaussianBlur.radiusProperty(), 0);
        KeyValue keyValue2 = new KeyValue(gaussianBlur.radiusProperty(), 10);
        KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(0), keyValue1);
        KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(1), keyValue2);
        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);

        Timeline timeline1 = new Timeline();
        KeyValue keyValue3 = new KeyValue(offsetValue, 1);
        KeyValue keyValue4 = new KeyValue(offsetValue, 0);
        KeyFrame keyFrame3 = new KeyFrame(Duration.seconds(0), keyValue3);
        KeyFrame keyFrame4 = new KeyFrame(Duration.seconds(1), keyValue4);
        timeline.getKeyFrames().addAll(keyFrame3, keyFrame4);

        blurPt.getChildren().addAll(ft2, timeline, timeline1);
        blurPt.setAutoReverse(true);
        blurPt.setRate(1.25);

        hbox.translateYProperty().bind(offsetValue.multiply(upperAnchorPane.heightProperty().negate()));

        reloadButtonRect.heightProperty().bind(reloadButton.heightProperty().add(5));
        reloadButtonRect.widthProperty().bind(reloadButton.widthProperty());
        reloadButtonRect.layoutYProperty().set(5);

        disarmButtonRect.heightProperty().bind(disarmButton.heightProperty().add(5));
        disarmButtonRect.widthProperty().bind(disarmButton.widthProperty());
        disarmButtonRect.layoutYProperty().set(5);

        shootButtonRect.heightProperty().bind(shootButton.heightProperty().add(5));
        shootButtonRect.widthProperty().bind(shootButton.widthProperty());
        shootButtonRect.layoutYProperty().set(5);
    }

    @FXML
    Button reloadButton;

    @FXML
    Pane reloadButtonPane;

    @FXML
    Rectangle reloadButtonRect;

    HBox hbox;
    GaussianBlur gaussianBlur;
    ParallelTransition blurPt;

    public void openHitDialog() {
        blurRectangle.setMouseTransparent(false);
        hbox.setMouseTransparent(false);
        blurPt.stop();
        blurPt.setCycleCount(1);
        blurPt.playFromStart();
    }

    public void closeHitDialog() {
        blurPt.stop();
        blurPt.setCycleCount(2);
        blurRectangle.setMouseTransparent(true);
        hbox.setMouseTransparent(true);
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

    @FXML
    private void shootCommand() {
        manager.sendShootCommand();
    }

    @FXML
    private void reloadCommand() {
        manager.sendReloadCommand();
    }

    @FXML
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

    @FXML
    public void reloadPush() {
        reloadButton.setLayoutY(5);
    }

    @FXML
    public void reloadRelease() {
        reloadButton.setLayoutY(0);
    }

    @FXML
    public void reloadHover() {
        reloadButton.setLayoutY(-2);
    }

    @FXML
    public void reloadMoved() {
        reloadButton.setLayoutY(0);
    }

    @FXML
    Button disarmButton;

    @FXML
    Rectangle disarmButtonRect;

    @FXML
    public void disarmPush() {
        disarmButton.setLayoutY(5);
    }

    @FXML
    public void disarmRelease() {
        disarmButton.setLayoutY(0);
    }

    @FXML
    public void disarmHover() {
        disarmButton.setLayoutY(-2);
    }

    @FXML
    public void disarmMoved() {
        disarmButton.setLayoutY(0);
    }

    @FXML
    Button shootButton;

    @FXML
    Rectangle shootButtonRect;

    @FXML
    public void shootPush() {
        shootButton.setLayoutY(5);
    }

    @FXML
    public void shootRelease() {
        shootButton.setLayoutY(0);
    }

    @FXML
    public void shootHover() {
        shootButton.setLayoutY(-2);
    }

    @FXML
    public void shootMoved() {
        shootButton.setLayoutY(0);
    }
}
