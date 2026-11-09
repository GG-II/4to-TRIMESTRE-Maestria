package android.support.v7.widget;

import java.util.ArrayList;

/* loaded from: classes.dex */
class PositionMap<E> implements Cloneable {
    private static final Object DELETED = new Object();
    private boolean mGarbage;
    private int[] mKeys;
    private int mSize;
    private Object[] mValues;

    public PositionMap() {
        this(10);
    }

    public PositionMap(int initialCapacity) {
        this.mGarbage = false;
        if (initialCapacity == 0) {
            this.mKeys = ContainerHelpers.EMPTY_INTS;
            this.mValues = ContainerHelpers.EMPTY_OBJECTS;
        } else {
            int initialCapacity2 = idealIntArraySize(initialCapacity);
            this.mKeys = new int[initialCapacity2];
            this.mValues = new Object[initialCapacity2];
        }
        this.mSize = 0;
    }

    /* renamed from: clone, reason: merged with bridge method [inline-methods] */
    public PositionMap<E> m4clone() {
        PositionMap<E> clone = null;
        try {
            clone = (PositionMap) super.clone();
            clone.mKeys = (int[]) this.mKeys.clone();
            clone.mValues = (Object[]) this.mValues.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return clone;
        }
    }

    public E get(int key) {
        return get(key, null);
    }

    public E get(int i, E e) {
        E e2;
        int binarySearch = ContainerHelpers.binarySearch(this.mKeys, this.mSize, i);
        if (binarySearch < 0 || (e2 = (E) this.mValues[binarySearch]) == DELETED) {
            return e;
        }
        return e2;
    }

    public void delete(int key) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i >= 0) {
            Object[] objArr = this.mValues;
            Object obj = objArr[i];
            Object obj2 = DELETED;
            if (obj != obj2) {
                objArr[i] = obj2;
                this.mGarbage = true;
            }
        }
    }

    public void remove(int key) {
        delete(key);
    }

    public void removeAt(int index) {
        Object[] objArr = this.mValues;
        Object obj = objArr[index];
        Object obj2 = DELETED;
        if (obj != obj2) {
            objArr[index] = obj2;
            this.mGarbage = true;
        }
    }

    public void removeAtRange(int index, int size) {
        int end = Math.min(this.mSize, index + size);
        for (int i = index; i < end; i++) {
            removeAt(i);
        }
    }

    public void insertKeyRange(int keyStart, int count) {
    }

    public void removeKeyRange(ArrayList<E> removedItems, int keyStart, int count) {
    }

    private void gc() {
        int n = this.mSize;
        int o = 0;
        int[] keys = this.mKeys;
        Object[] values = this.mValues;
        for (int i = 0; i < n; i++) {
            Object val = values[i];
            if (val != DELETED) {
                if (i != o) {
                    keys[o] = keys[i];
                    values[o] = val;
                    values[i] = null;
                }
                o++;
            }
        }
        this.mGarbage = false;
        this.mSize = o;
    }

    public void put(int key, E value) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i >= 0) {
            this.mValues[i] = value;
            return;
        }
        int i2 = i ^ (-1);
        int i3 = this.mSize;
        if (i2 < i3) {
            Object[] objArr = this.mValues;
            if (objArr[i2] == DELETED) {
                this.mKeys[i2] = key;
                objArr[i2] = value;
                return;
            }
        }
        if (this.mGarbage && i3 >= this.mKeys.length) {
            gc();
            i2 = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key) ^ (-1);
        }
        int i4 = this.mSize;
        if (i4 >= this.mKeys.length) {
            int n = idealIntArraySize(i4 + 1);
            int[] nkeys = new int[n];
            Object[] nvalues = new Object[n];
            int[] iArr = this.mKeys;
            System.arraycopy(iArr, 0, nkeys, 0, iArr.length);
            Object[] objArr2 = this.mValues;
            System.arraycopy(objArr2, 0, nvalues, 0, objArr2.length);
            this.mKeys = nkeys;
            this.mValues = nvalues;
        }
        int n2 = this.mSize;
        if (n2 - i2 != 0) {
            int[] iArr2 = this.mKeys;
            System.arraycopy(iArr2, i2, iArr2, i2 + 1, n2 - i2);
            Object[] objArr3 = this.mValues;
            System.arraycopy(objArr3, i2, objArr3, i2 + 1, this.mSize - i2);
        }
        this.mKeys[i2] = key;
        this.mValues[i2] = value;
        this.mSize++;
    }

    public int size() {
        if (this.mGarbage) {
            gc();
        }
        return this.mSize;
    }

    public int keyAt(int index) {
        if (this.mGarbage) {
            gc();
        }
        return this.mKeys[index];
    }

    public E valueAt(int i) {
        if (this.mGarbage) {
            gc();
        }
        return (E) this.mValues[i];
    }

    public void setValueAt(int index, E value) {
        if (this.mGarbage) {
            gc();
        }
        this.mValues[index] = value;
    }

    public int indexOfKey(int key) {
        if (this.mGarbage) {
            gc();
        }
        return ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
    }

    public int indexOfValue(E value) {
        if (this.mGarbage) {
            gc();
        }
        for (int i = 0; i < this.mSize; i++) {
            if (this.mValues[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public void clear() {
        int n = this.mSize;
        Object[] values = this.mValues;
        for (int i = 0; i < n; i++) {
            values[i] = null;
        }
        this.mSize = 0;
        this.mGarbage = false;
    }

    public void append(int key, E value) {
        int i = this.mSize;
        if (i != 0 && key <= this.mKeys[i - 1]) {
            put(key, value);
            return;
        }
        if (this.mGarbage && i >= this.mKeys.length) {
            gc();
        }
        int pos = this.mSize;
        if (pos >= this.mKeys.length) {
            int n = idealIntArraySize(pos + 1);
            int[] nkeys = new int[n];
            Object[] nvalues = new Object[n];
            int[] iArr = this.mKeys;
            System.arraycopy(iArr, 0, nkeys, 0, iArr.length);
            Object[] objArr = this.mValues;
            System.arraycopy(objArr, 0, nvalues, 0, objArr.length);
            this.mKeys = nkeys;
            this.mValues = nvalues;
        }
        this.mKeys[pos] = key;
        this.mValues[pos] = value;
        this.mSize = pos + 1;
    }

    public String toString() {
        if (size() <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.mSize * 28);
        buffer.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = keyAt(i);
            buffer.append(key);
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

    static int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }

    static int idealBooleanArraySize(int need) {
        return idealByteArraySize(need);
    }

    static int idealShortArraySize(int need) {
        return idealByteArraySize(need * 2) / 2;
    }

    static int idealCharArraySize(int need) {
        return idealByteArraySize(need * 2) / 2;
    }

    static int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    static int idealFloatArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    static int idealObjectArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    static int idealLongArraySize(int need) {
        return idealByteArraySize(need * 8) / 8;
    }

    static class ContainerHelpers {
        static final boolean[] EMPTY_BOOLEANS = new boolean[0];
        static final int[] EMPTY_INTS = new int[0];
        static final long[] EMPTY_LONGS = new long[0];
        static final Object[] EMPTY_OBJECTS = new Object[0];

        ContainerHelpers() {
        }

        static int binarySearch(int[] array, int size, int value) {
            int lo = 0;
            int hi = size - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                int midVal = array[mid];
                if (midVal < value) {
                    lo = mid + 1;
                } else if (midVal > value) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }
            return lo ^ (-1);
        }
    }
}
