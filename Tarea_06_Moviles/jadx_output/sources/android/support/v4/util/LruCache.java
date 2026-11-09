package android.support.v4.util;

import java.util.LinkedHashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class LruCache<K, V> {
    private int createCount;
    private int evictionCount;
    private int hitCount;
    private final LinkedHashMap<K, V> map;
    private int maxSize;
    private int missCount;
    private int putCount;
    private int size;

    public LruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<>(0, 0.75f, true);
    }

    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        synchronized (this) {
            this.maxSize = maxSize;
        }
        trimToSize(maxSize);
    }

    public final V get(K k) {
        V v;
        if (k == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            try {
                try {
                    V v2 = this.map.get(k);
                    if (v2 != null) {
                        this.hitCount++;
                        return v2;
                    }
                    this.missCount++;
                    V create = create(k);
                    if (create == null) {
                        return null;
                    }
                    synchronized (this) {
                        this.createCount++;
                        v = (V) this.map.put(k, create);
                        if (v != null) {
                            this.map.put(k, v);
                        } else {
                            this.size += safeSizeOf(k, create);
                        }
                    }
                    if (v != null) {
                        entryRemoved(false, k, create, v);
                        return v;
                    }
                    trimToSize(this.maxSize);
                    return create;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        synchronized (this) {
            try {
                try {
                    this.putCount++;
                    this.size += safeSizeOf(key, value);
                    V previous = this.map.put(key, value);
                    if (previous != null) {
                        this.size -= safeSizeOf(key, previous);
                    }
                    if (previous != null) {
                        entryRemoved(false, key, previous, value);
                    }
                    trimToSize(this.maxSize);
                    return previous;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Can't find top splitter block for handler:B:30:0x007a
        	at jadx.core.utils.BlockUtils.getTopSplitterForHandler(BlockUtils.java:1179)
        	at jadx.core.dex.visitors.regions.maker.ExcHandlersRegionMaker.collectHandlerRegions(ExcHandlersRegionMaker.java:53)
        	at jadx.core.dex.visitors.regions.maker.ExcHandlersRegionMaker.process(ExcHandlersRegionMaker.java:38)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:27)
        */
    public void trimToSize(int r6) {
        /*
            r5 = this;
            r0 = 0
            r1 = r0
        L2:
            monitor-enter(r5)
            int r2 = r5.size     // Catch: java.lang.Throwable -> L77
            if (r2 < 0) goto L56
            java.util.LinkedHashMap<K, V> r2 = r5.map     // Catch: java.lang.Throwable -> L77
            boolean r2 = r2.isEmpty()     // Catch: java.lang.Throwable -> L77
            if (r2 == 0) goto L13
            int r2 = r5.size     // Catch: java.lang.Throwable -> L77
            if (r2 != 0) goto L56
        L13:
            int r2 = r5.size     // Catch: java.lang.Throwable -> L77
            if (r2 <= r6) goto L54
            java.util.LinkedHashMap<K, V> r2 = r5.map     // Catch: java.lang.Throwable -> L77
            boolean r2 = r2.isEmpty()     // Catch: java.lang.Throwable -> L77
            if (r2 == 0) goto L20
            goto L54
        L20:
            java.util.LinkedHashMap<K, V> r2 = r5.map     // Catch: java.lang.Throwable -> L77
            java.util.Set r2 = r2.entrySet()     // Catch: java.lang.Throwable -> L77
            java.util.Iterator r2 = r2.iterator()     // Catch: java.lang.Throwable -> L77
            java.lang.Object r2 = r2.next()     // Catch: java.lang.Throwable -> L77
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2     // Catch: java.lang.Throwable -> L77
            java.lang.Object r0 = r2.getKey()     // Catch: java.lang.Throwable -> L77
            java.lang.Object r1 = r2.getValue()     // Catch: java.lang.Throwable -> L52
            java.util.LinkedHashMap<K, V> r3 = r5.map     // Catch: java.lang.Throwable -> L7a
            r3.remove(r0)     // Catch: java.lang.Throwable -> L7a
            int r3 = r5.size     // Catch: java.lang.Throwable -> L7a
            int r4 = r5.safeSizeOf(r0, r1)     // Catch: java.lang.Throwable -> L7a
            int r3 = r3 - r4
            r5.size = r3     // Catch: java.lang.Throwable -> L7a
            int r3 = r5.evictionCount     // Catch: java.lang.Throwable -> L7a
            r4 = 1
            int r3 = r3 + r4
            r5.evictionCount = r3     // Catch: java.lang.Throwable -> L7a
            monitor-exit(r5)     // Catch: java.lang.Throwable -> L7a
            r2 = 0
            r5.entryRemoved(r4, r0, r1, r2)
            goto L2
        L52:
            r2 = move-exception
            goto L78
        L54:
            monitor-exit(r5)     // Catch: java.lang.Throwable -> L77
            return
        L56:
            java.lang.IllegalStateException r2 = new java.lang.IllegalStateException     // Catch: java.lang.Throwable -> L77
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L77
            r3.<init>()     // Catch: java.lang.Throwable -> L77
            java.lang.Class r4 = r5.getClass()     // Catch: java.lang.Throwable -> L77
            java.lang.String r4 = r4.getName()     // Catch: java.lang.Throwable -> L77
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Throwable -> L77
            java.lang.String r4 = ".sizeOf() is reporting inconsistent results!"
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Throwable -> L77
            java.lang.String r3 = r3.toString()     // Catch: java.lang.Throwable -> L77
            r2.<init>(r3)     // Catch: java.lang.Throwable -> L77
            throw r2     // Catch: java.lang.Throwable -> L77
        L77:
            r2 = move-exception
        L78:
            monitor-exit(r5)     // Catch: java.lang.Throwable -> L7a
            throw r2
        L7a:
            r2 = move-exception
            goto L78
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.trimToSize(int):void");
    }

    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            try {
                try {
                    V previous = this.map.remove(key);
                    if (previous != null) {
                        this.size -= safeSizeOf(key, previous);
                    }
                    if (previous != null) {
                        entryRemoved(false, key, previous, null);
                    }
                    return previous;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }

    protected V create(K key) {
        return null;
    }

    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }

    protected int sizeOf(K key, V value) {
        return 1;
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    public final synchronized int size() {
        return this.size;
    }

    public final synchronized int maxSize() {
        return this.maxSize;
    }

    public final synchronized int hitCount() {
        return this.hitCount;
    }

    public final synchronized int missCount() {
        return this.missCount;
    }

    public final synchronized int createCount() {
        return this.createCount;
    }

    public final synchronized int putCount() {
        return this.putCount;
    }

    public final synchronized int evictionCount() {
        return this.evictionCount;
    }

    public final synchronized Map<K, V> snapshot() {
        return new LinkedHashMap(this.map);
    }

    public final synchronized String toString() {
        int hitPercent;
        int i = this.hitCount;
        int accesses = this.missCount + i;
        hitPercent = accesses != 0 ? (i * 100) / accesses : 0;
        return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", Integer.valueOf(this.maxSize), Integer.valueOf(this.hitCount), Integer.valueOf(this.missCount), Integer.valueOf(hitPercent));
    }
}
