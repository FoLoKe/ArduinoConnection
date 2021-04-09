package com.foloke.ardconn;

import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class AnimatedWall extends Pane {
    private final Rectangle wallRect;
    private final Pane animatedWallPane;
    public final Label distanceLabel;
    private final Pane root;

    private final UIController uiController;

    SequentialTransition sq;

    public AnimatedWall(Pane container, UIController controller) {
        super();
        root = container;
        this.uiController = controller;

        animatedWallPane = new Pane();
        distanceLabel = new Label("CHECK DISTANCE");

        distanceLabel.setTextFill(Color.WHITE);


        wallRect = new Rectangle(100, 100);
        wallRect.setArcHeight(50);
        wallRect.setArcWidth(50);
        wallRect.setFill(Color.PALEVIOLETRED);

        RotateTransition wrt1 = new RotateTransition();
        wrt1.setDuration(Duration.seconds(0.5));
        wrt1.setNode(animatedWallPane);
        wrt1.setToAngle(-45);

        RotateTransition wrt2 = new RotateTransition();
        wrt2.setDuration(Duration.seconds(0.75));
        wrt2.setNode(animatedWallPane);
        wrt2.setAxis(new Point3D(0, 0, 1));
        wrt2.setToAngle(390);

        RotateTransition wrt3 = new RotateTransition();
        wrt3.setDuration(Duration.seconds(0.25));
        wrt3.setNode(animatedWallPane);
        wrt3.setAutoReverse(true);
        wrt3.setToAngle(360);

        sq = new SequentialTransition();
        sq.getChildren().addAll(wrt1, wrt2, wrt3);
        sq.setCycleCount(1);

        animatedWallPane.getChildren().addAll(wallRect, distanceLabel);
        animatedWallPane.layoutXProperty().bind(
                root.widthProperty()
                .subtract(animatedWallPane.widthProperty())
                        .divide(2)
        );

        animatedWallPane.layoutYProperty().bind(
                root.heightProperty()
                        .subtract(animatedWallPane.heightProperty())
                        .divide(2)
        );

        SimpleDoubleProperty simpleDoubleProperty = new SimpleDoubleProperty();
        simpleDoubleProperty.bind(root.heightProperty().divide(root.widthProperty()));

        animatedWallPane.prefWidthProperty().bind(
                root.widthProperty().multiply(simpleDoubleProperty).subtract(root.heightProperty().divide(4))
        );

        animatedWallPane.prefHeightProperty().bind(
                root.heightProperty().subtract(root.heightProperty().divide(4))
        );

        wallRect.widthProperty().bind(animatedWallPane.widthProperty());
        wallRect.heightProperty().bind(animatedWallPane.heightProperty());

        root.getChildren().addAll(animatedWallPane);

        animatedWallPane.widthProperty().addListener((obs, old, newVal) -> {
            distanceLabel.setLayoutX((newVal.doubleValue() - distanceLabel.getWidth()) / 2);
            distanceLabel.setLayoutY((animatedWallPane.getPrefHeight() - distanceLabel.getHeight()) / 2);

            distanceLabel.maxWidthProperty().set(wallRect.getWidth() - 10);
            calculateTextSize(distanceLabel.getText());
        });

        distanceLabel.widthProperty().addListener((obs, old, newVal) -> {
            distanceLabel.setLayoutX((animatedWallPane.getPrefWidth() - distanceLabel.getWidth()) / 2);
        });
        distanceLabel.heightProperty().addListener((obs, old, newVal) -> {
            distanceLabel.setLayoutY((animatedWallPane.getPrefHeight() - distanceLabel.getHeight()) / 2);
        });

        distanceLabel.setMouseTransparent(true);

        sq.setOnFinished(observable -> {
            animatedWallPane.rotateProperty().set(0);
            if(uiController.block) {
                animate();
            }
        });

        distanceLabel.setFont(df);
        distanceLabel.setTextOverrun(OverrunStyle.CLIP);

        //distanceLabel.setStyle("-fx-font-weight: bold");

        distanceLabel.textProperty().addListener((observable, old, nevVal) -> {
            calculateTextSize(nevVal);
        });
    }

    private void calculateTextSize(String string) {
        Text text = new Text(string);
        text.setFont(df);
        //text.setStyle("-fx-font-weight: bold");
        double textWidth = text.getLayoutBounds().getWidth();
        if(textWidth <= distanceLabel.maxWidthProperty().get() - 10) {
            distanceLabel.setFont(df);
        } else {
            double newSize = 64 * (distanceLabel.maxWidthProperty().get() - 10) / textWidth;
            distanceLabel.setFont(Font.font(df.getFamily(), FontWeight.BOLD, newSize));
        }
    }

    Font df = Font.font("System", FontWeight.BOLD,64);

    public void setText(String string) {
        distanceLabel.setText(string);
    }

    public void animate() {
        sq.setCycleCount(1);
        sq.play();
    }

    public void setActualMouseEvent(EventHandler<? super MouseEvent> eventHandler) {
        wallRect.setOnMouseClicked(eventHandler);
    }
}
