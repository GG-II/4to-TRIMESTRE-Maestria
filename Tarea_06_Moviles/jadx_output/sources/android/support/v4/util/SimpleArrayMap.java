package android.support.v4.util;

import java.util.Map;

/* loaded from: classes.dex */
public class SimpleArrayMap<K, V> {
    private static final int BASE_SIZE = 4;
    private static final int CACHE_SIZE = 10;
    private static final boolean DEBUG = false;
    private static final String TAG = "ArrayMap";
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;
    Object[] mArray;
    int[] mHashes;
    int mSize;

    int indexOf(Object key, int hash) {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = ContainerHelpers.binarySearch(this.mHashes, N, hash);
        if (index < 0) {
            return index;
        }
        if (key.equals(this.mArray[index << 1])) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == hash) {
            if (key.equals(this.mArray[end << 1])) {
                return end;
            }
            end++;
        }
        for (int i = index - 1; i >= 0 && this.mHashes[i] == hash; i--) {
            if (key.equals(this.mArray[i << 1])) {
                return i;
            }
        }
        int i2 = end ^ (-1);
        return i2;
    }

    int indexOfNull() {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = ContainerHelpers.binarySearch(this.mHashes, N, 0);
        if (index < 0) {
            return index;
        }
        if (this.mArray[index << 1] == null) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == 0) {
            if (this.mArray[end << 1] == null) {
                return end;
            }
            end++;
        }
        for (int i = index - 1; i >= 0 && this.mHashes[i] == 0; i--) {
            if (this.mArray[i << 1] == null) {
                return i;
            }
        }
        int i2 = end ^ (-1);
        return i2;
    }

    private void allocArrays(int size) {
        if (size == 8) {
            synchronized (ArrayMap.class) {
                Object[] array = mTwiceBaseCache;
                if (array != null) {
                    this.mArray = array;
                    mTwiceBaseCache = (Object[]) array[0];
                    this.mHashes = (int[]) array[1];
                    array[1] = null;
                    array[0] = null;
                    mTwiceBaseCacheSize--;
                    return;
                }
            }
        } else if (size == 4) {
            synchronized (ArrayMap.class) {
                Object[] array2 = mBaseCache;
                if (array2 != null) {
                    this.mArray = array2;
                    mBaseCache = (Object[]) array2[0];
                    this.mHashes = (int[]) array2[1];
                    array2[1] = null;
                    array2[0] = null;
                    mBaseCacheSize--;
                    return;
                }
            }
        }
        this.mHashes = new int[size];
        this.mArray = new Object[size << 1];
    }

    private static void freeArrays(int[] hashes, Object[] array, int size) {
        if (hashes.length == 8) {
            synchronized (ArrayMap.class) {
                if (mTwiceBaseCacheSize < 10) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (int i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                }
            }
            return;
        }
        if (hashes.length == 4) {
            synchronized (ArrayMap.class) {
                if (mBaseCacheSize < 10) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (int i2 = (size << 1) - 1; i2 >= 2; i2--) {
                        array[i2] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                }
            }
        }
    }

    public SimpleArrayMap() {
        this.mHashes = ContainerHelpers.EMPTY_INTS;
        this.mArray = ContainerHelpers.EMPTY_OBJECTS;
        this.mSize = 0;
    }

    public SimpleArrayMap(int capacity) {
        if (capacity == 0) {
            this.mHashes = ContainerHelpers.EMPTY_INTS;
            this.mArray = ContainerHelpers.EMPTY_OBJECTS;
        } else {
            allocArrays(capacity);
        }
        this.mSize = 0;
    }

    public SimpleArrayMap(SimpleArrayMap map) {
        this();
        if (map != null) {
            putAll(map);
        }
    }

    public void clear() {
        int i = this.mSize;
        if (i != 0) {
            freeArrays(this.mHashes, this.mArray, i);
            this.mHashes = ContainerHelpers.EMPTY_INTS;
            this.mArray = ContainerHelpers.EMPTY_OBJECTS;
            this.mSize = 0;
        }
    }

    public void ensureCapacity(int minimumCapacity) {
        if (this.mHashes.length < minimumCapacity) {
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(minimumCapacity);
            int i = this.mSize;
            if (i > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, i);
                System.arraycopy(oarray, 0, this.mArray, 0, this.mSize << 1);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
    }

    public boolean containsKey(Object key) {
        return indexOfKey(key) >= 0;
    }

    public int indexOfKey(Object key) {
        return key == null ? indexOfNull() : indexOf(key, key.hashCode());
    }

    int indexOfValue(Object value) {
        int N = this.mSize * 2;
        Object[] array = this.mArray;
        if (value == null) {
            for (int i = 1; i < N; i += 2) {
                if (array[i] == null) {
                    return i >> 1;
                }
            }
            return -1;
        }
        for (int i2 = 1; i2 < N; i2 += 2) {
            if (value.equals(array[i2])) {
                return i2 >> 1;
            }
        }
        return -1;
    }

    public boolean containsValue(Object value) {
        return indexOfValue(value) >= 0;
    }

    public V get(Object obj) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey >= 0) {
            return (V) this.mArray[(indexOfKey << 1) + 1];
        }
        return null;
    }

    public K keyAt(int i) {
        return (K) this.mArray[i << 1];
    }

    public V valueAt(int i) {
        return (V) this.mArray[(i << 1) + 1];
    }

    public V setValueAt(int i, V v) {
        int i2 = (i << 1) + 1;
        Object[] objArr = this.mArray;
        V v2 = (V) objArr[i2];
        objArr[i2] = v;
        return v2;
    }

    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    public V put(K k, V v) {
        int hashCode;
        int indexOf;
        if (k == null) {
            hashCode = 0;
            indexOf = indexOfNull();
        } else {
            hashCode = k.hashCode();
            indexOf = indexOf(k, hashCode);
        }
        if (indexOf >= 0) {
            int i = (indexOf << 1) + 1;
            Object[] objArr = this.mArray;
            V v2 = (V) objArr[i];
            objArr[i] = v;
            return v2;
        }
        int i2 = indexOf ^ (-1);
        int i3 = this.mSize;
        if (i3 >= this.mHashes.length) {
            int i4 = 8;
            if (i3 >= 8) {
                i4 = (i3 >> 1) + i3;
            } else if (i3 < 4) {
                i4 = 4;
            }
            int i5 = i4;
            int[] iArr = this.mHashes;
            Object[] objArr2 = this.mArray;
            allocArrays(i5);
            int[] iArr2 = this.mHashes;
            if (iArr2.length > 0) {
                System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                System.arraycopy(objArr2, 0, this.mArray, 0, objArr2.length);
            }
            freeArrays(iArr, objArr2, this.mSize);
        }
        int i6 = this.mSize;
        if (i2 < i6) {
            int[] iArr3 = this.mHashes;
            System.arraycopy(iArr3, i2, iArr3, i2 + 1, i6 - i2);
            Object[] objArr3 = this.mArray;
            System.arraycopy(objArr3, i2 << 1, objArr3, (i2 + 1) << 1, (this.mSize - i2) << 1);
        }
        this.mHashes[i2] = hashCode;
        Object[] objArr4 = this.mArray;
        objArr4[i2 << 1] = k;
        objArr4[(i2 << 1) + 1] = v;
        this.mSize++;
        return null;
    }

    public void putAll(SimpleArrayMap<? extends K, ? extends V> array) {
        int N = array.mSize;
        ensureCapacity(this.mSize + N);
        if (this.mSize == 0) {
            if (N > 0) {
                System.arraycopy(array.mHashes, 0, this.mHashes, 0, N);
                System.arraycopy(array.mArray, 0, this.mArray, 0, N << 1);
                this.mSize = N;
                return;
            }
            return;
        }
        for (int i = 0; i < N; i++) {
            put(array.keyAt(i), array.valueAt(i));
        }
    }

    public V remove(Object key) {
        int index = indexOfKey(key);
        if (index >= 0) {
            return removeAt(index);
        }
        return null;
    }

    public V removeAt(int i) {
        Object[] objArr = this.mArray;
        V v = (V) objArr[(i << 1) + 1];
        int i2 = this.mSize;
        if (i2 <= 1) {
            freeArrays(this.mHashes, objArr, i2);
            this.mHashes = ContainerHelpers.EMPTY_INTS;
            this.mArray = ContainerHelpers.EMPTY_OBJECTS;
            this.mSize = 0;
        } else {
            int[] iArr = this.mHashes;
            if (iArr.length > 8 && i2 < iArr.length / 3) {
                int i3 = i2 > 8 ? i2 + (i2 >> 1) : 8;
                int[] iArr2 = this.mHashes;
                Object[] objArr2 = this.mArray;
                allocArrays(i3);
                this.mSize--;
                if (i > 0) {
                    System.arraycopy(iArr2, 0, this.mHashes, 0, i);
                    System.arraycopy(objArr2, 0, this.mArray, 0, i << 1);
                }
                int i4 = this.mSize;
                if (i < i4) {
                    System.arraycopy(iArr2, i + 1, this.mHashes, i, i4 - i);
                    System.arraycopy(objArr2, (i + 1) << 1, this.mArray, i << 1, (this.mSize - i) << 1);
                }
            } else {
                int i5 = i2 - 1;
                this.mSize = i5;
                if (i < i5) {
                    System.arraycopy(iArr, i + 1, iArr, i, i5 - i);
                    Object[] objArr3 = this.mArray;
                    System.arraycopy(objArr3, (i + 1) << 1, objArr3, i << 1, (this.mSize - i) << 1);
                }
                Object[] objArr4 = this.mArray;
                int i6 = this.mSize;
                objArr4[i6 << 1] = null;
                objArr4[(i6 << 1) + 1] = null;
            }
        }
        return v;
    }

    public int size() {
        return this.mSize;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Map)) {
            return false;
        }
        Map<?, ?> map = (Map) object;
        if (size() != map.size()) {
            return false;
        }
        for (int i = 0; i < this.mSize; i++) {
            try {
                K key = keyAt(i);
                V mine = valueAt(i);
                Object theirs = map.get(key);
                if (mine == null) {
                    if (theirs != null || !map.containsKey(key)) {
                        return false;
                    }
                } else if (!mine.equals(theirs)) {
                    return false;
                }
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int[] hashes = this.mHashes;
        Object[] array = this.mArray;
        int result = 0;
        int i = 0;
        int v = 1;
        int s = this.mSize;
        while (i < s) {
            Object value = array[v];
            result += hashes[i] ^ (value == null ? 0 : value.hashCode());
            i++;
            v += 2;
        }
        return result;
    }

    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.mSize * 28);
        buffer.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            Object key = keyAt(i);
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)");
            }
            buffer.append('=');
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }
}
