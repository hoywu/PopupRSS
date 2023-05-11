module com.devccv.popuprss {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;
    requires jdk.accessibility;

    opens com.devccv.popuprss to javafx.fxml, javafx.graphics;
    opens com.devccv.popuprss.bean to javafx.base;
    opens com.devccv.popuprss.controller to javafx.fxml;
    opens com.devccv.popuprss.util to javafx.fxml;
}