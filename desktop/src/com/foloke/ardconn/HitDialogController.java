package com.foloke.ardconn;

import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class HitDialogController {

    @FXML
    AnchorPane rootAnchor;

    @FXML
    CoolButtonController missButtonController;

    @FXML
    CoolButtonController hitButtonController;

    ParallelTransition animation;
    SimpleDoubleProperty offsetValue = new SimpleDoubleProperty();

    @FXML
    public void initialize() {

        animation = new ParallelTransition();

        offsetValue.set(1);

        rootAnchor.setMouseTransparent(false);
        //FillTransition ft2 = new FillTransition();
        //rootAnchor.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));

//        System.out.println(rootAnchor.centerShapeProperty());
//        ft2.setShape(rootAnchor.centerShapeProperty().v);
//        ft2.setDuration(Duration.seconds(1));
//        ft2.setFromValue(Color.TRANSPARENT);
//        ft2.setToValue(new Color(1, 1, 1, 0.25));

        Timeline timeline = new Timeline();
        KeyValue keyValue3 = new KeyValue(offsetValue, 1);
        KeyValue keyValue4 = new KeyValue(offsetValue, 0);
        KeyFrame keyFrame3 = new KeyFrame(Duration.seconds(0), keyValue3);
        KeyFrame keyFrame4 = new KeyFrame(Duration.seconds(1), keyValue4);
        timeline.getKeyFrames().addAll(keyFrame3, keyFrame4);

        animation.getChildren().addAll(timeline);
        animation.setAutoReverse(true);
        animation.setRate(1.25);

        hitButtonController.setColor(Color.web("#73e82b"));
        missButtonController.setColor(Color.web("#ff2828"));

        missButtonController.setText("MISS");
        hitButtonController.setText("HIT");
    }

    public void bind(AnchorPane pane) {
        rootAnchor.translateYProperty().bind(offsetValue.multiply(pane.heightProperty().negate()));
        rootAnchor.prefHeightProperty().bind(pane.heightProperty());
    }

    public void open() {
        rootAnchor.setMouseTransparent(false);
        //hbox.setMouseTransparent(false);
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

    public void setMissAction(CoolButtonController.Action action) {
        missButtonController.setAction(action);
    }

    public void setHitAction(CoolButtonController.Action action) {
        missButtonController.setAction(action);
    }

}
