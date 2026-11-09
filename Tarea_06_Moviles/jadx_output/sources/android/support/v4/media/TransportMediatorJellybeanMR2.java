package android.support.v4.media;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;

/* loaded from: classes.dex */
class TransportMediatorJellybeanMR2 {
    AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    boolean mAudioFocused;
    final AudioManager mAudioManager;
    final Context mContext;
    boolean mFocused;
    final RemoteControlClient.OnGetPlaybackPositionListener mGetPlaybackPositionListener;
    final Intent mIntent;
    final BroadcastReceiver mMediaButtonReceiver;
    PendingIntent mPendingIntent;
    int mPlayState;
    final RemoteControlClient.OnPlaybackPositionUpdateListener mPlaybackPositionUpdateListener;
    final String mReceiverAction;
    final IntentFilter mReceiverFilter;
    RemoteControlClient mRemoteControl;
    final View mTargetView;
    final TransportMediatorCallback mTransportCallback;
    final ViewTreeObserver.OnWindowAttachListener mWindowAttachListener;
    final ViewTreeObserver.OnWindowFocusChangeListener mWindowFocusListener;

    public TransportMediatorJellybeanMR2(Context context, AudioManager audioManager, View view, TransportMediatorCallback transportCallback) {
        ViewTreeObserver.OnWindowAttachListener onWindowAttachListener = new ViewTreeObserver.OnWindowAttachListener() { // from class: android.support.v4.media.TransportMediatorJellybeanMR2.1
            @Override // android.view.ViewTreeObserver.OnWindowAttachListener
            public void onWindowAttached() {
                TransportMediatorJellybeanMR2.this.windowAttached();
            }

            @Override // android.view.ViewTreeObserver.OnWindowAttachListener
            public void onWindowDetached() {
                TransportMediatorJellybeanMR2.this.windowDetached();
            }
        };
        this.mWindowAttachListener = onWindowAttachListener;
        ViewTreeObserver.OnWindowFocusChangeListener onWindowFocusChangeListener = new ViewTreeObserver.OnWindowFocusChangeListener() { // from class: android.support.v4.media.TransportMediatorJellybeanMR2.2
            @Override // android.view.ViewTreeObserver.OnWindowFocusChangeListener
            public void onWindowFocusChanged(boolean hasFocus) {
                if (!hasFocus) {
                    TransportMediatorJellybeanMR2.this.loseFocus();
                } else {
                    TransportMediatorJellybeanMR2.this.gainFocus();
                }
            }
        };
        this.mWindowFocusListener = onWindowFocusChangeListener;
        this.mMediaButtonReceiver = new BroadcastReceiver() { // from class: android.support.v4.media.TransportMediatorJellybeanMR2.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                try {
                    KeyEvent event = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
                    TransportMediatorJellybeanMR2.this.mTransportCallback.handleKey(event);
                } catch (ClassCastException e) {
                    Log.w("TransportController", e);
                }
            }
        };
        this.mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() { // from class: android.support.v4.media.TransportMediatorJellybeanMR2.4
            @Override // android.media.AudioManager.OnAudioFocusChangeListener
            public void onAudioFocusChange(int focusChange) {
                TransportMediatorJellybeanMR2.this.mTransportCallback.handleAudioFocusChange(focusChange);
            }
        };
        this.mGetPlaybackPositionListener = new RemoteControlClient.OnGetPlaybackPositionListener() { // from class: android.support.v4.media.TransportMediatorJellybeanMR2.5
            @Override // android.media.RemoteControlClient.OnGetPlaybackPositionListener
            public long onGetPlaybackPosition() {
                return TransportMediatorJellybeanMR2.this.mTransportCallback.getPlaybackPosition();
            }
        };
        this.mPlaybackPositionUpdateListener = new RemoteControlClient.OnPlaybackPositionUpdateListener() { // from class: android.support.v4.media.TransportMediatorJellybeanMR2.6
            @Override // android.media.RemoteControlClient.OnPlaybackPositionUpdateListener
            public void onPlaybackPositionUpdate(long newPositionMs) {
                TransportMediatorJellybeanMR2.this.mTransportCallback.playbackPositionUpdate(newPositionMs);
            }
        };
        this.mPlayState = 0;
        this.mContext = context;
        this.mAudioManager = audioManager;
        this.mTargetView = view;
        this.mTransportCallback = transportCallback;
        String str = context.getPackageName() + ":transport:" + System.identityHashCode(this);
        this.mReceiverAction = str;
        Intent intent = new Intent(str);
        this.mIntent = intent;
        intent.setPackage(context.getPackageName());
        IntentFilter intentFilter = new IntentFilter();
        this.mReceiverFilter = intentFilter;
        intentFilter.addAction(str);
        view.getViewTreeObserver().addOnWindowAttachListener(onWindowAttachListener);
        view.getViewTreeObserver().addOnWindowFocusChangeListener(onWindowFocusChangeListener);
    }

    public Object getRemoteControlClient() {
        return this.mRemoteControl;
    }

    public void destroy() {
        windowDetached();
        this.mTargetView.getViewTreeObserver().removeOnWindowAttachListener(this.mWindowAttachListener);
        this.mTargetView.getViewTreeObserver().removeOnWindowFocusChangeListener(this.mWindowFocusListener);
    }

    void windowAttached() {
        this.mContext.registerReceiver(this.mMediaButtonReceiver, this.mReceiverFilter);
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, this.mIntent, 268435456);
        RemoteControlClient remoteControlClient = new RemoteControlClient(this.mPendingIntent);
        this.mRemoteControl = remoteControlClient;
        remoteControlClient.setOnGetPlaybackPositionListener(this.mGetPlaybackPositionListener);
        this.mRemoteControl.setPlaybackPositionUpdateListener(this.mPlaybackPositionUpdateListener);
    }

    void gainFocus() {
        if (!this.mFocused) {
            this.mFocused = true;
            this.mAudioManager.registerMediaButtonEventReceiver(this.mPendingIntent);
            this.mAudioManager.registerRemoteControlClient(this.mRemoteControl);
            if (this.mPlayState == 3) {
                takeAudioFocus();
            }
        }
    }

    void takeAudioFocus() {
        if (!this.mAudioFocused) {
            this.mAudioFocused = true;
            this.mAudioManager.requestAudioFocus(this.mAudioFocusChangeListener, 3, 1);
        }
    }

    public void startPlaying() {
        if (this.mPlayState != 3) {
            this.mPlayState = 3;
            this.mRemoteControl.setPlaybackState(3);
        }
        if (this.mFocused) {
            takeAudioFocus();
        }
    }

    public void refreshState(boolean playing, long position, int transportControls) {
        RemoteControlClient remoteControlClient = this.mRemoteControl;
        if (remoteControlClient != null) {
            remoteControlClient.setPlaybackState(playing ? 3 : 1, position, playing ? 1.0f : 0.0f);
            this.mRemoteControl.setTransportControlFlags(transportControls);
        }
    }

    public void pausePlaying() {
        if (this.mPlayState == 3) {
            this.mPlayState = 2;
            this.mRemoteControl.setPlaybackState(2);
        }
        dropAudioFocus();
    }

    public void stopPlaying() {
        if (this.mPlayState != 1) {
            this.mPlayState = 1;
            this.mRemoteControl.setPlaybackState(1);
        }
        dropAudioFocus();
    }

    void dropAudioFocus() {
        if (this.mAudioFocused) {
            this.mAudioFocused = false;
            this.mAudioManager.abandonAudioFocus(this.mAudioFocusChangeListener);
        }
    }

    void loseFocus() {
        dropAudioFocus();
        if (this.mFocused) {
            this.mFocused = false;
            this.mAudioManager.unregisterRemoteControlClient(this.mRemoteControl);
            this.mAudioManager.unregisterMediaButtonEventReceiver(this.mPendingIntent);
        }
    }

    void windowDetached() {
        loseFocus();
        if (this.mPendingIntent != null) {
            this.mContext.unregisterReceiver(this.mMediaButtonReceiver);
            this.mPendingIntent.cancel();
            this.mPendingIntent = null;
            this.mRemoteControl = null;
        }
    }
}
