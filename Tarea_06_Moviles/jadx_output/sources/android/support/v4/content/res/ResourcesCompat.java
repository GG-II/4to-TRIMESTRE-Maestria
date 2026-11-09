package android.support.v4.content.res;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

/* loaded from: classes.dex */
public class ResourcesCompat {
    public static Drawable getDrawable(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 21) {
            return ResourcesCompatApi21.getDrawable(res, id, theme);
        }
        return res.getDrawable(id);
    }

    public static Drawable getDrawableForDensity(Resources res, int id, int density, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 21) {
            return ResourcesCompatApi21.getDrawableForDensity(res, id, density, theme);
        }
        return ResourcesCompatIcsMr1.getDrawableForDensity(res, id, density);
    }

    public int getColor(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 23) {
            return ResourcesCompatApi23.getColor(res, id, theme);
        }
        return res.getColor(id);
    }

    public ColorStateList getColorStateList(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 23) {
            return ResourcesCompatApi23.getColorStateList(res, id, theme);
        }
        return res.getColorStateList(id);
    }
}
