package com.foloke.ardconnd;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;

public class HitDialogController extends HiddenUI {

    @FXML
    CoolButtonController missButtonController;

    @FXML
    CoolButtonController hitButtonController;

    @FXML
    public void initialize() {
        super.initialize();
        hitButtonController.setColor(Color.web("#73e82b"));
        missButtonController.setColor(Color.web("#ff2828"));

        missButtonController.setText("MISS");
        hitButtonController.setText("HIT");
    }

    public void setMissAction(CoolButtonController.Action action) {
        missButtonController.setAction(action);
    }

    public void setHitAction(CoolButtonController.Action action) {
        hitButtonController.setAction(action);
    }

}
