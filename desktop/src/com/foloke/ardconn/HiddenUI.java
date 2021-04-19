package com.foloke.ardconn;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class HiddenUI {
    @FXML
    AnchorPane rootAnchor;

    private ParallelTransition animation;
    private final SimpleDoubleProperty offsetValue = new SimpleDoubleProperty();

    @FXML
    protected void initialize() {
        animation = new ParallelTransition();

        offsetValue.set(1);

        rootAnchor.setMouseTransparent(false);

        Timeline timeline = new Timeline();
        KeyValue keyValue3 = new KeyValue(offsetValue, 1);
        KeyValue keyValue4 = new KeyValue(offsetValue, 0);
        KeyFrame keyFrame3 = new KeyFrame(Duration.seconds(0), keyValue3);
        KeyFrame keyFrame4 = new KeyFrame(Duration.seconds(1), keyValue4);
        timeline.getKeyFrames().addAll(keyFrame3, keyFrame4);

        animation.getChildren().addAll(timeline);
        animation.setAutoReverse(true);
        animation.setRate(1.25);
    }

    public void open() {
        rootAnchor.setMouseTransparent(false);
        animation.stop();
        animation.setCycleCount(1);
        animation.playFromStart();
    }

    public void close() {
        animation.stop();
        animation.setCycleCount(2);
        rootAnchor.setMouseTransparent(true);
        animation.playFrom(Duration.seconds(1));
    }


    public void bind(AnchorPane pane) {
        rootAnchor.translateYProperty().bind(offsetValue.multiply(pane.heightProperty().negate()));
        rootAnchor.prefHeightProperty().bind(pane.heightProperty());
    }
}
