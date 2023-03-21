package com.devccv.popuprss.controller;

import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.subscribe.MicrosoftStore;
import com.devccv.popuprss.subscribe.Subscribe;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.Encrypt;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.widget.MyToggleNode;
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

public class SettingsController implements Initializable {
    private static final Subscribe subscribe = new MicrosoftStore();
    @FXML
    private MFXTextField subscribeStatusField;
    @FXML
    private MFXTextField subscribeValidity;
    @FXML
    private MFXComboBox<String> languageCombo;
    @FXML
    private MFXPasswordField rssField;
    @FXML
    private ImageView lockIcon;
    @FXML
    private MFXTextField checkDelayField;
    @FXML
    public MFXRadioButton proxyNo;
    @FXML
    public MFXRadioButton proxyHTTP;
    @FXML
    public MFXRadioButton proxySOCKS;
    @FXML
    public MFXTextField proxyURLField;
    @FXML
    public Label proxyErrorLabel;
    @FXML
    private Label delayErrorLabel;
    @FXML
    private MFXCheckbox autoStartCheckbox;
    @FXML
    private MFXCheckbox autoPopupCheckbox;
    @FXML
    private MyToggleNode saveButton;

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

        //检查订阅
        if (subscribe.isSubscribed()) {
            subscribeStatusField.setText(ResourceBundleUtil.getStringValue("settings_subscribe_status_valid"));
            subscribeStatusField.setStyle("-fx-text-fill: #005F40");
            subscribeValidity.setText(subscribe.getSubscribeValidity());
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

            if (!subscribe.isSubscribed()) {
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

    private boolean checkProxyValid() {
        String proxyURL = proxyURLField.getText();
        boolean isValid = true;
        String errorMessage = null;
        if (proxyHTTP.isSelected() && !proxyURL.isBlank()) {
            if (!proxyURL.startsWith("http://") && !proxyURL.startsWith("https://")) {
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
    public void onMouseClickedSaveBtn() {
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
        if (!rssField.getText().isBlank()) {
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
