package com.devccv.popuprss.controller;

import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.Encrypt;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.widget.MyToggleNode;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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

public class SettingsController implements Initializable {
    @FXML
    public MFXTextField subscribeStatusField;
    @FXML
    public MFXTextField subscribeValidity;
    @FXML
    public MFXPasswordField rssField;
    @FXML
    public ImageView lockIcon;
    @FXML
    public MFXComboBox<String> languageCombo;
    @FXML
    public MFXTextField checkDelayField;
    @FXML
    public Label delayErrorLabel;
    @FXML
    public MFXCheckbox autoStartCheckbox;
    @FXML
    public MFXCheckbox autoPopupCheckbox;
    @FXML
    public MyToggleNode saveButton;

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
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_decryption_error"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rssField.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(false));
            });
        }

        //语言
        languageCombo.getItems().setAll(List.of("Chinese", "English"));
        if ("Chinese".equals(ConfigManager.CONFIG.getLanguage())) {
            languageCombo.selectFirst();
        } else {
            languageCombo.selectLast();
        }
        languageCombo.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(false));

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

        //检查订阅
        if (isSubscribed()) {
            subscribeStatusField.setText(ResourceBundleUtil.getStringValue("settings_subscribe_status_valid"));
            subscribeStatusField.setStyle("-fx-text-fill: #005F40");
            subscribeValidity.setText(getSubscribeValidity());
            subscribeValidity.setStyle("-fx-text-fill: #005F40");
        } else {
            subscribeStatusField.setText(ResourceBundleUtil.getStringValue("settings_subscribe_status_invalid"));
            subscribeStatusField.setStyle("-fx-text-fill: #ff0000");
            subscribeValidity.setText(ResourceBundleUtil.getStringValue("settings_null_validity"));
            subscribeValidity.setStyle("-fx-text-fill: #ff0000");
        }
    }

    private boolean checkDelayValid() {
        try {
            int delay = Integer.parseInt(checkDelayField.getText());

            if (!isSubscribed()) {
                //未订阅用户
                if (delay < 180) {
                    delayErrorLabel.setText(ResourceBundleUtil.getStringValue("settings_free_user_delay_cant_less_than"));
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

    private boolean isSubscribed() {
        return true;
    }

    private String getSubscribeValidity() {
        return "2024/03/01";
    }

    @FXML
    public void onMouseClickedSaveBtn() {
        saveButton.setDisable(true);
        saveButton.setSelected(false);
        //检查延迟是否合法
        if (!checkDelayValid()) {
            saveButton.setDisable(false);
            return;
        }
        ConfigManager.CONFIG.setCheckDelay(Integer.parseInt(checkDelayField.getText()));

        //RSS链接
        try {
            String encrypt = Encrypt.encryptWithUserName(rssField.getText());
            ConfigManager.CONFIG.setRssLink(encrypt);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_encryption_error"));
        }

        //语言
        ConfigManager.CONFIG.setLanguage(languageCombo.getText());

        //复选框
        ConfigManager.CONFIG.setCheckOnStart(autoStartCheckbox.isSelected());
        ConfigManager.CONFIG.setAutoPopup(autoPopupCheckbox.isSelected());

        ConfigManager.saveConfig();
        saveButton.requestFocus();
    }
}
