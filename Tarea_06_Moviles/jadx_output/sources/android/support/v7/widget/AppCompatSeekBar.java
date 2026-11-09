package android.support.v7.widget;

import android.content.Context;
import android.support.v7.appcompat.R;
import android.support.v7.internal.widget.TintManager;
import android.util.AttributeSet;
import android.widget.SeekBar;

/* loaded from: classes.dex */
public class AppCompatSeekBar extends SeekBar {
    private AppCompatSeekBarHelper mAppCompatSeekBarHelper;
    private TintManager mTintManager;

    public AppCompatSeekBar(Context context) {
        this(context, null);
    }

    public AppCompatSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public AppCompatSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTintManager = TintManager.get(context);
        AppCompatSeekBarHelper appCompatSeekBarHelper = new AppCompatSeekBarHelper(this, this.mTintManager);
        this.mAppCompatSeekBarHelper = appCompatSeekBarHelper;
        appCompatSeekBarHelper.loadFromAttributes(attrs, defStyleAttr);
    }
}
