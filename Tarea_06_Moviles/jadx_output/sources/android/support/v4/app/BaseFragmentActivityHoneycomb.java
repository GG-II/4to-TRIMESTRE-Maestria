package android.support.v4.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/* loaded from: classes.dex */
abstract class BaseFragmentActivityHoneycomb extends BaseFragmentActivityDonut {
    BaseFragmentActivityHoneycomb() {
    }

    @Override // android.app.Activity, android.view.LayoutInflater.Factory2
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View v = dispatchFragmentsOnCreateView(parent, name, context, attrs);
        if (v == null) {
            return super.onCreateView(parent, name, context, attrs);
        }
        return v;
    }
}
