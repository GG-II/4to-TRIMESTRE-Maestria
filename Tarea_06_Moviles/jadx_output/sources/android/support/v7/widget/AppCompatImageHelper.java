package android.support.v7.widget;

import android.R;
import android.support.v4.content.ContextCompat;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

/* loaded from: classes.dex */
class AppCompatImageHelper {
    private static final int[] VIEW_ATTRS = {R.attr.src};
    private final TintManager mTintManager;
    private final ImageView mView;

    AppCompatImageHelper(ImageView view, TintManager tintManager) {
        this.mView = view;
        this.mTintManager = tintManager;
    }

    void loadFromAttributes(AttributeSet attrs, int defStyleAttr) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(this.mView.getContext(), attrs, VIEW_ATTRS, defStyleAttr, 0);
        try {
            if (a.hasValue(0)) {
                this.mView.setImageDrawable(a.getDrawable(0));
            }
        } finally {
            a.recycle();
        }
    }

    void setImageResource(int resId) {
        ImageView imageView = this.mView;
        TintManager tintManager = this.mTintManager;
        imageView.setImageDrawable(tintManager != null ? tintManager.getDrawable(resId) : ContextCompat.getDrawable(imageView.getContext(), resId));
    }
}
