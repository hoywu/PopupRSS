package com.devccv.popuprss.controller;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.Encrypt;
import com.devccv.popuprss.util.ResourceBundleUtil;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ResourceBundle;

public final class SettingsController implements Initializable {
    @FXML
    private MFXTextField statusField;
    @FXML
    private MFXTextField version;
    @FXML
    private MFXComboBox<String> languageCombo;
    @FXML
    private MFXPasswordField rssField;
    @FXML
    private ImageView lockIcon;
    @FXML
    private MFXTextField checkDelayField;
    @FXML
    private MFXRadioButton proxyNo;
    @FXML
    private MFXRadioButton proxyHTTP;
    @FXML
    private MFXRadioButton proxySOCKS;
    @FXML
    private MFXTextField proxyURLField;
    @FXML
    private Label proxyErrorLabel;
    @FXML
    private Label delayErrorLabel;
    @FXML
    private MFXCheckbox autoStartCheckbox;
    @FXML
    private MFXCheckbox autoPopupCheckbox;
    @FXML
    private MFXRectangleToggleNode saveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //设置图标
        lockIcon.setImage(new Image(ResourcesLoader.loadStream("icon/lock.png")));
        ImageView icon = new ImageView(new Image(ResourcesLoader.loadStream("icon/save.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        saveButton.setLabelLeadingIcon(icon);

        //填充配置并设置激活保存按钮监听器
        //RSS链接
        String rssLink = ConfigManager.CONFIG.getRssLink();
        if (!rssLink.isBlank()) {
            Platform.runLater(() -> {
                try {
                    rssField.setText(Encrypt.decryptWithUserName(rssLink));
                } catch (Exception e) {
                    LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_decryption_error"));
                }
            });
        }
        rssField.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(false));

        //语言
        languageCombo.getItems().setAll(List.of("Chinese", "English"));
        if ("Chinese".equals(ConfigManager.CONFIG.getLanguage())) {
            languageCombo.selectFirst();
        } else {
            languageCombo.selectLast();
        }
        languageCombo.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(false));

        //代理
        ToggleGroup toggleGroup = new ToggleGroup();
        proxyNo.setToggleGroup(toggleGroup);
        proxyHTTP.setToggleGroup(toggleGroup);
        proxySOCKS.setToggleGroup(toggleGroup);
        proxyNo.setGap(3.0);
        proxyHTTP.setGap(3.0);
        proxySOCKS.setGap(3.0);
        proxyNo.setOnAction(event -> {
            saveButton.setDisable(false);
            proxyURLField.setDisable(true);
            proxyErrorLabel.setVisible(false);
        });
        proxyHTTP.setOnAction(event -> {
            saveButton.setDisable(false);
            proxyURLField.setDisable(false);
            checkProxyValid();
        });
        proxySOCKS.setOnAction(event -> {
            saveButton.setDisable(false);
            proxyURLField.setDisable(false);
            checkProxyValid();
        });
        switch (ConfigManager.CONFIG.getProxy()) {
            case "HTTP" -> proxyHTTP.setSelected(true);
            case "SOCKS" -> proxySOCKS.setSelected(true);
            default -> {
                proxyNo.setSelected(true);
                proxyURLField.setDisable(true);
            }
        }
        proxyURLField.setText(ConfigManager.CONFIG.getProxyURL());
        proxyURLField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(false);
            checkProxyValid();
        });

        //检查间隔
        checkDelayField.setText(String.valueOf(ConfigManager.CONFIG.getCheckDelay()));
        checkDelayValid();
        checkDelayField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(false);
            checkDelayValid();
        });

        //复选框
        autoStartCheckbox.setSelected(ConfigManager.CONFIG.isCheckOnStart());
        autoStartCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(false));
        autoPopupCheckbox.setSelected(ConfigManager.CONFIG.isAutoPopup());
        autoPopupCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(false));

        //显示状态
        if (App.status.isValid()) {
            statusField.setText(ResourceBundleUtil.getStringValue("settings_status_valid"));
            statusField.setStyle("-fx-text-fill: #005F40");
            version.setText(App.status.getVersion());
            version.setStyle("-fx-text-fill: #005F40");
        } else {
            statusField.setText(ResourceBundleUtil.getStringValue("settings_status_invalid"));
            statusField.setStyle("-fx-text-fill: #ff0000");
            version.setText(ResourceBundleUtil.getStringValue("settings_null_validity"));
            version.setStyle("-fx-text-fill: #ff0000");
        }
    }

    private boolean checkDelayValid() {
        try {
            int delay = Integer.parseInt(checkDelayField.getText());

            if (!App.status.isValid()) {
                //无效
                if (delay < 120) {
                    delayErrorLabel.setText(ResourceBundleUtil.getStringValue("settings_status_invalid"));
                    delayErrorLabel.setVisible(true);
                    return false;
                } else {
                    delayErrorLabel.setVisible(false);
                    return true;
                }
            }

            if (delay < 60) {
                delayErrorLabel.setText(ResourceBundleUtil.getStringValue("settings_delay_cant_less_than"));
                delayErrorLabel.setVisible(true);
                return false;
            } else {
                delayErrorLabel.setVisible(false);
                return true;
            }
        } catch (NumberFormatException e) {
            delayErrorLabel.setText(ResourceBundleUtil.getStringValue("settings_delay_must_be_number"));
            delayErrorLabel.setVisible(true);
            return false;
        }
    }

    private boolean checkProxyValid() {
        String proxyURL = proxyURLField.getText();
        boolean isValid = true;
        String errorMessage = null;
        if (proxyHTTP.isSelected() && !proxyURL.isBlank()) {
            if (!proxyURL.startsWith("http://")) {
                errorMessage = ResourceBundleUtil.getStringValue("settings_http_proxy_url_must_start_with");
                isValid = false;
            }
        } else if (proxySOCKS.isSelected() && !proxyURL.isBlank()) {
            if (!proxyURL.startsWith("socks://")) {
                errorMessage = ResourceBundleUtil.getStringValue("settings_socks_proxy_url_must_start_with");
                isValid = false;
            }
        } else if (!proxyNo.isSelected() && proxyURL.isBlank()) {
            errorMessage = ResourceBundleUtil.getStringValue("settings_proxy_url_cant_be_empty");
            isValid = false;
        }
        if (isValid) {
            proxyErrorLabel.setVisible(false);
        } else {
            proxyErrorLabel.setText(errorMessage);
            proxyErrorLabel.setVisible(true);
        }
        return isValid;
    }

    @FXML
    private void onMouseClickedSaveBtn() {
        saveButton.setDisable(true);
        saveButton.setSelected(false);

        //检查延迟是否合法
        if (!checkDelayValid()) {
            saveButton.setDisable(false);
            return;
        }

        //检查代理地址是否合法
        if (!checkProxyValid()) {
            saveButton.setDisable(false);
            return;
        }

        //RSS链接
        if (rssField.getText().isBlank()) {
            ConfigManager.CONFIG.setRssLink("");
        } else {
            try {
                String encrypt = Encrypt.encryptWithUserName(rssField.getText());
                ConfigManager.CONFIG.setRssLink(encrypt);
            } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                     NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_encryption_error"));
            }
        }

        //语言
        ConfigManager.CONFIG.setLanguage(languageCombo.getText());

        //延迟
        ConfigManager.CONFIG.setCheckDelay(Integer.parseInt(checkDelayField.getText()));

        //代理
        ConfigManager.CONFIG.setProxy(proxyNo.isSelected() ? "NO" : proxyHTTP.isSelected() ? "HTTP" : "SOCKS");
        ConfigManager.CONFIG.setProxyURL(proxyURLField.getText());

        //复选框
        ConfigManager.CONFIG.setCheckOnStart(autoStartCheckbox.isSelected());
        ConfigManager.CONFIG.setAutoPopup(autoPopupCheckbox.isSelected());

        ConfigManager.saveConfig();
        saveButton.requestFocus();
    }
}
