package android.support.v4.content;

import android.content.SharedPreferences;

/* loaded from: classes.dex */
public class SharedPreferencesCompat {

    public static class EditorCompat {
        private static EditorCompat sInstance;
        private final Helper mHelper = new EditorHelperApi9Impl();

        private interface Helper {
            void apply(SharedPreferences.Editor editor);
        }

        private static class EditorHelperBaseImpl implements Helper {
            private EditorHelperBaseImpl() {
            }

            @Override // android.support.v4.content.SharedPreferencesCompat.EditorCompat.Helper
            public void apply(SharedPreferences.Editor editor) {
                editor.commit();
            }
        }

        private static class EditorHelperApi9Impl implements Helper {
            private EditorHelperApi9Impl() {
            }

            @Override // android.support.v4.content.SharedPreferencesCompat.EditorCompat.Helper
            public void apply(SharedPreferences.Editor editor) {
                EditorCompatGingerbread.apply(editor);
            }
        }

        private EditorCompat() {
        }

        public static EditorCompat getInstance() {
            if (sInstance == null) {
                sInstance = new EditorCompat();
            }
            return sInstance;
        }

        public void apply(SharedPreferences.Editor editor) {
            this.mHelper.apply(editor);
        }
    }
}
