package android.support.design.widget;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.design.widget.ValueAnimatorCompat;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/* loaded from: classes.dex */
class ValueAnimatorCompatImplEclairMr1 extends ValueAnimatorCompat.Impl {
    private static final int DEFAULT_DURATION = 200;
    private static final int HANDLER_DELAY = 10;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private float mAnimatedFraction;
    private Interpolator mInterpolator;
    private boolean mIsRunning;
    private ValueAnimatorCompat.Impl.AnimatorListenerProxy mListener;
    private long mStartTime;
    private ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy mUpdateListener;
    private final int[] mIntValues = new int[2];
    private final float[] mFloatValues = new float[2];
    private int mDuration = 200;
    private final Runnable mRunnable = new Runnable() { // from class: android.support.design.widget.ValueAnimatorCompatImplEclairMr1.1
        @Override // java.lang.Runnable
        public void run() {
            ValueAnimatorCompatImplEclairMr1.this.update();
        }
    };

    ValueAnimatorCompatImplEclairMr1() {
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void start() {
        if (this.mIsRunning) {
            return;
        }
        if (this.mInterpolator == null) {
            this.mInterpolator = new AccelerateDecelerateInterpolator();
        }
        this.mStartTime = SystemClock.uptimeMillis();
        this.mIsRunning = true;
        ValueAnimatorCompat.Impl.AnimatorListenerProxy animatorListenerProxy = this.mListener;
        if (animatorListenerProxy != null) {
            animatorListenerProxy.onAnimationStart();
        }
        sHandler.postDelayed(this.mRunnable, 10L);
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public boolean isRunning() {
        return this.mIsRunning;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void setListener(ValueAnimatorCompat.Impl.AnimatorListenerProxy listener) {
        this.mListener = listener;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void setUpdateListener(ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy updateListener) {
        this.mUpdateListener = updateListener;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void setIntValues(int from, int to) {
        int[] iArr = this.mIntValues;
        iArr[0] = from;
        iArr[1] = to;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public int getAnimatedIntValue() {
        int[] iArr = this.mIntValues;
        return AnimationUtils.lerp(iArr[0], iArr[1], getAnimatedFraction());
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void setFloatValues(float from, float to) {
        float[] fArr = this.mFloatValues;
        fArr[0] = from;
        fArr[1] = to;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public float getAnimatedFloatValue() {
        float[] fArr = this.mFloatValues;
        return AnimationUtils.lerp(fArr[0], fArr[1], getAnimatedFraction());
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void cancel() {
        this.mIsRunning = false;
        sHandler.removeCallbacks(this.mRunnable);
        ValueAnimatorCompat.Impl.AnimatorListenerProxy animatorListenerProxy = this.mListener;
        if (animatorListenerProxy != null) {
            animatorListenerProxy.onAnimationCancel();
        }
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public float getAnimatedFraction() {
        return this.mAnimatedFraction;
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public void end() {
        if (this.mIsRunning) {
            this.mIsRunning = false;
            sHandler.removeCallbacks(this.mRunnable);
            this.mAnimatedFraction = 1.0f;
            ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy animatorUpdateListenerProxy = this.mUpdateListener;
            if (animatorUpdateListenerProxy != null) {
                animatorUpdateListenerProxy.onAnimationUpdate();
            }
            ValueAnimatorCompat.Impl.AnimatorListenerProxy animatorListenerProxy = this.mListener;
            if (animatorListenerProxy != null) {
                animatorListenerProxy.onAnimationEnd();
            }
        }
    }

    @Override // android.support.design.widget.ValueAnimatorCompat.Impl
    public long getDuration() {
        return this.mDuration;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void update() {
        if (this.mIsRunning) {
            long elapsed = SystemClock.uptimeMillis() - this.mStartTime;
            float linearFraction = elapsed / this.mDuration;
            Interpolator interpolator = this.mInterpolator;
            this.mAnimatedFraction = interpolator != null ? interpolator.getInterpolation(linearFraction) : linearFraction;
            ValueAnimatorCompat.Impl.AnimatorUpdateListenerProxy animatorUpdateListenerProxy = this.mUpdateListener;
            if (animatorUpdateListenerProxy != null) {
                animatorUpdateListenerProxy.onAnimationUpdate();
            }
            if (SystemClock.uptimeMillis() >= this.mStartTime + this.mDuration) {
                this.mIsRunning = false;
                ValueAnimatorCompat.Impl.AnimatorListenerProxy animatorListenerProxy = this.mListener;
                if (animatorListenerProxy != null) {
                    animatorListenerProxy.onAnimationEnd();
                }
            }
        }
        if (this.mIsRunning) {
            sHandler.postDelayed(this.mRunnable, 10L);
        }
    }
}
