package android.support.v7.graphics.drawable;

import android.graphics.PorterDuff;
import android.support.v4.view.MotionEventCompat;

/* loaded from: classes.dex */
public class DrawableUtils {
    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case MotionEventCompat.AXIS_RZ /* 14 */:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                return PorterDuff.Mode.valueOf("ADD");
            default:
                return defaultMode;
        }
    }
}
