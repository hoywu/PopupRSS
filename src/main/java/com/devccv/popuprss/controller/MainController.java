package com.devccv.popuprss.controller;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.util.ResourceBundleUtil;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import io.github.palexdev.materialfx.controls.MFXTooltip;
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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.devccv.popuprss.ResourcesLoader.loadURL;
import static com.devccv.popuprss.util.ResourceBundleUtil.getStringValue;

public final class MainController implements Initializable {
    @FXML
    private HBox root;
    private final Stage stage;
    private double mousePressedX;
    private double mousePressedY;
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

    public MainController(Stage stage) {
        this.stage = stage;
        this.toggleGroup = new ToggleGroup();
        ToggleButtonsUtil.addAlwaysOneSelectedSupport(toggleGroup);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //底层控件设置为透明
        root.setBackground(Background.EMPTY);
        //设置自定义字体
        titleLabel.setFont(ResourceBundleUtil.titleFont);
        subTitleLabel.setFont(ResourceBundleUtil.subTitleFont);
        statusText.setFont(ResourceBundleUtil.logFont);
        //按钮提示
        MFXTooltip closeIconTip = MFXTooltip.of(closeIcon, ResourceBundleUtil.getStringValue("close_icon_tooltip"));
        closeIconTip.setShowDelay(new Duration(50.0));
        closeIconTip.install();
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
    }

    private void addSideBarItem(int viewNum, String icon, String text, String fxml, boolean defaultSelected) {
        //添加选择卡到侧边栏
        ToggleButton toggle = createToggle(icon, text);
        //异步载入子视图
        App.FIXED_THREAD_POOL.submit(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(loadURL(fxml), ResourceBundleUtil.resource);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            subViews.put(viewNum, fxmlLoader.getRoot());
        });
        EventHandler<ActionEvent> handler = event -> {
            LogsViewController.stopUpdateLogUI.set(viewNum != 0);
            while (!subViews.containsKey(viewNum)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
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
    private void onMouseClickedRoot() {
        //取消所有控件焦点
        root.requestFocus();
    }

    @FXML
    private void onMousePressedRoot(MouseEvent event) {
        //记录鼠标点击位置
        mousePressedX = event.getSceneX();
        mousePressedY = event.getSceneY();
        //停止日志输出
        LogsViewController.stopUpdateLogUI.set(true);
    }

    @FXML
    private void onMouseReleasedRoot() {
        //开启日志输出
        LogsViewController.stopUpdateLogUI.set(false);
    }

    @FXML
    private void onMouseDraggedRoot(MouseEvent event) {
        //拖拽移动窗口
        stage.setX(event.getScreenX() - mousePressedX);
        stage.setY(event.getScreenY() - mousePressedY);
    }

    @FXML
    private void onMouseClickedCloseIcon(MouseEvent event) {
        //关闭线程池，退出
        if (event.getButton() == MouseButton.PRIMARY) {
            App.FIXED_THREAD_POOL.shutdown();
            LogsViewController.tryShutdownFlushLogThread();
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
