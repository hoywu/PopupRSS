package com.devccv.popuprss.controller;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.ResourceBundleUtil;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import io.github.palexdev.materialfx.controls.MFXTooltip;
import io.github.palexdev.materialfx.utils.SwingFXUtils;
import io.github.palexdev.materialfx.utils.ToggleButtonsUtil;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.devccv.popuprss.ResourcesLoader.loadURL;
import static com.devccv.popuprss.util.ResourceBundleUtil.getStringValue;

public final class MainController implements Initializable {
    @FXML
    private AnchorPane root;
    @FXML
    public AnchorPane rootPane;
    private final Stage stage;
    private boolean isMaximized = false;
    private double pervStageX;
    private double pervStageY;
    private double pervStageWidth;
    private double pervStageHeight;
    private double mousePressedX;
    private double mousePressedY;
//    private double mousePressedScreenX;
//    private double mousePressedScreenY;
//    private double mousePressedStageWidth;
//    private double mousePressedStageHeight;
    @FXML
    public Button resizeBtnSW;
    @FXML
    public Button resizeBtnSE;
    public static Consumer<String> switchToErrorStatus; //需要使用Platform.runLater在UI线程调用
    public static Consumer<String> switchToEnableStatus; //需要使用Platform.runLater在UI线程调用
    public static Consumer<String> switchToDisableStatus; //需要使用Platform.runLater在UI线程调用
    @FXML
    private HBox statusBar;
    @FXML
    private Rectangle errorBar;
    @FXML
    private Rectangle enableBar;
    @FXML
    private Rectangle disableBar;
    @FXML
    private Label statusText;
    @FXML
    public Circle minimizeIcon;
    @FXML
    private Circle closeIcon;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subTitleLabel;
    @FXML
    private ImageView logo;
    @FXML
    private VBox navBar;
    @FXML
    private StackPane contentPane;
    private final ToggleGroup toggleGroup;
    private ObservableList<Node> navBarChildren;
    private ObservableList<Node> contentPaneChildren;
    private final ConcurrentHashMap<Integer, Node> subViews = new ConcurrentHashMap<>();
    private TrayIcon trayIconObj;
    private static boolean minimizeNotifyFlag = true;

    public MainController(Stage stage) {
        this.stage = stage;
        this.toggleGroup = new ToggleGroup();
        ToggleButtonsUtil.addAlwaysOneSelectedSupport(toggleGroup);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //底层控件设置为透明
        root.setBackground(Background.EMPTY);
        //设置四角拖动按钮透明
        resizeBtnSW.setBackground(Background.EMPTY);
        resizeBtnSE.setBackground(Background.EMPTY);
        //设置自定义字体
        titleLabel.setFont(ResourceBundleUtil.titleFont);
        subTitleLabel.setFont(ResourceBundleUtil.subTitleFont);
        statusText.setFont(ResourceBundleUtil.logFont);
        //按钮提示
        MFXTooltip closeIconTip = MFXTooltip.of(closeIcon, ResourceBundleUtil.getStringValue("close_icon_tooltip"));
        MFXTooltip minimizeIconTip = MFXTooltip.of(minimizeIcon, ResourceBundleUtil.getStringValue("minimize_icon_tooltip"));
        closeIconTip.setShowDelay(new Duration(50.0));
        closeIconTip.install();
        minimizeIconTip.setShowDelay(new Duration(50.0));
        minimizeIconTip.install();
        //载入Logo
        logo.setImage(new Image(ResourcesLoader.loadStream("icon/logo.png")));
        //获取内容区子元素列表
        contentPaneChildren = contentPane.getChildren();
        //载入侧边栏
        navBarChildren = navBar.getChildren();
        addSideBarItem(0, "mfx-content-paste", getStringValue("list_logs"), "fxml/Logs.fxml", true);
        addSideBarItem(1, "mfx-bars", getStringValue("list_archived"), "fxml/Archived.fxml", false);
        addSideBarItem(2, "mfx-gear", getStringValue("list_settings"), "fxml/Settings.fxml", false);
        //设置顶部状态栏
        Rectangle clip = new Rectangle(100, 16);
        clip.setArcHeight(10);
        clip.setArcWidth(10);
        statusBar.setClip(clip);
        errorBar.setTranslateX(-100);
        enableBar.setTranslateX(-100);
        disableBar.setTranslateX(-100);
        //构造供外部调用的状态栏切换方法
        switchToErrorStatus = s -> {
            switchBar(BAR_STATUS.error);
            statusText.setText(s);
        };
        switchToEnableStatus = s -> {
            switchBar(BAR_STATUS.enable);
            statusText.setText(s);
        };
        switchToDisableStatus = s -> {
            switchBar(BAR_STATUS.disable);
            statusText.setText(s);
        };
        //窗口关闭事件
        stage.setOnCloseRequest(event -> {
            //关闭线程池，退出
            App.FIXED_THREAD_POOL.shutdownNow();
            SystemTray.getSystemTray().remove(trayIconObj);
            Platform.exit();
        });
        //托盘图标
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            SystemTray tray = SystemTray.getSystemTray();
            //托盘图标
            Image trayIcon = new Image(ResourcesLoader.loadStream("icon/logo.png"));
            trayIconObj = new TrayIcon(SwingFXUtils.fromFXImage(trayIcon, null), getStringValue("tray_tooltip"));
            trayIconObj.setImageAutoSize(true);
            //托盘图标点击事件
            trayIconObj.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    Platform.runLater(() -> {
                        stage.show();
                        stage.setIconified(false);
                        stage.toFront();
                    });
                }
            });
            //添加托盘图标
            try {
                tray.add(trayIconObj);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }
        //自动最小化
        if (ConfigManager.CONFIG.isAutoMinimize()) {
            minimizeNotifyFlag = false;
            Platform.runLater(stage::hide);
        }
    }

    private void addSideBarItem(int viewNum, String icon, String text, String fxml, boolean defaultSelected) {
        //添加选择卡到侧边栏
        ToggleButton toggle = createToggle(icon, text);
        //载入子视图
        FXMLLoader fxmlLoader = new FXMLLoader(loadURL(fxml), ResourceBundleUtil.resource);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        subViews.put(viewNum, fxmlLoader.getRoot());
        //添加选择卡点击事件
        EventHandler<ActionEvent> handler = event -> {
            if (viewNum == 0) {
                LogsViewController.stopUpdateLogUI.set(false);
                LogsViewController.newLog("");
            } else {
                LogsViewController.stopUpdateLogUI.set(true);
            }
            contentPaneChildren.setAll(subViews.get(viewNum));
        };
        toggle.setOnAction(handler);
        if (defaultSelected) {
            toggle.setSelected(true);
            handler.handle(null);
        }
        navBarChildren.add(toggle);
    }

    private ToggleButton createToggle(String icon, String text) {
        //创建侧边栏选择卡按钮
        MFXIconWrapper wrapper = new MFXIconWrapper(icon, 20, 26);
        MFXRectangleToggleNode toggleNode = new MFXRectangleToggleNode(text, wrapper);
        toggleNode.setAlignment(Pos.CENTER_LEFT);
        toggleNode.setMaxWidth(Double.MAX_VALUE);
        toggleNode.setToggleGroup(toggleGroup);
        toggleNode.getStyleClass().add("side-bar-toggle-node");
        return toggleNode;
    }

    @FXML
    private void onMouseClickedRoot(MouseEvent event) {
        //取消所有控件焦点
        root.requestFocus();

        if (event.getClickCount() == 2 && event.getButton().name().equals(MouseButton.PRIMARY.name())) {
            //最大化
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            if (isMaximized) {
                stage.setX(pervStageX);
                stage.setY(pervStageY);
                stage.setWidth(pervStageWidth);
                stage.setHeight(pervStageHeight);
                AnchorPane.setLeftAnchor(rootPane, 25.0);
                AnchorPane.setRightAnchor(rootPane, 25.0);
                AnchorPane.setTopAnchor(rootPane, 25.0);
                AnchorPane.setBottomAnchor(rootPane, 25.0);
            } else {
                pervStageX = stage.getX();
                pervStageY = stage.getY();
                pervStageWidth = stage.getWidth();
                pervStageHeight = stage.getHeight();
                stage.setX(primaryScreenBounds.getMinX());
                stage.setY(primaryScreenBounds.getMinY());
                stage.setWidth(primaryScreenBounds.getWidth());
                stage.setHeight(primaryScreenBounds.getHeight());
                AnchorPane.setLeftAnchor(rootPane, 0.0);
                AnchorPane.setRightAnchor(rootPane, 0.0);
                AnchorPane.setTopAnchor(rootPane, 0.0);
                AnchorPane.setBottomAnchor(rootPane, 0.0);
            }
            isMaximized = !isMaximized;
        }
    }

    @FXML
    private void onMouseDraggedSW(MouseEvent event) {
        //拖拽改变窗口大小 左下角
        //todo
    }

    @FXML
    private void onMouseDraggedSE(MouseEvent event) {
        //拖拽改变窗口大小 右下角
        double x = event.getScreenX();
        double y = event.getScreenY();
        if (isMaximized) {
            x -= 25.0;
            y -= 25.0;
        }
        double width = x - stage.getX() + 25.0;
        double height = y - stage.getY() + 25.0;
        if (width < 800) width = 800;
        if (height < 500) height = 500;
        stage.setWidth(width);
        stage.setHeight(height);
    }

    @FXML
    private void onMousePressedRoot(MouseEvent event) {
        //记录鼠标点击位置
        mousePressedX = event.getSceneX();
        mousePressedY = event.getSceneY();
//        mousePressedScreenX = event.getScreenX();
//        mousePressedScreenY = event.getScreenY();
//        //记录舞台大小
//        mousePressedStageWidth = stage.getWidth();
//        mousePressedStageHeight = stage.getHeight();
        //停止日志输出
        LogsViewController.stopUpdateLogUI.set(true);
    }

    @FXML
    private void onMouseReleasedRoot() {
        //开启日志输出
        LogsViewController.stopUpdateLogUI.set(false);
        LogsViewController.newLog("");
    }

    @FXML
    private void onMouseDraggedRoot(MouseEvent event) {
        //拖拽移动窗口
        stage.setX(event.getScreenX() - mousePressedX);
        stage.setY(event.getScreenY() - mousePressedY);
    }

    @FXML
    private void onMouseClickedMinimizeIcon(MouseEvent event) {
        //最小化窗口到托盘图标
        if (event.getButton() == MouseButton.PRIMARY) {
            if (minimizeNotifyFlag) {
                trayIconObj.displayMessage(ResourceBundleUtil.getStringValue("tray_message_title"), ResourceBundleUtil.getStringValue("tray_message_minimize"), TrayIcon.MessageType.INFO);
                minimizeNotifyFlag = false;
            }
            stage.hide();
        }
    }

    @FXML
    private void onMouseClickedCloseIcon(MouseEvent event) {
        //关闭线程池，退出
        if (event.getButton() == MouseButton.PRIMARY) {
            App.FIXED_THREAD_POOL.shutdownNow();
            SystemTray.getSystemTray().remove(trayIconObj);
            Platform.exit();
        }
    }

    private enum BAR_STATUS {
        error, enable, disable
    }

    private void switchBar(BAR_STATUS to) {
        switch (to) {
            case error -> {
                if (errorBar.getTranslateX() == 0) return;
                Timeline errTimeline = createTimeline(errorBar, 0);
                Timeline enTimeline = createTimeline(enableBar, 0);
                Timeline diTimeline = createTimeline(disableBar, 0);

                ParallelTransition parallelTransition = new ParallelTransition(errTimeline, enTimeline, diTimeline);
                parallelTransition.play();
            }
            case enable -> {
                if (enableBar.getTranslateX() == -100) return;
                Timeline errTimeline = createTimeline(errorBar, -100);
                Timeline enTimeline = createTimeline(enableBar, -100);
                Timeline diTimeline = createTimeline(disableBar, -100);

                ParallelTransition parallelTransition = new ParallelTransition(errTimeline, enTimeline, diTimeline);
                parallelTransition.play();
            }
            case disable -> {
                if (disableBar.getTranslateX() == -200) return;
                Timeline errTimeline = createTimeline(errorBar, -200);
                Timeline enTimeline = createTimeline(enableBar, -200);
                Timeline diTimeline = createTimeline(disableBar, -200);

                ParallelTransition parallelTransition = new ParallelTransition(errTimeline, enTimeline, diTimeline);
                parallelTransition.play();
            }
        }
    }

    private Timeline createTimeline(Node node, int end) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(node.translateXProperty(), end);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        return timeline;
    }
}
