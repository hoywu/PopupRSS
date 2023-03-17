package com.devccv.popuprss.widget;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.effects.ripple.RippleClipType;
import io.github.palexdev.materialfx.factories.RippleClipTypeFactory;

public class MFXPillButton extends MFXButton {
    @Override
    protected void setupRippleGenerator() {
        super.setupRippleGenerator();
        setRippleArcs(20); // For -fx-border-radius: 10;
    }

    public void setRippleArcs(double arcs) {
        getRippleGenerator().setClipSupplier(() ->
                new RippleClipTypeFactory(RippleClipType.ROUNDED_RECTANGLE).setArcs(arcs).build(this));
    }
}
