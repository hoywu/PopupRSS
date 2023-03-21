package com.devccv.popuprss.controller;

import com.devccv.popuprss.bean.Record;
import com.devccv.popuprss.util.CSV;
import com.devccv.popuprss.util.ResourceBundleUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public final class ArchivedViewController implements Initializable {
    @FXML
    private TableView<Record> table;
    private ObservableList<Record> tableItems;
    public static Consumer<Record> newRecord;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //初始化table列
        TableColumn<Record, String> columnTime = new TableColumn<>(ResourceBundleUtil.getStringValue("table_time"));
        TableColumn<Record, String> columnLevel = new TableColumn<>(ResourceBundleUtil.getStringValue("table_level"));
        TableColumn<Record, String> columnLanguage = new TableColumn<>(ResourceBundleUtil.getStringValue("table_language"));
        TableColumn<Record, String> columnTitle = new TableColumn<>(ResourceBundleUtil.getStringValue("table_title"));
        TableColumn<Record, String> columnUnits = new TableColumn<>(ResourceBundleUtil.getStringValue("table_units"));
        TableColumn<Record, String> columnReward = new TableColumn<>(ResourceBundleUtil.getStringValue("table_reward"));
        columnTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        columnLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        columnLanguage.setCellValueFactory(new PropertyValueFactory<>("language"));
        columnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        columnUnits.setCellValueFactory(new PropertyValueFactory<>("units"));
        columnReward.setCellValueFactory(new PropertyValueFactory<>("reward"));
        columnTime.setPrefWidth(110.0);
        columnLevel.setPrefWidth(35.0);
        columnLanguage.setPrefWidth(50.0);
        columnTitle.setPrefWidth(220.0);
        columnUnits.setPrefWidth(35.0);
        columnReward.setPrefWidth(55.0);
        table.getColumns().addAll(columnTime, columnLevel, columnLanguage, columnTitle, columnUnits, columnReward);

        //增加鼠标悬停Tooltip
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Record item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    this.setTooltip(new Tooltip(item.getDescription()));
                }
            }
        });

        //添加历史记录
        table.getItems().addAll(CSV.readArchived());

        //新记录添加监听器
        table.itemsProperty().addListener((observable, oldValue, newValue) -> {
            table.scrollTo(newValue.size() - 1);
            CSV.appendArchived(newValue.get(newValue.size() - 1));
        });

        //构造供外部调用的添加记录方法
        tableItems = table.getItems();
        newRecord = record -> tableItems.add(record);
    }
}
