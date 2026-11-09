package android.support.design.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.design.R;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class CoordinatorLayout extends ViewGroup implements NestedScrollingParent {
    static final Class<?>[] CONSTRUCTOR_PARAMS;
    static final CoordinatorLayoutInsetsHelper INSETS_HELPER;
    static final String TAG = "CoordinatorLayout";
    static final Comparator<View> TOP_SORTED_CHILDREN_COMPARATOR;
    private static final int TYPE_ON_INTERCEPT = 0;
    private static final int TYPE_ON_TOUCH = 1;
    static final String WIDGET_PACKAGE_NAME;
    static final ThreadLocal<Map<String, Constructor<Behavior>>> sConstructors;
    private View mBehaviorTouchView;
    private final List<View> mDependencySortedChildren;
    private boolean mDrawStatusBarBackground;
    private boolean mIsAttachedToWindow;
    private int[] mKeylines;
    private WindowInsetsCompat mLastInsets;
    final Comparator<View> mLayoutDependencyComparator;
    private boolean mNeedsPreDrawListener;
    private View mNestedScrollingDirectChild;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private View mNestedScrollingTarget;
    private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;
    private OnPreDrawListener mOnPreDrawListener;
    private Paint mScrimPaint;
    private Drawable mStatusBarBackground;
    private final List<View> mTempDependenciesList;
    private final int[] mTempIntPair;
    private final List<View> mTempList1;
    private final Rect mTempRect1;
    private final Rect mTempRect2;
    private final Rect mTempRect3;

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultBehavior {
        Class<? extends Behavior> value();
    }

    static {
        Package pkg = CoordinatorLayout.class.getPackage();
        WIDGET_PACKAGE_NAME = pkg != null ? pkg.getName() : null;
        if (Build.VERSION.SDK_INT >= 21) {
            TOP_SORTED_CHILDREN_COMPARATOR = new ViewElevationComparator();
            INSETS_HELPER = new CoordinatorLayoutInsetsHelperLollipop();
        } else {
            TOP_SORTED_CHILDREN_COMPARATOR = null;
            INSETS_HELPER = null;
        }
        CONSTRUCTOR_PARAMS = new Class[]{Context.class, AttributeSet.class};
        sConstructors = new ThreadLocal<>();
    }

    public CoordinatorLayout(Context context) {
        this(context, null);
    }

    public CoordinatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mLayoutDependencyComparator = new Comparator<View>() { // from class: android.support.design.widget.CoordinatorLayout.1
            @Override // java.util.Comparator
            public int compare(View lhs, View rhs) {
                if (lhs == rhs) {
                    return 0;
                }
                if (((LayoutParams) lhs.getLayoutParams()).dependsOn(CoordinatorLayout.this, lhs, rhs)) {
                    return 1;
                }
                if (!((LayoutParams) rhs.getLayoutParams()).dependsOn(CoordinatorLayout.this, rhs, lhs)) {
                    return 0;
                }
                return -1;
            }
        };
        this.mDependencySortedChildren = new ArrayList();
        this.mTempList1 = new ArrayList();
        this.mTempDependenciesList = new ArrayList();
        this.mTempRect1 = new Rect();
        this.mTempRect2 = new Rect();
        this.mTempRect3 = new Rect();
        this.mTempIntPair = new int[2];
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        ThemeUtils.checkAppCompatTheme(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CoordinatorLayout, defStyleAttr, R.style.Widget_Design_CoordinatorLayout);
        int keylineArrayRes = a.getResourceId(R.styleable.CoordinatorLayout_keylines, 0);
        if (keylineArrayRes != 0) {
            Resources res = context.getResources();
            this.mKeylines = res.getIntArray(keylineArrayRes);
            float density = res.getDisplayMetrics().density;
            int count = this.mKeylines.length;
            for (int i = 0; i < count; i++) {
                this.mKeylines[i] = (int) (r6[i] * density);
            }
        }
        this.mStatusBarBackground = a.getDrawable(R.styleable.CoordinatorLayout_statusBarBackground);
        a.recycle();
        CoordinatorLayoutInsetsHelper coordinatorLayoutInsetsHelper = INSETS_HELPER;
        if (coordinatorLayoutInsetsHelper != null) {
            coordinatorLayoutInsetsHelper.setupForWindowInsets(this, new ApplyInsetsListener());
        }
        super.setOnHierarchyChangeListener(new HierarchyChangeListener());
    }

    @Override // android.view.ViewGroup
    public void setOnHierarchyChangeListener(ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener) {
        this.mOnHierarchyChangeListener = onHierarchyChangeListener;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        resetTouchBehaviors();
        if (this.mNeedsPreDrawListener) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(this.mOnPreDrawListener);
        }
        if (this.mLastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            ViewCompat.requestApplyInsets(this);
        }
        this.mIsAttachedToWindow = true;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetTouchBehaviors();
        if (this.mNeedsPreDrawListener && this.mOnPreDrawListener != null) {
            ViewTreeObserver vto = getViewTreeObserver();
            vto.removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        View view = this.mNestedScrollingTarget;
        if (view != null) {
            onStopNestedScroll(view);
        }
        this.mIsAttachedToWindow = false;
    }

    public void setStatusBarBackground(Drawable bg) {
        this.mStatusBarBackground = bg;
        invalidate();
    }

    public Drawable getStatusBarBackground() {
        return this.mStatusBarBackground;
    }

    public void setStatusBarBackgroundResource(int resId) {
        setStatusBarBackground(resId != 0 ? ContextCompat.getDrawable(getContext(), resId) : null);
    }

    public void setStatusBarBackgroundColor(int color) {
        setStatusBarBackground(new ColorDrawable(color));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setWindowInsets(WindowInsetsCompat insets) {
        if (this.mLastInsets != insets) {
            this.mLastInsets = insets;
            boolean z = insets != null && insets.getSystemWindowInsetTop() > 0;
            this.mDrawStatusBarBackground = z;
            setWillNotDraw(!z && getBackground() == null);
            dispatchChildApplyWindowInsets(insets);
            requestLayout();
        }
    }

    private void resetTouchBehaviors() {
        View view = this.mBehaviorTouchView;
        if (view != null) {
            Behavior b = ((LayoutParams) view.getLayoutParams()).getBehavior();
            if (b != null) {
                long now = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                b.onTouchEvent(this, this.mBehaviorTouchView, cancelEvent);
                cancelEvent.recycle();
            }
            this.mBehaviorTouchView = null;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.resetTouchBehaviorTracking();
        }
    }

    private void getTopSortedChildren(List<View> out) {
        out.clear();
        boolean useCustomOrder = isChildrenDrawingOrderEnabled();
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            int childIndex = useCustomOrder ? getChildDrawingOrder(childCount, i) : i;
            View child = getChildAt(childIndex);
            out.add(child);
        }
        Comparator<View> comparator = TOP_SORTED_CHILDREN_COMPARATOR;
        if (comparator != null) {
            Collections.sort(out, comparator);
        }
    }

    private boolean performIntercept(MotionEvent ev, int type) {
        boolean intercepted = false;
        boolean newBlock = false;
        MotionEvent cancelEvent = null;
        int action = MotionEventCompat.getActionMasked(ev);
        List<View> topmostChildList = this.mTempList1;
        getTopSortedChildren(topmostChildList);
        int childCount = topmostChildList.size();
        for (int i = 0; i < childCount; i++) {
            View child = topmostChildList.get(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Behavior b = lp.getBehavior();
            if ((intercepted || newBlock) && action != 0) {
                if (b != null) {
                    if (cancelEvent == null) {
                        long now = SystemClock.uptimeMillis();
                        cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                    }
                    switch (type) {
                        case 0:
                            b.onInterceptTouchEvent(this, child, cancelEvent);
                            break;
                        case 1:
                            b.onTouchEvent(this, child, cancelEvent);
                            break;
                    }
                }
            } else {
                if (!intercepted && b != null) {
                    switch (type) {
                        case 0:
                            intercepted = b.onInterceptTouchEvent(this, child, ev);
                            break;
                        case 1:
                            intercepted = b.onTouchEvent(this, child, ev);
                            break;
                    }
                    if (intercepted) {
                        this.mBehaviorTouchView = child;
                    }
                }
                boolean wasBlocking = lp.didBlockInteraction();
                boolean isBlocking = lp.isBlockingInteractionBelow(this, child);
                newBlock = isBlocking && !wasBlocking;
                if (isBlocking && !newBlock) {
                    topmostChildList.clear();
                    return intercepted;
                }
            }
        }
        topmostChildList.clear();
        return intercepted;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MotionEvent cancelEvent = null;
        int action = MotionEventCompat.getActionMasked(ev);
        if (action == 0) {
            resetTouchBehaviors();
        }
        boolean intercepted = performIntercept(ev, 0);
        if (0 != 0) {
            cancelEvent.recycle();
        }
        if (action == 1 || action == 3) {
            resetTouchBehaviors();
        }
        return intercepted;
    }

    /* JADX WARN: Code restructure failed: missing block: B:4:0x0015, code lost:
    
        if (r6 != false) goto L6;
     */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean onTouchEvent(android.view.MotionEvent r19) {
        /*
            r18 = this;
            r0 = r18
            r1 = r19
            r2 = 0
            r3 = 0
            r4 = 0
            int r5 = android.support.v4.view.MotionEventCompat.getActionMasked(r19)
            android.view.View r6 = r0.mBehaviorTouchView
            r7 = 1
            if (r6 != 0) goto L17
            boolean r6 = r0.performIntercept(r1, r7)
            r3 = r6
            if (r6 == 0) goto L2b
        L17:
            android.view.View r6 = r0.mBehaviorTouchView
            android.view.ViewGroup$LayoutParams r6 = r6.getLayoutParams()
            android.support.design.widget.CoordinatorLayout$LayoutParams r6 = (android.support.design.widget.CoordinatorLayout.LayoutParams) r6
            android.support.design.widget.CoordinatorLayout$Behavior r8 = r6.getBehavior()
            if (r8 == 0) goto L2b
            android.view.View r9 = r0.mBehaviorTouchView
            boolean r2 = r8.onTouchEvent(r0, r9, r1)
        L2b:
            android.view.View r6 = r0.mBehaviorTouchView
            if (r6 != 0) goto L35
            boolean r6 = super.onTouchEvent(r19)
            r2 = r2 | r6
            goto L4c
        L35:
            if (r3 == 0) goto L4c
            if (r4 != 0) goto L49
            long r16 = android.os.SystemClock.uptimeMillis()
            r12 = 3
            r13 = 0
            r14 = 0
            r15 = 0
            r8 = r16
            r10 = r16
            android.view.MotionEvent r4 = android.view.MotionEvent.obtain(r8, r10, r12, r13, r14, r15)
        L49:
            super.onTouchEvent(r4)
        L4c:
            if (r4 == 0) goto L52
            r4.recycle()
        L52:
            if (r5 == r7) goto L57
            r6 = 3
            if (r5 != r6) goto L5a
        L57:
            r18.resetTouchBehaviors()
        L5a:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.design.widget.CoordinatorLayout.onTouchEvent(android.view.MotionEvent):boolean");
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (disallowIntercept) {
            resetTouchBehaviors();
        }
    }

    private int getKeyline(int index) {
        int[] iArr = this.mKeylines;
        if (iArr == null) {
            Log.e(TAG, "No keylines defined for " + this + " - attempted index lookup " + index);
            return 0;
        }
        if (index < 0 || index >= iArr.length) {
            Log.e(TAG, "Keyline index " + index + " out of range for " + this);
            return 0;
        }
        return iArr[index];
    }

    /* JADX WARN: Multi-variable type inference failed */
    static Behavior parseBehavior(Context context, AttributeSet attrs, String name) {
        String fullName;
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (name.startsWith(".")) {
            fullName = context.getPackageName() + name;
        } else if (name.indexOf(46) >= 0) {
            fullName = name;
        } else {
            String str = WIDGET_PACKAGE_NAME;
            fullName = !TextUtils.isEmpty(str) ? str + '.' + name : name;
        }
        try {
            ThreadLocal<Map<String, Constructor<Behavior>>> threadLocal = sConstructors;
            Map<String, Constructor<Behavior>> constructors = threadLocal.get();
            if (constructors == null) {
                constructors = new HashMap();
                threadLocal.set(constructors);
            }
            Constructor<Behavior> c = constructors.get(fullName);
            if (c == null) {
                c = Class.forName(fullName, true, context.getClassLoader()).getConstructor(CONSTRUCTOR_PARAMS);
                c.setAccessible(true);
                constructors.put(fullName, c);
            }
            return c.newInstance(context, attrs);
        } catch (Exception e) {
            throw new RuntimeException("Could not inflate Behavior subclass " + fullName, e);
        }
    }

    LayoutParams getResolvedLayoutParams(View child) {
        LayoutParams result = (LayoutParams) child.getLayoutParams();
        if (!result.mBehaviorResolved) {
            DefaultBehavior defaultBehavior = null;
            for (Class<?> childClass = child.getClass(); childClass != null; childClass = childClass.getSuperclass()) {
                DefaultBehavior defaultBehavior2 = (DefaultBehavior) childClass.getAnnotation(DefaultBehavior.class);
                defaultBehavior = defaultBehavior2;
                if (defaultBehavior2 != null) {
                    break;
                }
            }
            if (defaultBehavior != null) {
                try {
                    result.setBehavior(defaultBehavior.value().newInstance());
                } catch (Exception e) {
                    Log.e(TAG, "Default behavior class " + defaultBehavior.value().getName() + " could not be instantiated. Did you forget a default constructor?", e);
                }
            }
            result.mBehaviorResolved = true;
        }
        return result;
    }

    private void prepareChildren() {
        this.mDependencySortedChildren.clear();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = getResolvedLayoutParams(child);
            lp.findAnchorView(this, child);
            this.mDependencySortedChildren.add(child);
        }
        selectionSort(this.mDependencySortedChildren, this.mLayoutDependencyComparator);
    }

    void getDescendantRect(View descendant, Rect out) {
        ViewGroupUtils.getDescendantRect(this, descendant, out);
    }

    @Override // android.view.View
    protected int getSuggestedMinimumWidth() {
        return Math.max(super.getSuggestedMinimumWidth(), getPaddingLeft() + getPaddingRight());
    }

    @Override // android.view.View
    protected int getSuggestedMinimumHeight() {
        return Math.max(super.getSuggestedMinimumHeight(), getPaddingTop() + getPaddingBottom());
    }

    public void onMeasureChild(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childState;
        int heightUsed;
        int widthUsed;
        int childWidthMeasureSpec;
        int horizInsets;
        LayoutParams lp;
        int childState2;
        int heightUsed2;
        int widthUsed2;
        int i;
        int childCount;
        int heightMode;
        prepareChildren();
        ensurePreDrawListener();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        boolean isRtl = layoutDirection == 1;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightMode2 = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthPadding = paddingLeft + paddingRight;
        int heightPadding = paddingTop + paddingBottom;
        int widthUsed3 = getSuggestedMinimumWidth();
        int heightUsed3 = getSuggestedMinimumHeight();
        boolean applyInsets = this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this);
        int childCount2 = this.mDependencySortedChildren.size();
        int i2 = 0;
        int childState3 = widthUsed3;
        int widthUsed4 = 0;
        while (i2 < childCount2) {
            View child = this.mDependencySortedChildren.get(i2);
            LayoutParams lp2 = (LayoutParams) child.getLayoutParams();
            int keylineWidthUsed = 0;
            if (lp2.keyline >= 0 && widthMode != 0) {
                int keylinePos = getKeyline(lp2.keyline);
                childState = widthUsed4;
                int childState4 = lp2.gravity;
                int keylineGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(childState4), layoutDirection) & 7;
                heightUsed = heightUsed3;
                if ((keylineGravity != 3 || isRtl) && (keylineGravity != 5 || !isRtl)) {
                    widthUsed = childState3;
                    if ((keylineGravity == 5 && !isRtl) || (keylineGravity == 3 && isRtl)) {
                        keylineWidthUsed = Math.max(0, keylinePos - paddingLeft);
                    }
                } else {
                    widthUsed = childState3;
                    keylineWidthUsed = Math.max(0, (widthSize - paddingRight) - keylinePos);
                }
            } else {
                childState = widthUsed4;
                heightUsed = heightUsed3;
                widthUsed = childState3;
            }
            if (applyInsets && !ViewCompat.getFitsSystemWindows(child)) {
                int horizInsets2 = this.mLastInsets.getSystemWindowInsetLeft() + this.mLastInsets.getSystemWindowInsetRight();
                int vertInsets = this.mLastInsets.getSystemWindowInsetTop() + this.mLastInsets.getSystemWindowInsetBottom();
                int childWidthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(widthSize - horizInsets2, widthMode);
                childWidthMeasureSpec = childWidthMeasureSpec2;
                int childWidthMeasureSpec3 = heightSize - vertInsets;
                int childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(childWidthMeasureSpec3, heightMode2);
                horizInsets = childHeightMeasureSpec;
            } else {
                childWidthMeasureSpec = widthMeasureSpec;
                horizInsets = heightMeasureSpec;
            }
            Behavior b = lp2.getBehavior();
            if (b != null) {
                childState2 = childState;
                heightUsed2 = heightUsed;
                lp = lp2;
                widthUsed2 = widthUsed;
                i = i2;
                int i3 = keylineWidthUsed;
                childCount = childCount2;
                int childCount3 = horizInsets;
                heightMode = heightMode2;
                if (b.onMeasureChild(this, child, childWidthMeasureSpec, i3, childCount3, 0)) {
                    LayoutParams lp3 = lp;
                    childState3 = Math.max(widthUsed2, widthPadding + child.getMeasuredWidth() + lp3.leftMargin + lp3.rightMargin);
                    heightUsed3 = Math.max(heightUsed2, heightPadding + child.getMeasuredHeight() + lp3.topMargin + lp3.bottomMargin);
                    widthUsed4 = ViewCompat.combineMeasuredStates(childState2, ViewCompat.getMeasuredState(child));
                    i2 = i + 1;
                    childCount2 = childCount;
                    heightMode2 = heightMode;
                }
            } else {
                lp = lp2;
                childState2 = childState;
                heightUsed2 = heightUsed;
                widthUsed2 = widthUsed;
                i = i2;
                childCount = childCount2;
                heightMode = heightMode2;
            }
            onMeasureChild(child, childWidthMeasureSpec, keylineWidthUsed, horizInsets, 0);
            LayoutParams lp32 = lp;
            childState3 = Math.max(widthUsed2, widthPadding + child.getMeasuredWidth() + lp32.leftMargin + lp32.rightMargin);
            heightUsed3 = Math.max(heightUsed2, heightPadding + child.getMeasuredHeight() + lp32.topMargin + lp32.bottomMargin);
            widthUsed4 = ViewCompat.combineMeasuredStates(childState2, ViewCompat.getMeasuredState(child));
            i2 = i + 1;
            childCount2 = childCount;
            heightMode2 = heightMode;
        }
        int childCount4 = widthUsed4;
        int i4 = heightUsed3;
        int heightUsed4 = childState3;
        int width = ViewCompat.resolveSizeAndState(heightUsed4, widthMeasureSpec, (-16777216) & childCount4);
        int height = ViewCompat.resolveSizeAndState(i4, heightMeasureSpec, childCount4 << 16);
        setMeasuredDimension(width, height);
    }

    private void dispatchChildApplyWindowInsets(WindowInsetsCompat insets) {
        if (insets.isConsumed()) {
            return;
        }
        int z = getChildCount();
        for (int i = 0; i < z; i++) {
            View child = getChildAt(i);
            if (ViewCompat.getFitsSystemWindows(child)) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                Behavior b = lp.getBehavior();
                if (b != null) {
                    insets = b.onApplyWindowInsets(this, child, insets);
                    if (insets.isConsumed()) {
                        return;
                    }
                }
                insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
                if (insets.isConsumed()) {
                    return;
                }
            }
        }
    }

    public void onLayoutChild(View child, int layoutDirection) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.checkAnchorChanged()) {
            throw new IllegalStateException("An anchor may not be changed after CoordinatorLayout measurement begins before layout is complete.");
        }
        if (lp.mAnchorView != null) {
            layoutChildWithAnchor(child, lp.mAnchorView, layoutDirection);
        } else if (lp.keyline >= 0) {
            layoutChildWithKeyline(child, lp.keyline, layoutDirection);
        } else {
            layoutChild(child, layoutDirection);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int childCount = this.mDependencySortedChildren.size();
        for (int i = 0; i < childCount; i++) {
            View child = this.mDependencySortedChildren.get(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Behavior behavior = lp.getBehavior();
            if (behavior == null || !behavior.onLayoutChild(this, child, layoutDirection)) {
                onLayoutChild(child, layoutDirection);
            }
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (this.mDrawStatusBarBackground && this.mStatusBarBackground != null) {
            WindowInsetsCompat windowInsetsCompat = this.mLastInsets;
            int inset = windowInsetsCompat != null ? windowInsetsCompat.getSystemWindowInsetTop() : 0;
            if (inset > 0) {
                this.mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
                this.mStatusBarBackground.draw(c);
            }
        }
    }

    void recordLastChildRect(View child, Rect r) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        lp.setLastChildRect(r);
    }

    void getLastChildRect(View child, Rect out) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        out.set(lp.getLastChildRect());
    }

    void getChildRect(View child, boolean transform, Rect out) {
        if (child.isLayoutRequested() || child.getVisibility() == 8) {
            out.set(0, 0, 0, 0);
        } else if (transform) {
            getDescendantRect(child, out);
        } else {
            out.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        }
    }

    void getDesiredAnchoredChildRect(View child, int layoutDirection, Rect anchorRect, Rect out) {
        int left;
        int top;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int absGravity = GravityCompat.getAbsoluteGravity(resolveAnchoredChildGravity(lp.gravity), layoutDirection);
        int absAnchorGravity = GravityCompat.getAbsoluteGravity(resolveGravity(lp.anchorGravity), layoutDirection);
        int hgrav = absGravity & 7;
        int vgrav = absGravity & 112;
        int anchorHgrav = absAnchorGravity & 7;
        int anchorVgrav = absAnchorGravity & 112;
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        switch (anchorHgrav) {
            case 1:
                left = anchorRect.left + (anchorRect.width() / 2);
                break;
            case 5:
                left = anchorRect.right;
                break;
            default:
                left = anchorRect.left;
                break;
        }
        switch (anchorVgrav) {
            case 16:
                top = anchorRect.top + (anchorRect.height() / 2);
                break;
            case 80:
                top = anchorRect.bottom;
                break;
            default:
                top = anchorRect.top;
                break;
        }
        switch (hgrav) {
            case 1:
                left -= childWidth / 2;
                break;
            case 5:
                break;
            default:
                left -= childWidth;
                break;
        }
        switch (vgrav) {
            case 16:
                top -= childHeight / 2;
                break;
            case 80:
                break;
            default:
                top -= childHeight;
                break;
        }
        int width = getWidth();
        int height = getHeight();
        int left2 = Math.max(getPaddingLeft() + lp.leftMargin, Math.min(left, ((width - getPaddingRight()) - childWidth) - lp.rightMargin));
        int top2 = Math.max(getPaddingTop() + lp.topMargin, Math.min(top, ((height - getPaddingBottom()) - childHeight) - lp.bottomMargin));
        int top3 = left2 + childWidth;
        out.set(left2, top2, top3, top2 + childHeight);
    }

    private void layoutChildWithAnchor(View child, View anchor, int layoutDirection) {
        Rect anchorRect = this.mTempRect1;
        Rect childRect = this.mTempRect2;
        getDescendantRect(anchor, anchorRect);
        getDesiredAnchoredChildRect(child, layoutDirection, anchorRect, childRect);
        child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
    }

    private void layoutChildWithKeyline(View child, int keyline, int layoutDirection) {
        int keyline2;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int absGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(lp.gravity), layoutDirection);
        int hgrav = absGravity & 7;
        int vgrav = absGravity & 112;
        int width = getWidth();
        int height = getHeight();
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        if (layoutDirection != 1) {
            keyline2 = keyline;
        } else {
            keyline2 = width - keyline;
        }
        int left = getKeyline(keyline2) - childWidth;
        int top = 0;
        switch (hgrav) {
            case 1:
                left += childWidth / 2;
                break;
            case 5:
                left += childWidth;
                break;
        }
        switch (vgrav) {
            case 16:
                top = 0 + (childHeight / 2);
                break;
            case 80:
                top = 0 + childHeight;
                break;
        }
        int left2 = Math.max(getPaddingLeft() + lp.leftMargin, Math.min(left, ((width - getPaddingRight()) - childWidth) - lp.rightMargin));
        int top2 = Math.max(getPaddingTop() + lp.topMargin, Math.min(top, ((height - getPaddingBottom()) - childHeight) - lp.bottomMargin));
        child.layout(left2, top2, left2 + childWidth, top2 + childHeight);
    }

    private void layoutChild(View child, int layoutDirection) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        Rect parent = this.mTempRect1;
        parent.set(getPaddingLeft() + lp.leftMargin, getPaddingTop() + lp.topMargin, (getWidth() - getPaddingRight()) - lp.rightMargin, (getHeight() - getPaddingBottom()) - lp.bottomMargin);
        if (this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this) && !ViewCompat.getFitsSystemWindows(child)) {
            parent.left += this.mLastInsets.getSystemWindowInsetLeft();
            parent.top += this.mLastInsets.getSystemWindowInsetTop();
            parent.right -= this.mLastInsets.getSystemWindowInsetRight();
            parent.bottom -= this.mLastInsets.getSystemWindowInsetBottom();
        }
        Rect out = this.mTempRect2;
        GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(), child.getMeasuredHeight(), parent, out, layoutDirection);
        child.layout(out.left, out.top, out.right, out.bottom);
    }

    private static int resolveGravity(int gravity) {
        if (gravity == 0) {
            return 8388659;
        }
        return gravity;
    }

    private static int resolveKeylineGravity(int gravity) {
        if (gravity == 0) {
            return 8388661;
        }
        return gravity;
    }

    private static int resolveAnchoredChildGravity(int gravity) {
        if (gravity == 0) {
            return 17;
        }
        return gravity;
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mBehavior != null && lp.mBehavior.getScrimOpacity(this, child) > 0.0f) {
            if (this.mScrimPaint == null) {
                this.mScrimPaint = new Paint();
            }
            this.mScrimPaint.setColor(lp.mBehavior.getScrimColor(this, child));
            canvas.drawRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom(), this.mScrimPaint);
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    void dispatchOnDependentViewChanged(boolean fromNestedScroll) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int childCount = this.mDependencySortedChildren.size();
        for (int i = 0; i < childCount; i++) {
            View child = this.mDependencySortedChildren.get(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            for (int j = 0; j < i; j++) {
                if (lp.mAnchorDirectChild == this.mDependencySortedChildren.get(j)) {
                    offsetChildToAnchor(child, layoutDirection);
                }
            }
            Rect oldRect = this.mTempRect1;
            Rect newRect = this.mTempRect2;
            getLastChildRect(child, oldRect);
            getChildRect(child, true, newRect);
            if (!oldRect.equals(newRect)) {
                recordLastChildRect(child, newRect);
                for (int j2 = i + 1; j2 < childCount; j2++) {
                    View checkChild = this.mDependencySortedChildren.get(j2);
                    LayoutParams checkLp = (LayoutParams) checkChild.getLayoutParams();
                    Behavior b = checkLp.getBehavior();
                    if (b != null && b.layoutDependsOn(this, checkChild, child)) {
                        if (!fromNestedScroll && checkLp.getChangedAfterNestedScroll()) {
                            checkLp.resetChangedAfterNestedScroll();
                        } else {
                            boolean handled = b.onDependentViewChanged(this, checkChild, child);
                            if (fromNestedScroll) {
                                checkLp.setChangedAfterNestedScroll(handled);
                            }
                        }
                    }
                }
            }
        }
    }

    void dispatchDependentViewRemoved(View view) {
        LayoutParams lp;
        Behavior b;
        int childCount = this.mDependencySortedChildren.size();
        boolean viewSeen = false;
        for (int i = 0; i < childCount; i++) {
            View child = this.mDependencySortedChildren.get(i);
            if (child == view) {
                viewSeen = true;
            } else if (viewSeen && (b = (lp = (LayoutParams) child.getLayoutParams()).getBehavior()) != null && lp.dependsOn(this, child, view)) {
                b.onDependentViewRemoved(this, child, view);
            }
        }
    }

    public void dispatchDependentViewsChanged(View view) {
        LayoutParams lp;
        Behavior b;
        int childCount = this.mDependencySortedChildren.size();
        boolean viewSeen = false;
        for (int i = 0; i < childCount; i++) {
            View child = this.mDependencySortedChildren.get(i);
            if (child == view) {
                viewSeen = true;
            } else if (viewSeen && (b = (lp = (LayoutParams) child.getLayoutParams()).getBehavior()) != null && lp.dependsOn(this, child, view)) {
                b.onDependentViewChanged(this, child, view);
            }
        }
    }

    public List<View> getDependencies(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        List<View> list = this.mTempDependenciesList;
        list.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View other = getChildAt(i);
            if (other != child && lp.dependsOn(this, child, other)) {
                list.add(other);
            }
        }
        return list;
    }

    void ensurePreDrawListener() {
        boolean hasDependencies = false;
        int childCount = getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                break;
            }
            View child = getChildAt(i);
            if (!hasDependencies(child)) {
                i++;
            } else {
                hasDependencies = true;
                break;
            }
        }
        if (hasDependencies != this.mNeedsPreDrawListener) {
            if (hasDependencies) {
                addPreDrawListener();
            } else {
                removePreDrawListener();
            }
        }
    }

    boolean hasDependencies(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mAnchorView != null) {
            return true;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View other = getChildAt(i);
            if (other != child && lp.dependsOn(this, child, other)) {
                return true;
            }
        }
        return false;
    }

    void addPreDrawListener() {
        if (this.mIsAttachedToWindow) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mNeedsPreDrawListener = true;
    }

    void removePreDrawListener() {
        if (this.mIsAttachedToWindow && this.mOnPreDrawListener != null) {
            ViewTreeObserver vto = getViewTreeObserver();
            vto.removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mNeedsPreDrawListener = false;
    }

    void offsetChildToAnchor(View child, int layoutDirection) {
        Behavior b;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mAnchorView != null) {
            Rect anchorRect = this.mTempRect1;
            Rect childRect = this.mTempRect2;
            Rect desiredChildRect = this.mTempRect3;
            getDescendantRect(lp.mAnchorView, anchorRect);
            getChildRect(child, false, childRect);
            getDesiredAnchoredChildRect(child, layoutDirection, anchorRect, desiredChildRect);
            int dx = desiredChildRect.left - childRect.left;
            int dy = desiredChildRect.top - childRect.top;
            if (dx != 0) {
                child.offsetLeftAndRight(dx);
            }
            if (dy != 0) {
                child.offsetTopAndBottom(dy);
            }
            if ((dx != 0 || dy != 0) && (b = lp.getBehavior()) != null) {
                b.onDependentViewChanged(this, child, lp.mAnchorView);
            }
        }
    }

    public boolean isPointInChildBounds(View child, int x, int y) {
        Rect r = this.mTempRect1;
        getDescendantRect(child, r);
        return r.contains(x, y);
    }

    public boolean doViewsOverlap(View first, View second) {
        if (first.getVisibility() != 0 || second.getVisibility() != 0) {
            return false;
        }
        Rect firstRect = this.mTempRect1;
        getChildRect(first, first.getParent() != this, firstRect);
        Rect secondRect = this.mTempRect2;
        getChildRect(second, second.getParent() != this, secondRect);
        return firstRect.left <= secondRect.right && firstRect.top <= secondRect.bottom && firstRect.right >= secondRect.left && firstRect.bottom >= secondRect.top;
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        }
        if (p instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return (p instanceof LayoutParams) && super.checkLayoutParams(p);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        boolean handled = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                boolean accepted = viewBehavior.onStartNestedScroll(this, view, child, target, nestedScrollAxes);
                handled |= accepted;
                lp.acceptNestedScroll(accepted);
            } else {
                lp.acceptNestedScroll(false);
            }
        }
        return handled;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Behavior viewBehavior;
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        this.mNestedScrollingDirectChild = child;
        this.mNestedScrollingTarget = target;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted() && (viewBehavior = lp.getBehavior()) != null) {
                viewBehavior.onNestedScrollAccepted(this, view, child, target, nestedScrollAxes);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onStopNestedScroll(View target) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(target);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted()) {
                Behavior viewBehavior = lp.getBehavior();
                if (viewBehavior != null) {
                    viewBehavior.onStopNestedScroll(this, view, target);
                }
                lp.resetNestedScroll();
                lp.resetChangedAfterNestedScroll();
            }
        }
        this.mNestedScrollingDirectChild = null;
        this.mNestedScrollingTarget = null;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Behavior viewBehavior;
        int childCount = getChildCount();
        boolean accepted = false;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted() && (viewBehavior = lp.getBehavior()) != null) {
                viewBehavior.onNestedScroll(this, view, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
                accepted = true;
            }
        }
        if (accepted) {
            dispatchOnDependentViewChanged(true);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Behavior viewBehavior;
        int childCount = getChildCount();
        int xConsumed = 0;
        int yConsumed = 0;
        boolean accepted = false;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted() && (viewBehavior = lp.getBehavior()) != null) {
                int[] iArr = this.mTempIntPair;
                iArr[1] = 0;
                iArr[0] = 0;
                viewBehavior.onNestedPreScroll(this, view, target, dx, dy, iArr);
                int[] iArr2 = this.mTempIntPair;
                int xConsumed2 = dx > 0 ? Math.max(xConsumed, iArr2[0]) : Math.min(xConsumed, iArr2[0]);
                int[] iArr3 = this.mTempIntPair;
                xConsumed = xConsumed2;
                yConsumed = dy > 0 ? Math.max(yConsumed, iArr3[1]) : Math.min(yConsumed, iArr3[1]);
                accepted = true;
            }
        }
        consumed[0] = xConsumed;
        consumed[1] = yConsumed;
        if (accepted) {
            dispatchOnDependentViewChanged(true);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Behavior viewBehavior;
        int childCount = getChildCount();
        boolean handled = false;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted() && (viewBehavior = lp.getBehavior()) != null) {
                handled = viewBehavior.onNestedFling(this, view, target, velocityX, velocityY, consumed) | handled;
            }
        }
        if (handled) {
            dispatchOnDependentViewChanged(true);
        }
        return handled;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Behavior viewBehavior;
        boolean handled = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted() && (viewBehavior = lp.getBehavior()) != null) {
                handled |= viewBehavior.onNestedPreFling(this, view, target, velocityX, velocityY);
            }
        }
        return handled;
    }

    @Override // android.view.ViewGroup, android.support.v4.view.NestedScrollingParent
    public int getNestedScrollAxes() {
        return this.mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    class OnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        OnPreDrawListener() {
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            CoordinatorLayout.this.dispatchOnDependentViewChanged(false);
            return true;
        }
    }

    static class ViewElevationComparator implements Comparator<View> {
        ViewElevationComparator() {
        }

        @Override // java.util.Comparator
        public int compare(View lhs, View rhs) {
            float lz = ViewCompat.getZ(lhs);
            float rz = ViewCompat.getZ(rhs);
            if (lz > rz) {
                return -1;
            }
            if (lz < rz) {
                return 1;
            }
            return 0;
        }
    }

    public static abstract class Behavior<V extends View> {
        public Behavior() {
        }

        public Behavior(Context context, AttributeSet attrs) {
        }

        public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
            return false;
        }

        public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
            return false;
        }

        public final int getScrimColor(CoordinatorLayout parent, V child) {
            return ViewCompat.MEASURED_STATE_MASK;
        }

        public final float getScrimOpacity(CoordinatorLayout parent, V child) {
            return 0.0f;
        }

        public boolean blocksInteractionBelow(CoordinatorLayout parent, V child) {
            return getScrimOpacity(parent, child) > 0.0f;
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
            return false;
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
            return false;
        }

        public void onDependentViewRemoved(CoordinatorLayout parent, V child, View dependency) {
        }

        public boolean isDirty(CoordinatorLayout parent, V child) {
            return false;
        }

        public boolean onMeasureChild(CoordinatorLayout parent, V child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            return false;
        }

        public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
            return false;
        }

        public static void setTag(View child, Object tag) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.mBehaviorTag = tag;
        }

        public static Object getTag(View child) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            return lp.mBehaviorTag;
        }

        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
            return false;
        }

        public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
        }

        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
        }

        public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        }

        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed) {
        }

        public boolean onNestedFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY, boolean consumed) {
            return false;
        }

        public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY) {
            return false;
        }

        public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, V child, WindowInsetsCompat insets) {
            return insets;
        }

        public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state) {
        }

        public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
            return View.BaseSavedState.EMPTY_STATE;
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int anchorGravity;
        public int gravity;
        public int keyline;
        View mAnchorDirectChild;
        int mAnchorId;
        View mAnchorView;
        Behavior mBehavior;
        boolean mBehaviorResolved;
        Object mBehaviorTag;
        private boolean mDidAcceptNestedScroll;
        private boolean mDidBlockInteraction;
        private boolean mDidChangeAfterNestedScroll;
        final Rect mLastChildRect;

        public LayoutParams(int width, int height) {
            super(width, height);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.mLastChildRect = new Rect();
        }

        LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.mLastChildRect = new Rect();
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CoordinatorLayout_LayoutParams);
            this.gravity = a.getInteger(R.styleable.CoordinatorLayout_LayoutParams_android_layout_gravity, 0);
            this.mAnchorId = a.getResourceId(R.styleable.CoordinatorLayout_LayoutParams_layout_anchor, -1);
            this.anchorGravity = a.getInteger(R.styleable.CoordinatorLayout_LayoutParams_layout_anchorGravity, 0);
            this.keyline = a.getInteger(R.styleable.CoordinatorLayout_LayoutParams_layout_keyline, -1);
            boolean hasValue = a.hasValue(R.styleable.CoordinatorLayout_LayoutParams_layout_behavior);
            this.mBehaviorResolved = hasValue;
            if (hasValue) {
                this.mBehavior = CoordinatorLayout.parseBehavior(context, attrs, a.getString(R.styleable.CoordinatorLayout_LayoutParams_layout_behavior));
            }
            a.recycle();
        }

        public LayoutParams(LayoutParams p) {
            super((ViewGroup.MarginLayoutParams) p);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.mLastChildRect = new Rect();
        }

        public LayoutParams(ViewGroup.MarginLayoutParams p) {
            super(p);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.mLastChildRect = new Rect();
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.mLastChildRect = new Rect();
        }

        public int getAnchorId() {
            return this.mAnchorId;
        }

        public void setAnchorId(int id) {
            invalidateAnchor();
            this.mAnchorId = id;
        }

        public Behavior getBehavior() {
            return this.mBehavior;
        }

        public void setBehavior(Behavior behavior) {
            if (this.mBehavior != behavior) {
                this.mBehavior = behavior;
                this.mBehaviorTag = null;
                this.mBehaviorResolved = true;
            }
        }

        void setLastChildRect(Rect r) {
            this.mLastChildRect.set(r);
        }

        Rect getLastChildRect() {
            return this.mLastChildRect;
        }

        boolean checkAnchorChanged() {
            return this.mAnchorView == null && this.mAnchorId != -1;
        }

        boolean didBlockInteraction() {
            if (this.mBehavior == null) {
                this.mDidBlockInteraction = false;
            }
            return this.mDidBlockInteraction;
        }

        boolean isBlockingInteractionBelow(CoordinatorLayout parent, View child) {
            boolean z = this.mDidBlockInteraction;
            if (z) {
                return true;
            }
            Behavior behavior = this.mBehavior;
            boolean blocksInteractionBelow = z | (behavior != null ? behavior.blocksInteractionBelow(parent, child) : false);
            this.mDidBlockInteraction = blocksInteractionBelow;
            return blocksInteractionBelow;
        }

        void resetTouchBehaviorTracking() {
            this.mDidBlockInteraction = false;
        }

        void resetNestedScroll() {
            this.mDidAcceptNestedScroll = false;
        }

        void acceptNestedScroll(boolean accept) {
            this.mDidAcceptNestedScroll = accept;
        }

        boolean isNestedScrollAccepted() {
            return this.mDidAcceptNestedScroll;
        }

        boolean getChangedAfterNestedScroll() {
            return this.mDidChangeAfterNestedScroll;
        }

        void setChangedAfterNestedScroll(boolean changed) {
            this.mDidChangeAfterNestedScroll = changed;
        }

        void resetChangedAfterNestedScroll() {
            this.mDidChangeAfterNestedScroll = false;
        }

        boolean dependsOn(CoordinatorLayout parent, View child, View dependency) {
            Behavior behavior;
            return dependency == this.mAnchorDirectChild || ((behavior = this.mBehavior) != null && behavior.layoutDependsOn(parent, child, dependency));
        }

        void invalidateAnchor() {
            this.mAnchorDirectChild = null;
            this.mAnchorView = null;
        }

        View findAnchorView(CoordinatorLayout parent, View forChild) {
            if (this.mAnchorId == -1) {
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
                return null;
            }
            if (this.mAnchorView == null || !verifyAnchorView(forChild, parent)) {
                resolveAnchorView(forChild, parent);
            }
            return this.mAnchorView;
        }

        boolean isDirty(CoordinatorLayout parent, View child) {
            Behavior behavior = this.mBehavior;
            return behavior != null && behavior.isDirty(parent, child);
        }

        private void resolveAnchorView(View forChild, CoordinatorLayout parent) {
            View findViewById = parent.findViewById(this.mAnchorId);
            this.mAnchorView = findViewById;
            if (findViewById != null) {
                View directChild = this.mAnchorView;
                for (ViewParent p = findViewById.getParent(); p != parent && p != null; p = p.getParent()) {
                    if (p == forChild) {
                        if (parent.isInEditMode()) {
                            this.mAnchorDirectChild = null;
                            this.mAnchorView = null;
                            return;
                        }
                        throw new IllegalStateException("Anchor must not be a descendant of the anchored view");
                    }
                    if (p instanceof View) {
                        directChild = (View) p;
                    }
                }
                this.mAnchorDirectChild = directChild;
                return;
            }
            if (parent.isInEditMode()) {
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
                return;
            }
            throw new IllegalStateException("Could not find CoordinatorLayout descendant view with id " + parent.getResources().getResourceName(this.mAnchorId) + " to anchor view " + forChild);
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r1v2, types: [android.view.ViewParent] */
        private boolean verifyAnchorView(View view, CoordinatorLayout coordinatorLayout) {
            if (this.mAnchorView.getId() != this.mAnchorId) {
                return false;
            }
            CoordinatorLayout coordinatorLayout2 = this.mAnchorView;
            for (CoordinatorLayout coordinatorLayout3 = this.mAnchorView.getParent(); coordinatorLayout3 != coordinatorLayout; coordinatorLayout3 = coordinatorLayout3.getParent()) {
                if (coordinatorLayout3 == null || coordinatorLayout3 == view) {
                    this.mAnchorDirectChild = null;
                    this.mAnchorView = null;
                    return false;
                }
                if (coordinatorLayout3 instanceof View) {
                    coordinatorLayout2 = coordinatorLayout3;
                }
            }
            this.mAnchorDirectChild = coordinatorLayout2;
            return true;
        }
    }

    final class ApplyInsetsListener implements OnApplyWindowInsetsListener {
        ApplyInsetsListener() {
        }

        @Override // android.support.v4.view.OnApplyWindowInsetsListener
        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            CoordinatorLayout.this.setWindowInsets(insets);
            return insets.consumeSystemWindowInsets();
        }
    }

    final class HierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
        HierarchyChangeListener() {
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewAdded(View parent, View child) {
            if (CoordinatorLayout.this.mOnHierarchyChangeListener != null) {
                CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewRemoved(View parent, View child) {
            CoordinatorLayout.this.dispatchDependentViewRemoved(child);
            if (CoordinatorLayout.this.mOnHierarchyChangeListener != null) {
                CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable state) {
        Parcelable savedState;
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        SparseArray<Parcelable> behaviorStates = ss.behaviorStates;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childId = child.getId();
            LayoutParams lp = getResolvedLayoutParams(child);
            Behavior b = lp.getBehavior();
            if (childId != -1 && b != null && (savedState = behaviorStates.get(childId)) != null) {
                b.onRestoreInstanceState(this, child, savedState);
            }
        }
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable state;
        SavedState ss = new SavedState(super.onSaveInstanceState());
        SparseArray<Parcelable> behaviorStates = new SparseArray<>();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childId = child.getId();
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Behavior b = lp.getBehavior();
            if (childId != -1 && b != null && (state = b.onSaveInstanceState(this, child)) != null) {
                behaviorStates.append(childId, state);
            }
        }
        ss.behaviorStates = behaviorStates;
        return ss;
    }

    protected static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() { // from class: android.support.design.widget.CoordinatorLayout.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
        SparseArray<Parcelable> behaviorStates;

        public SavedState(Parcel source, ClassLoader loader) {
            super(source);
            int size = source.readInt();
            int[] ids = new int[size];
            source.readIntArray(ids);
            Parcelable[] states = source.readParcelableArray(loader);
            this.behaviorStates = new SparseArray<>(size);
            for (int i = 0; i < size; i++) {
                this.behaviorStates.append(ids[i], states[i]);
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            SparseArray<Parcelable> sparseArray = this.behaviorStates;
            int size = sparseArray != null ? sparseArray.size() : 0;
            dest.writeInt(size);
            int[] ids = new int[size];
            Parcelable[] states = new Parcelable[size];
            for (int i = 0; i < size; i++) {
                ids[i] = this.behaviorStates.keyAt(i);
                states[i] = this.behaviorStates.valueAt(i);
            }
            dest.writeIntArray(ids);
            dest.writeParcelableArray(states, flags);
        }
    }

    private static void selectionSort(List<View> list, Comparator<View> comparator) {
        if (list == null || list.size() < 2) {
            return;
        }
        View[] array = new View[list.size()];
        list.toArray(array);
        int count = array.length;
        for (int i = 0; i < count; i++) {
            int min = i;
            for (int j = i + 1; j < count; j++) {
                if (comparator.compare(array[j], array[min]) < 0) {
                    min = j;
                }
            }
            if (i != min) {
                View minItem = array[min];
                array[min] = array[i];
                array[i] = minItem;
            }
        }
        list.clear();
        for (View view : array) {
            list.add(view);
        }
    }
}
