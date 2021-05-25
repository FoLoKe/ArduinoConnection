package com.foloke.ardconnd;

import com.foloke.ardconn.Manager;
import com.foloke.ardconn.Record;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.List;

public class RecordsController extends HiddenUI {
    @FXML
    Label distanceLabel;

    @FXML
    TextField angleTextField;

    @FXML
    ChoiceBox<String> shellChoiceBox;

    @FXML
    ListView<Record> recordsListView;

    @FXML
    CoolButtonController saveBtnController;

    @FXML
    CoolButtonController cancelBtnController;

    @FXML
    protected void initialize() {
        super.initialize();

        saveBtnController.setColor(Color.web("#73e82b"));
        cancelBtnController.setColor(Color.web("#ff2828"));

        saveBtnController.setText("ДОБАВИТЬ");
        cancelBtnController.setText("ОТМЕНА");

        ObservableList<String> list = FXCollections.observableArrayList(Manager.shells);
        shellChoiceBox.setItems(list);
        shellChoiceBox.setValue(list.get(0));
    }

    public void setCancelAction(CoolButtonController.Action action) {
        cancelBtnController.setAction(action);
    }

    public void setSaveAction(CoolButtonController.Action action) {
        saveBtnController.setAction(action);
    }

    public Float getAngle() {
        String value = angleTextField.getText();
        Float floatValue = null;
        if (value.matches("\\d*") && value.length() > 0) {
            floatValue = Float.parseFloat(value);
        }

        return floatValue;
    }

    public String getChoice() {
        return shellChoiceBox.getValue();
    }

    public void setDistance(float distance) {
        distanceLabel.setText(distance / 100f + " м");
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

    public void setRecords(List<Record> records) {
        ObservableList<Record> list = FXCollections.observableList(records);
        recordsListView.setItems(list);
    }
}
