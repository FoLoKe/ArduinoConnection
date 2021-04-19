package com.foloke.ardconn;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class CoolButtonController {
    @FXML
    Button actualButton;

    @FXML
    Rectangle backgroundPane;

    public interface Action {
        void act();
    }

    private Action action;

    private Color initialColor;

    @FXML
    private void initialize() {
        initialColor = Color.LIGHTBLUE;
    }

    @FXML
    public void preformAction() {

        if(action != null) {
            action.act();
        }
    }

    @FXML
    public void beginOver() {
        actualButton.setLayoutY(2);
    }

    @FXML
    public void endOver() {
        actualButton.setLayoutY(0);
    }

    @FXML
    public void pressBegin() {
        actualButton.setLayoutY(5);
    }

    @FXML
    public void pressOver() {
        actualButton.setLayoutY(0);
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setText(String text) {
        actualButton.setText(text);
    }

    public void setColor(Color color) {
        initialColor = color;
        actualButton.setStyle(actualButton.getStyle().concat("-fx-background-color: " + toRgba(color, color.getOpacity())));
        backgroundPane.setFill(color.darker());
        //actualButton.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
    }

    private String toRgba(Color color, double opacity) {
        int r = (int) (255 * color.getRed());
        int g = (int) (255 * color.getGreen());
        int b = (int) (255 * color.getBlue());
        int a = (int) (255 * opacity);
        return String.format("#%02x%02x%02x%02x", r, g, b, a);
    }

    public void lock() {
        actualButton.setDisable(true);
        backgroundPane.setFill(Color.web(toRgba(initialColor.darker(), 0.25)));
    }

    public void unlock() {
        actualButton.setDisable(false);
        backgroundPane.setFill(initialColor.darker());
    }
}
