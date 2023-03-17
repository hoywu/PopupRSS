package com.devccv.popuprss.widget;

import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import io.github.palexdev.materialfx.effects.ripple.MFXCircleRippleGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

public class MyToggleNode extends MFXRectangleToggleNode {
    private StringProperty rippleColor;
    private MyToggleNodeSkin myToggleNodeSkin;

    public final void setRippleColor(String value) {
        if (rippleColor == null) {
            rippleColor = new SimpleStringProperty(this, "rippleColor", "#284047");
        }
        rippleColor.setValue(value);
    }

    public final String getRippleColor() {
        return rippleColor == null ? "" : rippleColor.getValue();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        myToggleNodeSkin = new MyToggleNodeSkin(this, Color.web(rippleColor.get()));
        return myToggleNodeSkin;
    }

    public MFXCircleRippleGenerator getRippleGenerator() {
        return myToggleNodeSkin.getRippleGenerator();
    }
}
