package android.support.v4.content;

import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class ParallelExecutorCompat {
    public static Executor getParallelExecutor() {
        return ExecutorCompatHoneycomb.getParallelExecutor();
    }
}
