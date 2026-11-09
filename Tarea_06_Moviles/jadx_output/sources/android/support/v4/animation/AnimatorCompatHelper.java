package android.support.v4.animation;

import android.view.View;

/* loaded from: classes.dex */
public abstract class AnimatorCompatHelper {
    static AnimatorProvider IMPL = new HoneycombMr1AnimatorCompatProvider();

    public static ValueAnimatorCompat emptyValueAnimator() {
        return IMPL.emptyValueAnimator();
    }

    AnimatorCompatHelper() {
    }

    public static void clearInterpolator(View view) {
        IMPL.clearInterpolator(view);
    }
}
