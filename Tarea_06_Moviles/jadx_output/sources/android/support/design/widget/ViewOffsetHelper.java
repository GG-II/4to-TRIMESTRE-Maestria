package android.support.design.widget;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;

/* loaded from: classes.dex */
class ViewOffsetHelper {
    private int mLayoutLeft;
    private int mLayoutTop;
    private int mOffsetLeft;
    private int mOffsetTop;
    private final View mView;

    public ViewOffsetHelper(View view) {
        this.mView = view;
    }

    public void onViewLayout() {
        this.mLayoutTop = this.mView.getTop();
        this.mLayoutLeft = this.mView.getLeft();
        updateOffsets();
    }

    private void updateOffsets() {
        View view = this.mView;
        ViewCompat.offsetTopAndBottom(view, this.mOffsetTop - (view.getTop() - this.mLayoutTop));
        View view2 = this.mView;
        ViewCompat.offsetLeftAndRight(view2, this.mOffsetLeft - (view2.getLeft() - this.mLayoutLeft));
        if (Build.VERSION.SDK_INT < 23) {
            tickleInvalidationFlag(this.mView);
            Object parent = this.mView.getParent();
            if (parent instanceof View) {
                tickleInvalidationFlag((View) parent);
            }
        }
    }

    private static void tickleInvalidationFlag(View view) {
        float x = ViewCompat.getTranslationX(view);
        ViewCompat.setTranslationY(view, 1.0f + x);
        ViewCompat.setTranslationY(view, x);
    }

    public boolean setTopAndBottomOffset(int offset) {
        if (this.mOffsetTop != offset) {
            this.mOffsetTop = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    public boolean setLeftAndRightOffset(int offset) {
        if (this.mOffsetLeft != offset) {
            this.mOffsetLeft = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    public int getTopAndBottomOffset() {
        return this.mOffsetTop;
    }

    public int getLeftAndRightOffset() {
        return this.mOffsetLeft;
    }
}
