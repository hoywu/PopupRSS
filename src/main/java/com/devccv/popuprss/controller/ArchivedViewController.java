package com.devccv.popuprss.controller;

import com.devccv.popuprss.bean.Record;
import com.devccv.popuprss.util.ResourceBundleUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ArchivedViewController implements Initializable {
    @FXML
    private TableView<Record> table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //初始化table列
        TableColumn<Record, String> columnTime = new TableColumn<>(ResourceBundleUtil.getStringValue("table_time"));
        TableColumn<Record, String> columnLevel = new TableColumn<>(ResourceBundleUtil.getStringValue("table_level"));
        TableColumn<Record, String> columnLanguage = new TableColumn<>(ResourceBundleUtil.getStringValue("table_language"));
        TableColumn<Record, String> columnDescription = new TableColumn<>(ResourceBundleUtil.getStringValue("table_description"));
        TableColumn<Record, String> columnUnits = new TableColumn<>(ResourceBundleUtil.getStringValue("table_units"));
        TableColumn<Record, String> columnReward = new TableColumn<>(ResourceBundleUtil.getStringValue("table_reward"));
        columnTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        columnLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        columnLanguage.setCellValueFactory(new PropertyValueFactory<>("language"));
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnUnits.setCellValueFactory(new PropertyValueFactory<>("units"));
        columnReward.setCellValueFactory(new PropertyValueFactory<>("reward"));
        columnTime.setPrefWidth(110.0);
        columnLevel.setPrefWidth(35.0);
        columnLanguage.setPrefWidth(50.0);
        columnDescription.setPrefWidth(220.0);
        columnUnits.setPrefWidth(35.0);
        columnReward.setPrefWidth(55.0);
        table.getColumns().addAll(columnTime, columnLevel, columnLanguage, columnDescription, columnUnits, columnReward);

        //增加鼠标悬停Tooltip
        table.setRowFactory(tv -> {
            TableRow<Record> row = new TableRow<>() {
                @Override
                protected void updateItem(Record item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        this.setTooltip(new Tooltip(item.getDescription()));
                    }
                }
            };
            return row;
        });
    }
}
