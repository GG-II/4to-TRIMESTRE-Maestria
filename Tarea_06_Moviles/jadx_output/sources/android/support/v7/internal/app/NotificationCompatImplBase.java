package android.support.v7.internal.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.support.v4.app.NotificationCompatBase;
import android.support.v7.appcompat.R;
import android.widget.RemoteViews;
import java.util.List;

/* loaded from: classes.dex */
public class NotificationCompatImplBase {
    static final int MAX_MEDIA_BUTTONS = 5;
    static final int MAX_MEDIA_BUTTONS_IN_COMPACT = 3;

    public static <T extends NotificationCompatBase.Action> void overrideContentView(NotificationBuilderWithBuilderAccessor builder, Context context, CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo, int number, Bitmap largeIcon, CharSequence subText, boolean useChronometer, long when, List<T> actions, int[] actionsToShowInCompact, boolean showCancelButton, PendingIntent cancelButtonIntent) {
        RemoteViews views = generateContentView(context, contentTitle, contentText, contentInfo, number, largeIcon, subText, useChronometer, when, actions, actionsToShowInCompact, showCancelButton, cancelButtonIntent);
        builder.getBuilder().setContent(views);
        if (showCancelButton) {
            builder.getBuilder().setOngoing(true);
        }
    }

    private static <T extends NotificationCompatBase.Action> RemoteViews generateContentView(Context context, CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo, int number, Bitmap largeIcon, CharSequence subText, boolean useChronometer, long when, List<T> actions, int[] actionsToShowInCompact, boolean showCancelButton, PendingIntent cancelButtonIntent) {
        RemoteViews view = applyStandardTemplate(context, contentTitle, contentText, contentInfo, number, largeIcon, subText, useChronometer, when, R.layout.notification_template_media, true);
        int numActions = actions.size();
        int N = actionsToShowInCompact == null ? 0 : Math.min(actionsToShowInCompact.length, 3);
        view.removeAllViews(R.id.media_actions);
        if (N > 0) {
            for (int i = 0; i < N; i++) {
                if (i >= numActions) {
                    throw new IllegalArgumentException(String.format("setShowActionsInCompactView: action %d out of bounds (max %d)", Integer.valueOf(i), Integer.valueOf(numActions - 1)));
                }
                NotificationCompatBase.Action action = actions.get(actionsToShowInCompact[i]);
                RemoteViews button = generateMediaActionButton(context, action);
                view.addView(R.id.media_actions, button);
            }
        }
        if (!showCancelButton) {
            view.setViewVisibility(R.id.end_padder, 0);
            view.setViewVisibility(R.id.cancel_action, 8);
        } else {
            view.setViewVisibility(R.id.end_padder, 8);
            view.setViewVisibility(R.id.cancel_action, 0);
            view.setOnClickPendingIntent(R.id.cancel_action, cancelButtonIntent);
            view.setInt(R.id.cancel_action, "setAlpha", context.getResources().getInteger(R.integer.cancel_button_image_alpha));
        }
        return view;
    }

    public static <T extends NotificationCompatBase.Action> void overrideBigContentView(Notification n, Context context, CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo, int number, Bitmap largeIcon, CharSequence subText, boolean useChronometer, long when, List<T> actions, boolean showCancelButton, PendingIntent cancelButtonIntent) {
        n.bigContentView = generateBigContentView(context, contentTitle, contentText, contentInfo, number, largeIcon, subText, useChronometer, when, actions, showCancelButton, cancelButtonIntent);
        if (showCancelButton) {
            n.flags |= 2;
        }
    }

    private static <T extends NotificationCompatBase.Action> RemoteViews generateBigContentView(Context context, CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo, int number, Bitmap largeIcon, CharSequence subText, boolean useChronometer, long when, List<T> actions, boolean showCancelButton, PendingIntent cancelButtonIntent) {
        int actionCount = Math.min(actions.size(), 5);
        RemoteViews big = applyStandardTemplate(context, contentTitle, contentText, contentInfo, number, largeIcon, subText, useChronometer, when, getBigLayoutResource(actionCount), false);
        big.removeAllViews(R.id.media_actions);
        if (actionCount > 0) {
            for (int i = 0; i < actionCount; i++) {
                RemoteViews button = generateMediaActionButton(context, actions.get(i));
                big.addView(R.id.media_actions, button);
            }
        }
        if (showCancelButton) {
            big.setViewVisibility(R.id.cancel_action, 0);
            big.setInt(R.id.cancel_action, "setAlpha", context.getResources().getInteger(R.integer.cancel_button_image_alpha));
            big.setOnClickPendingIntent(R.id.cancel_action, cancelButtonIntent);
        } else {
            big.setViewVisibility(R.id.cancel_action, 8);
        }
        return big;
    }

    private static RemoteViews generateMediaActionButton(Context context, NotificationCompatBase.Action action) {
        boolean tombstone = action.getActionIntent() == null;
        RemoteViews button = new RemoteViews(context.getPackageName(), R.layout.notification_media_action);
        button.setImageViewResource(R.id.action0, action.getIcon());
        if (!tombstone) {
            button.setOnClickPendingIntent(R.id.action0, action.getActionIntent());
        }
        button.setContentDescription(R.id.action0, action.getTitle());
        return button;
    }

    private static int getBigLayoutResource(int actionCount) {
        if (actionCount <= 3) {
            return R.layout.notification_template_big_media_narrow;
        }
        return R.layout.notification_template_big_media;
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x00f7  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x012c  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x012e  */
    /* JADX WARN: Removed duplicated region for block: B:36:0x0127  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static android.widget.RemoteViews applyStandardTemplate(android.content.Context r20, java.lang.CharSequence r21, java.lang.CharSequence r22, java.lang.CharSequence r23, int r24, android.graphics.Bitmap r25, java.lang.CharSequence r26, boolean r27, long r28, int r30, boolean r31) {
        /*
            Method dump skipped, instructions count: 308
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.internal.app.NotificationCompatImplBase.applyStandardTemplate(android.content.Context, java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence, int, android.graphics.Bitmap, java.lang.CharSequence, boolean, long, int, boolean):android.widget.RemoteViews");
    }
}
