package android.support.v4.speech.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;

/* loaded from: classes.dex */
class TextToSpeechICS {
    private static final String TAG = "android.support.v4.speech.tts";

    TextToSpeechICS() {
    }

    static TextToSpeech construct(Context context, TextToSpeech.OnInitListener onInitListener, String engineName) {
        return new TextToSpeech(context, onInitListener, engineName);
    }
}
