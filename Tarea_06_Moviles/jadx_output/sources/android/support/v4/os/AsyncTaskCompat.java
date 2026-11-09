package android.support.v4.os;

import android.os.AsyncTask;

/* loaded from: classes.dex */
public class AsyncTaskCompat {
    public static <Params, Progress, Result> AsyncTask<Params, Progress, Result> executeParallel(AsyncTask<Params, Progress, Result> task, Params... params) {
        if (task == null) {
            throw new IllegalArgumentException("task can not be null");
        }
        AsyncTaskCompatHoneycomb.executeParallel(task, params);
        return task;
    }
}
