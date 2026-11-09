package android.support.design.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.internal.widget.ActivityChooserView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

/* loaded from: classes.dex */
abstract class HeaderBehavior<V extends View> extends ViewOffsetBehavior<V> {
    private static final int INVALID_POINTER = -1;
    private int mActivePointerId;
    private Runnable mFlingRunnable;
    private boolean mIsBeingDragged;
    private int mLastMotionY;
    private ScrollerCompat mScroller;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    public HeaderBehavior() {
        this.mActivePointerId = -1;
        this.mTouchSlop = -1;
    }

    public HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mActivePointerId = -1;
        this.mTouchSlop = -1;
    }

    @Override // android.support.design.widget.CoordinatorLayout.Behavior
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        int pointerIndex;
        if (this.mTouchSlop < 0) {
            this.mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }
        int action = ev.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        switch (MotionEventCompat.getActionMasked(ev)) {
            case 0:
                this.mIsBeingDragged = false;
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                if (canDragView(child) && parent.isPointInChildBounds(child, x, y)) {
                    this.mLastMotionY = y;
                    this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    ensureVelocityTracker();
                    break;
                }
                break;
            case 1:
            case 3:
                this.mIsBeingDragged = false;
                this.mActivePointerId = -1;
                VelocityTracker velocityTracker = this.mVelocityTracker;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    this.mVelocityTracker = null;
                    break;
                }
                break;
            case 2:
                int activePointerId = this.mActivePointerId;
                if (activePointerId != -1 && (pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId)) != -1) {
                    int y2 = (int) MotionEventCompat.getY(ev, pointerIndex);
                    int yDiff = Math.abs(y2 - this.mLastMotionY);
                    if (yDiff > this.mTouchSlop) {
                        this.mIsBeingDragged = true;
                        this.mLastMotionY = y2;
                        break;
                    }
                }
                break;
        }
        VelocityTracker velocityTracker2 = this.mVelocityTracker;
        if (velocityTracker2 != null) {
            velocityTracker2.addMovement(ev);
        }
        return this.mIsBeingDragged;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0080  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x00ae  */
    @Override // android.support.design.widget.CoordinatorLayout.Behavior
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean onTouchEvent(android.support.design.widget.CoordinatorLayout r12, V r13, android.view.MotionEvent r14) {
        /*
            r11 = this;
            int r0 = r11.mTouchSlop
            if (r0 >= 0) goto L12
            android.content.Context r0 = r12.getContext()
            android.view.ViewConfiguration r0 = android.view.ViewConfiguration.get(r0)
            int r0 = r0.getScaledTouchSlop()
            r11.mTouchSlop = r0
        L12:
            int r0 = android.support.v4.view.MotionEventCompat.getActionMasked(r14)
            r1 = 1
            r2 = -1
            r3 = 0
            switch(r0) {
                case 0: goto L87;
                case 1: goto L55;
                case 2: goto L1e;
                case 3: goto L78;
                default: goto L1c;
            }
        L1c:
            goto Laa
        L1e:
            int r0 = r11.mActivePointerId
            int r0 = android.support.v4.view.MotionEventCompat.findPointerIndex(r14, r0)
            if (r0 != r2) goto L27
            return r3
        L27:
            float r2 = android.support.v4.view.MotionEventCompat.getY(r14, r0)
            int r2 = (int) r2
            int r3 = r11.mLastMotionY
            int r3 = r3 - r2
            boolean r4 = r11.mIsBeingDragged
            if (r4 != 0) goto L42
            int r4 = java.lang.Math.abs(r3)
            int r5 = r11.mTouchSlop
            if (r4 <= r5) goto L42
            r11.mIsBeingDragged = r1
            if (r3 <= 0) goto L41
            int r3 = r3 - r5
            goto L42
        L41:
            int r3 = r3 + r5
        L42:
            boolean r4 = r11.mIsBeingDragged
            if (r4 == 0) goto Laa
            r11.mLastMotionY = r2
            int r8 = r11.getMaxDragOffset(r13)
            r9 = 0
            r4 = r11
            r5 = r12
            r6 = r13
            r7 = r3
            r4.scroll(r5, r6, r7, r8, r9)
            goto Laa
        L55:
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            if (r0 == 0) goto L78
            r0.addMovement(r14)
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            r4 = 1000(0x3e8, float:1.401E-42)
            r0.computeCurrentVelocity(r4)
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            int r4 = r11.mActivePointerId
            float r0 = android.support.v4.view.VelocityTrackerCompat.getYVelocity(r0, r4)
            int r4 = r11.getScrollRangeForDragFling(r13)
            int r8 = -r4
            r9 = 0
            r5 = r11
            r6 = r12
            r7 = r13
            r10 = r0
            r5.fling(r6, r7, r8, r9, r10)
        L78:
            r11.mIsBeingDragged = r3
            r11.mActivePointerId = r2
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            if (r0 == 0) goto Laa
            r0.recycle()
            r0 = 0
            r11.mVelocityTracker = r0
            goto Laa
        L87:
            float r0 = r14.getX()
            int r0 = (int) r0
            float r2 = r14.getY()
            int r2 = (int) r2
            boolean r4 = r12.isPointInChildBounds(r13, r0, r2)
            if (r4 == 0) goto La9
            boolean r4 = r11.canDragView(r13)
            if (r4 == 0) goto La9
            r11.mLastMotionY = r2
            int r3 = android.support.v4.view.MotionEventCompat.getPointerId(r14, r3)
            r11.mActivePointerId = r3
            r11.ensureVelocityTracker()
            goto Laa
        La9:
            return r3
        Laa:
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            if (r0 == 0) goto Lb1
            r0.addMovement(r14)
        Lb1:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.design.widget.HeaderBehavior.onTouchEvent(android.support.design.widget.CoordinatorLayout, android.view.View, android.view.MotionEvent):boolean");
    }

    int setHeaderTopBottomOffset(CoordinatorLayout parent, V header, int newOffset) {
        return setHeaderTopBottomOffset(parent, header, newOffset, Integer.MIN_VALUE, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    }

    int setHeaderTopBottomOffset(CoordinatorLayout parent, V header, int newOffset, int minOffset, int maxOffset) {
        int newOffset2;
        int curOffset = getTopAndBottomOffset();
        if (minOffset == 0 || curOffset < minOffset || curOffset > maxOffset || curOffset == (newOffset2 = MathUtils.constrain(newOffset, minOffset, maxOffset))) {
            return 0;
        }
        setTopAndBottomOffset(newOffset2);
        int consumed = curOffset - newOffset2;
        return consumed;
    }

    int getTopBottomOffsetForScrollingSibling() {
        return getTopAndBottomOffset();
    }

    final int scroll(CoordinatorLayout coordinatorLayout, V header, int dy, int minOffset, int maxOffset) {
        return setHeaderTopBottomOffset(coordinatorLayout, header, getTopBottomOffsetForScrollingSibling() - dy, minOffset, maxOffset);
    }

    final boolean fling(CoordinatorLayout coordinatorLayout, V layout, int minOffset, int maxOffset, float velocityY) {
        Runnable runnable = this.mFlingRunnable;
        if (runnable != null) {
            layout.removeCallbacks(runnable);
            this.mFlingRunnable = null;
        }
        if (this.mScroller == null) {
            this.mScroller = ScrollerCompat.create(layout.getContext());
        }
        this.mScroller.fling(0, getTopAndBottomOffset(), 0, Math.round(velocityY), 0, 0, minOffset, maxOffset);
        if (this.mScroller.computeScrollOffset()) {
            FlingRunnable flingRunnable = new FlingRunnable(coordinatorLayout, layout);
            this.mFlingRunnable = flingRunnable;
            ViewCompat.postOnAnimation(layout, flingRunnable);
            return true;
        }
        return false;
    }

    boolean canDragView(V view) {
        return false;
    }

    int getMaxDragOffset(V view) {
        return -view.getHeight();
    }

    int getScrollRangeForDragFling(V view) {
        return view.getHeight();
    }

    private void ensureVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private class FlingRunnable implements Runnable {
        private final V mLayout;
        private final CoordinatorLayout mParent;

        FlingRunnable(CoordinatorLayout parent, V layout) {
            this.mParent = parent;
            this.mLayout = layout;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mLayout != null && HeaderBehavior.this.mScroller != null && HeaderBehavior.this.mScroller.computeScrollOffset()) {
                HeaderBehavior headerBehavior = HeaderBehavior.this;
                headerBehavior.setHeaderTopBottomOffset(this.mParent, this.mLayout, headerBehavior.mScroller.getCurrY());
                ViewCompat.postOnAnimation(this.mLayout, this);
            }
        }
    }
}
