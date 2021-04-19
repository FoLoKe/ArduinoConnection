package com.foloke.ardconn;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class RecordsController extends HiddenUI {
    Manager manager;

    @FXML
    Label distanceLabel;

    @FXML
    TextField nameTextField;

    @FXML
    ListView<String> recordsListView;

    @FXML
    CoolButtonController saveBtnController;

    @FXML
    CoolButtonController cancelBtnController;

    @FXML
    protected void initialize() {
        super.initialize();

        saveBtnController.setColor(Color.web("#73e82b"));
        cancelBtnController.setColor(Color.web("#ff2828"));

        saveBtnController.setText("SAVE");
        cancelBtnController.setText("CANCEL");
    }

    public void setCancelAction(CoolButtonController.Action action) {
        cancelBtnController.setAction(action);
    }

    public void setSaveAction(CoolButtonController.Action action) {
        saveBtnController.setAction(action);
    }

    public String getName() {
        return nameTextField.getText();
    }

    public void setDistance(String test) {
        distanceLabel.setText(test);
    }

    public void lock(boolean state) {
        if(state) {
            cancelBtnController.lock();
            saveBtnController.lock();
        } else {
            cancelBtnController.unlock();
            saveBtnController.unlock();
        }
    }
}
