package sutil.ui;

import java.util.function.Predicate;

import sutil.math.SVector;

public record UIMouseWheelAction(int mods, boolean mouseAbove, Predicate<SVector> action) {

    public boolean mouseWheel(SVector scroll, int mods, boolean isMouseAbove) {
        if ((!mouseAbove || isMouseAbove) && this.mods == mods)
            return action.test(scroll);
        return false;
    }
}