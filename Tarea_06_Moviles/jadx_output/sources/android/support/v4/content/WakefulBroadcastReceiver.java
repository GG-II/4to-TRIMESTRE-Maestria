package android.support.v4.content;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.util.SparseArray;

/* loaded from: classes.dex */
public abstract class WakefulBroadcastReceiver extends BroadcastReceiver {
    private static final String EXTRA_WAKE_LOCK_ID = "android.support.content.wakelockid";
    private static final SparseArray<PowerManager.WakeLock> mActiveWakeLocks = new SparseArray<>();
    private static int mNextId = 1;

    public static ComponentName startWakefulService(Context context, Intent intent) {
        SparseArray<PowerManager.WakeLock> sparseArray = mActiveWakeLocks;
        synchronized (sparseArray) {
            int id = mNextId;
            int i = id + 1;
            mNextId = i;
            if (i <= 0) {
                mNextId = 1;
            }
            intent.putExtra(EXTRA_WAKE_LOCK_ID, id);
            ComponentName comp = context.startService(intent);
            if (comp == null) {
                return null;
            }
            PowerManager pm = (PowerManager) context.getSystemService("power");
            PowerManager.WakeLock wl = pm.newWakeLock(1, "wake:" + comp.flattenToShortString());
            wl.setReferenceCounted(false);
            wl.acquire(60000L);
            sparseArray.put(id, wl);
            return comp;
        }
    }

    public static boolean completeWakefulIntent(Intent intent) {
        int id = intent.getIntExtra(EXTRA_WAKE_LOCK_ID, 0);
        if (id == 0) {
            return false;
        }
        SparseArray<PowerManager.WakeLock> sparseArray = mActiveWakeLocks;
        synchronized (sparseArray) {
            PowerManager.WakeLock wl = sparseArray.get(id);
            if (wl != null) {
                wl.release();
                sparseArray.remove(id);
                return true;
            }
            Log.w("WakefulBroadcastReceiver", "No active wake lock id #" + id);
            return true;
        }
    }
}
