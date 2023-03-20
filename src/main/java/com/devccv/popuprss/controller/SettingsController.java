package com.devccv.popuprss.controller;

import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.Encrypt;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.widget.MyToggleNode;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML
    public MFXPasswordField rssField;
    @FXML
    public ImageView lockIcon;
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

        //填充配置
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
            });
        }

        //设置激活保存按钮监听器
        rssField.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(false));
        saveButton.setDisable(true);
    }

    @FXML
    public void onMouseClickedSaveBtn() {
        saveButton.setDisable(true);
        saveButton.setSelected(false);

        try {
            String encrypt = Encrypt.encryptWithUserName(rssField.getText());
            ConfigManager.CONFIG.setRssLink(encrypt);
            ConfigManager.saveConfig();
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_encryption_error"));
        }
    }
}
