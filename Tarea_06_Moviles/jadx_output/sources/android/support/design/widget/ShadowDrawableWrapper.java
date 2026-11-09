package android.support.design.widget;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.design.R;
import android.support.v7.graphics.drawable.DrawableWrapper;

/* loaded from: classes.dex */
class ShadowDrawableWrapper extends DrawableWrapper {
    static final double COS_45 = Math.cos(Math.toRadians(45.0d));
    static final float SHADOW_BOTTOM_SCALE = 1.0f;
    static final float SHADOW_HORIZ_SCALE = 0.5f;
    static final float SHADOW_MULTIPLIER = 1.5f;
    static final float SHADOW_TOP_SCALE = 0.25f;
    private boolean mAddPaddingForCorners;
    final RectF mContentBounds;
    float mCornerRadius;
    final Paint mCornerShadowPaint;
    Path mCornerShadowPath;
    private boolean mDirty;
    final Paint mEdgeShadowPaint;
    float mMaxShadowSize;
    private boolean mPrintedShadowClipWarning;
    float mRawMaxShadowSize;
    float mRawShadowSize;
    private final int mShadowEndColor;
    private final int mShadowMiddleColor;
    float mShadowSize;
    private final int mShadowStartColor;

    public ShadowDrawableWrapper(Resources resources, Drawable content, float radius, float shadowSize, float maxShadowSize) {
        super(content);
        this.mDirty = true;
        this.mAddPaddingForCorners = true;
        this.mPrintedShadowClipWarning = false;
        this.mShadowStartColor = resources.getColor(R.color.design_fab_shadow_start_color);
        this.mShadowMiddleColor = resources.getColor(R.color.design_fab_shadow_mid_color);
        this.mShadowEndColor = resources.getColor(R.color.design_fab_shadow_end_color);
        Paint paint = new Paint(5);
        this.mCornerShadowPaint = paint;
        paint.setStyle(Paint.Style.FILL);
        this.mCornerRadius = Math.round(radius);
        this.mContentBounds = new RectF();
        Paint paint2 = new Paint(paint);
        this.mEdgeShadowPaint = paint2;
        paint2.setAntiAlias(false);
        setShadowSize(shadowSize, maxShadowSize);
    }

    private static int toEven(float value) {
        int i = Math.round(value);
        return i % 2 == 1 ? i - 1 : i;
    }

    public void setAddPaddingForCorners(boolean addPaddingForCorners) {
        this.mAddPaddingForCorners = addPaddingForCorners;
        invalidateSelf();
    }

    @Override // android.support.v7.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        this.mCornerShadowPaint.setAlpha(alpha);
        this.mEdgeShadowPaint.setAlpha(alpha);
    }

    @Override // android.support.v7.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect bounds) {
        this.mDirty = true;
    }

    void setShadowSize(float shadowSize, float maxShadowSize) {
        if (shadowSize < 0.0f || maxShadowSize < 0.0f) {
            throw new IllegalArgumentException("invalid shadow size");
        }
        float shadowSize2 = toEven(shadowSize);
        float maxShadowSize2 = toEven(maxShadowSize);
        if (shadowSize2 > maxShadowSize2) {
            shadowSize2 = maxShadowSize2;
            if (!this.mPrintedShadowClipWarning) {
                this.mPrintedShadowClipWarning = true;
            }
        }
        if (this.mRawShadowSize == shadowSize2 && this.mRawMaxShadowSize == maxShadowSize2) {
            return;
        }
        this.mRawShadowSize = shadowSize2;
        this.mRawMaxShadowSize = maxShadowSize2;
        this.mShadowSize = Math.round(SHADOW_MULTIPLIER * shadowSize2);
        this.mMaxShadowSize = maxShadowSize2;
        this.mDirty = true;
        invalidateSelf();
    }

    @Override // android.support.v7.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        int vOffset = (int) Math.ceil(calculateVerticalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners));
        int hOffset = (int) Math.ceil(calculateHorizontalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners));
        padding.set(hOffset, vOffset, hOffset, vOffset);
        return true;
    }

    public static float calculateVerticalPadding(float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
        if (addPaddingForCorners) {
            double d = SHADOW_MULTIPLIER * maxShadowSize;
            double d2 = 1.0d - COS_45;
            double d3 = cornerRadius;
            Double.isNaN(d3);
            Double.isNaN(d);
            return (float) (d + (d2 * d3));
        }
        return SHADOW_MULTIPLIER * maxShadowSize;
    }

    public static float calculateHorizontalPadding(float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
        if (addPaddingForCorners) {
            double d = maxShadowSize;
            double d2 = 1.0d - COS_45;
            double d3 = cornerRadius;
            Double.isNaN(d3);
            Double.isNaN(d);
            return (float) (d + (d2 * d3));
        }
        return maxShadowSize;
    }

    @Override // android.support.v7.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    public void setCornerRadius(float radius) {
        float radius2 = Math.round(radius);
        if (this.mCornerRadius == radius2) {
            return;
        }
        this.mCornerRadius = radius2;
        this.mDirty = true;
        invalidateSelf();
    }

    @Override // android.support.v7.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.mDirty) {
            buildComponents(getBounds());
            this.mDirty = false;
        }
        drawShadow(canvas);
        super.draw(canvas);
    }

    private void drawShadow(Canvas canvas) {
        int saved;
        float shadowScaleBottom;
        float shadowScaleTop;
        float shadowOffsetTop;
        float edgeShadowTop = (-this.mCornerRadius) - this.mShadowSize;
        float shadowOffset = this.mCornerRadius;
        boolean drawHorizontalEdges = this.mContentBounds.width() - (shadowOffset * 2.0f) > 0.0f;
        boolean drawVerticalEdges = this.mContentBounds.height() - (shadowOffset * 2.0f) > 0.0f;
        float f = this.mRawShadowSize;
        float shadowOffsetTop2 = f - (SHADOW_TOP_SCALE * f);
        float shadowOffsetHorizontal = f - (SHADOW_HORIZ_SCALE * f);
        float shadowOffsetBottom = f - (f * SHADOW_BOTTOM_SCALE);
        float shadowScaleHorizontal = shadowOffset / (shadowOffset + shadowOffsetHorizontal);
        float shadowScaleTop2 = shadowOffset / (shadowOffset + shadowOffsetTop2);
        float shadowScaleBottom2 = shadowOffset / (shadowOffset + shadowOffsetBottom);
        int saved2 = canvas.save();
        canvas.translate(this.mContentBounds.left + shadowOffset, this.mContentBounds.top + shadowOffset);
        canvas.scale(shadowScaleHorizontal, shadowScaleTop2);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (drawHorizontalEdges) {
            canvas.scale(SHADOW_BOTTOM_SCALE / shadowScaleHorizontal, SHADOW_BOTTOM_SCALE);
            float shadowOffsetTop3 = this.mContentBounds.width() - (shadowOffset * 2.0f);
            saved = saved2;
            shadowScaleBottom = shadowScaleBottom2;
            shadowScaleTop = shadowScaleTop2;
            shadowOffsetTop = shadowScaleHorizontal;
            canvas.drawRect(0.0f, edgeShadowTop, shadowOffsetTop3, -this.mCornerRadius, this.mEdgeShadowPaint);
        } else {
            saved = saved2;
            shadowScaleBottom = shadowScaleBottom2;
            shadowScaleTop = shadowScaleTop2;
            shadowOffsetTop = shadowScaleHorizontal;
        }
        canvas.restoreToCount(saved);
        int saved3 = canvas.save();
        canvas.translate(this.mContentBounds.right - shadowOffset, this.mContentBounds.bottom - shadowOffset);
        float shadowScaleBottom3 = shadowScaleBottom;
        canvas.scale(shadowOffsetTop, shadowScaleBottom3);
        canvas.rotate(180.0f);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (drawHorizontalEdges) {
            canvas.scale(SHADOW_BOTTOM_SCALE / shadowOffsetTop, SHADOW_BOTTOM_SCALE);
            canvas.drawRect(0.0f, edgeShadowTop, this.mContentBounds.width() - (shadowOffset * 2.0f), (-this.mCornerRadius) + this.mShadowSize, this.mEdgeShadowPaint);
        }
        canvas.restoreToCount(saved3);
        int saved4 = canvas.save();
        canvas.translate(this.mContentBounds.left + shadowOffset, this.mContentBounds.bottom - shadowOffset);
        canvas.scale(shadowOffsetTop, shadowScaleBottom3);
        canvas.rotate(270.0f);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (drawVerticalEdges) {
            canvas.scale(SHADOW_BOTTOM_SCALE / shadowScaleBottom3, SHADOW_BOTTOM_SCALE);
            canvas.drawRect(0.0f, edgeShadowTop, this.mContentBounds.height() - (shadowOffset * 2.0f), -this.mCornerRadius, this.mEdgeShadowPaint);
        }
        canvas.restoreToCount(saved4);
        int saved5 = canvas.save();
        canvas.translate(this.mContentBounds.right - shadowOffset, this.mContentBounds.top + shadowOffset);
        float shadowScaleTop3 = shadowScaleTop;
        canvas.scale(shadowOffsetTop, shadowScaleTop3);
        canvas.rotate(90.0f);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (drawVerticalEdges) {
            canvas.scale(SHADOW_BOTTOM_SCALE / shadowScaleTop3, SHADOW_BOTTOM_SCALE);
            canvas.drawRect(0.0f, edgeShadowTop, this.mContentBounds.height() - (2.0f * shadowOffset), -this.mCornerRadius, this.mEdgeShadowPaint);
        }
        canvas.restoreToCount(saved5);
    }

    private void buildShadowCorners() {
        float f = this.mCornerRadius;
        RectF innerBounds = new RectF(-f, -f, f, f);
        RectF outerBounds = new RectF(innerBounds);
        float f2 = this.mShadowSize;
        outerBounds.inset(-f2, -f2);
        Path path = this.mCornerShadowPath;
        if (path == null) {
            this.mCornerShadowPath = new Path();
        } else {
            path.reset();
        }
        this.mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
        this.mCornerShadowPath.moveTo(-this.mCornerRadius, 0.0f);
        this.mCornerShadowPath.rLineTo(-this.mShadowSize, 0.0f);
        this.mCornerShadowPath.arcTo(outerBounds, 180.0f, 90.0f, false);
        this.mCornerShadowPath.arcTo(innerBounds, 270.0f, -90.0f, false);
        this.mCornerShadowPath.close();
        float shadowRadius = -outerBounds.top;
        if (shadowRadius > 0.0f) {
            float startRatio = this.mCornerRadius / shadowRadius;
            float midRatio = startRatio + ((SHADOW_BOTTOM_SCALE - startRatio) / 2.0f);
            this.mCornerShadowPaint.setShader(new RadialGradient(0.0f, 0.0f, shadowRadius, new int[]{0, this.mShadowStartColor, this.mShadowMiddleColor, this.mShadowEndColor}, new float[]{0.0f, startRatio, midRatio, SHADOW_BOTTOM_SCALE}, Shader.TileMode.CLAMP));
        }
        this.mEdgeShadowPaint.setShader(new LinearGradient(0.0f, innerBounds.top, 0.0f, outerBounds.top, new int[]{this.mShadowStartColor, this.mShadowMiddleColor, this.mShadowEndColor}, new float[]{0.0f, SHADOW_HORIZ_SCALE, SHADOW_BOTTOM_SCALE}, Shader.TileMode.CLAMP));
        this.mEdgeShadowPaint.setAntiAlias(false);
    }

    private void buildComponents(Rect bounds) {
        float verticalOffset = this.mRawMaxShadowSize * SHADOW_MULTIPLIER;
        this.mContentBounds.set(bounds.left + this.mRawMaxShadowSize, bounds.top + verticalOffset, bounds.right - this.mRawMaxShadowSize, bounds.bottom - verticalOffset);
        getWrappedDrawable().setBounds((int) this.mContentBounds.left, (int) this.mContentBounds.top, (int) this.mContentBounds.right, (int) this.mContentBounds.bottom);
        buildShadowCorners();
    }

    public float getCornerRadius() {
        return this.mCornerRadius;
    }

    public void setShadowSize(float size) {
        setShadowSize(size, this.mRawMaxShadowSize);
    }

    public void setMaxShadowSize(float size) {
        setShadowSize(this.mRawShadowSize, size);
    }

    public float getShadowSize() {
        return this.mRawShadowSize;
    }

    public float getMaxShadowSize() {
        return this.mRawMaxShadowSize;
    }

    public float getMinWidth() {
        float f = this.mRawMaxShadowSize;
        float content = Math.max(f, this.mCornerRadius + (f / 2.0f)) * 2.0f;
        return (this.mRawMaxShadowSize * 2.0f) + content;
    }

    public float getMinHeight() {
        float f = this.mRawMaxShadowSize;
        float content = Math.max(f, this.mCornerRadius + ((f * SHADOW_MULTIPLIER) / 2.0f)) * 2.0f;
        return (this.mRawMaxShadowSize * SHADOW_MULTIPLIER * 2.0f) + content;
    }
}
