module com.devccv.popuprss {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;


    opens com.devccv.popuprss to javafx.fxml;
    opens com.devccv.popuprss.controller to javafx.fxml;
    exports com.devccv.popuprss;
    exports com.devccv.popuprss.util;
    opens com.devccv.popuprss.util to javafx.fxml;
    opens com.devccv.popuprss.widget to javafx.fxml;
}