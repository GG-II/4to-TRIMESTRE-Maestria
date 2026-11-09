package android.support.v4.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.IntentCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ShareCompat {
    public static final String EXTRA_CALLING_ACTIVITY = "android.support.v4.app.EXTRA_CALLING_ACTIVITY";
    public static final String EXTRA_CALLING_PACKAGE = "android.support.v4.app.EXTRA_CALLING_PACKAGE";
    private static ShareCompatImpl IMPL;

    interface ShareCompatImpl {
        void configureMenuItem(MenuItem menuItem, IntentBuilder intentBuilder);

        String escapeHtml(CharSequence charSequence);
    }

    static class ShareCompatImplBase implements ShareCompatImpl {
        ShareCompatImplBase() {
        }

        @Override // android.support.v4.app.ShareCompat.ShareCompatImpl
        public void configureMenuItem(MenuItem item, IntentBuilder shareIntent) {
            item.setIntent(shareIntent.createChooserIntent());
        }

        @Override // android.support.v4.app.ShareCompat.ShareCompatImpl
        public String escapeHtml(CharSequence text) {
            StringBuilder out = new StringBuilder();
            withinStyle(out, text, 0, text.length());
            return out.toString();
        }

        private static void withinStyle(StringBuilder out, CharSequence text, int start, int end) {
            int i = start;
            while (i < end) {
                char c = text.charAt(i);
                if (c == '<') {
                    out.append("&lt;");
                } else if (c == '>') {
                    out.append("&gt;");
                } else if (c == '&') {
                    out.append("&amp;");
                } else if (c > '~' || c < ' ') {
                    out.append("&#" + ((int) c) + ";");
                } else if (c == ' ') {
                    while (i + 1 < end && text.charAt(i + 1) == ' ') {
                        out.append("&nbsp;");
                        i++;
                    }
                    out.append(' ');
                } else {
                    out.append(c);
                }
                i++;
            }
        }
    }

    static class ShareCompatImplICS extends ShareCompatImplBase {
        ShareCompatImplICS() {
        }

        @Override // android.support.v4.app.ShareCompat.ShareCompatImplBase, android.support.v4.app.ShareCompat.ShareCompatImpl
        public void configureMenuItem(MenuItem item, IntentBuilder shareIntent) {
            ShareCompatICS.configureMenuItem(item, shareIntent.getActivity(), shareIntent.getIntent());
            if (shouldAddChooserIntent(item)) {
                item.setIntent(shareIntent.createChooserIntent());
            }
        }

        boolean shouldAddChooserIntent(MenuItem item) {
            return !item.hasSubMenu();
        }
    }

    static class ShareCompatImplJB extends ShareCompatImplICS {
        ShareCompatImplJB() {
        }

        @Override // android.support.v4.app.ShareCompat.ShareCompatImplBase, android.support.v4.app.ShareCompat.ShareCompatImpl
        public String escapeHtml(CharSequence html) {
            return ShareCompatJB.escapeHtml(html);
        }

        @Override // android.support.v4.app.ShareCompat.ShareCompatImplICS
        boolean shouldAddChooserIntent(MenuItem item) {
            return false;
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 16) {
            IMPL = new ShareCompatImplJB();
        } else {
            IMPL = new ShareCompatImplICS();
        }
    }

    public static String getCallingPackage(Activity calledActivity) {
        String result = calledActivity.getCallingPackage();
        if (result == null) {
            return calledActivity.getIntent().getStringExtra(EXTRA_CALLING_PACKAGE);
        }
        return result;
    }

    public static ComponentName getCallingActivity(Activity calledActivity) {
        ComponentName result = calledActivity.getCallingActivity();
        if (result == null) {
            return (ComponentName) calledActivity.getIntent().getParcelableExtra(EXTRA_CALLING_ACTIVITY);
        }
        return result;
    }

    public static void configureMenuItem(MenuItem item, IntentBuilder shareIntent) {
        IMPL.configureMenuItem(item, shareIntent);
    }

    public static void configureMenuItem(Menu menu, int menuItemId, IntentBuilder shareIntent) {
        MenuItem item = menu.findItem(menuItemId);
        if (item == null) {
            throw new IllegalArgumentException("Could not find menu item with id " + menuItemId + " in the supplied menu");
        }
        configureMenuItem(item, shareIntent);
    }

    public static class IntentBuilder {
        private Activity mActivity;
        private ArrayList<String> mBccAddresses;
        private ArrayList<String> mCcAddresses;
        private CharSequence mChooserTitle;
        private Intent mIntent;
        private ArrayList<Uri> mStreams;
        private ArrayList<String> mToAddresses;

        public static IntentBuilder from(Activity launchingActivity) {
            return new IntentBuilder(launchingActivity);
        }

        private IntentBuilder(Activity launchingActivity) {
            this.mActivity = launchingActivity;
            Intent action = new Intent().setAction("android.intent.action.SEND");
            this.mIntent = action;
            action.putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, launchingActivity.getPackageName());
            this.mIntent.putExtra(ShareCompat.EXTRA_CALLING_ACTIVITY, launchingActivity.getComponentName());
            this.mIntent.addFlags(524288);
        }

        /* JADX WARN: Code restructure failed: missing block: B:13:0x002c, code lost:
        
            if (r0.size() > 1) goto L17;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public android.content.Intent getIntent() {
            /*
                r8 = this;
                java.util.ArrayList<java.lang.String> r0 = r8.mToAddresses
                r1 = 0
                if (r0 == 0) goto Lc
                java.lang.String r2 = "android.intent.extra.EMAIL"
                r8.combineArrayExtra(r2, r0)
                r8.mToAddresses = r1
            Lc:
                java.util.ArrayList<java.lang.String> r0 = r8.mCcAddresses
                if (r0 == 0) goto L17
                java.lang.String r2 = "android.intent.extra.CC"
                r8.combineArrayExtra(r2, r0)
                r8.mCcAddresses = r1
            L17:
                java.util.ArrayList<java.lang.String> r0 = r8.mBccAddresses
                if (r0 == 0) goto L22
                java.lang.String r2 = "android.intent.extra.BCC"
                r8.combineArrayExtra(r2, r0)
                r8.mBccAddresses = r1
            L22:
                java.util.ArrayList<android.net.Uri> r0 = r8.mStreams
                r2 = 0
                if (r0 == 0) goto L2f
                int r0 = r0.size()
                r3 = 1
                if (r0 <= r3) goto L2f
                goto L30
            L2f:
                r3 = 0
            L30:
                r0 = r3
                android.content.Intent r3 = r8.mIntent
                java.lang.String r3 = r3.getAction()
                java.lang.String r4 = "android.intent.action.SEND_MULTIPLE"
                boolean r3 = r3.equals(r4)
                java.lang.String r5 = "android.intent.extra.STREAM"
                if (r0 != 0) goto L69
                if (r3 == 0) goto L69
                android.content.Intent r6 = r8.mIntent
                java.lang.String r7 = "android.intent.action.SEND"
                r6.setAction(r7)
                java.util.ArrayList<android.net.Uri> r6 = r8.mStreams
                if (r6 == 0) goto L62
                boolean r6 = r6.isEmpty()
                if (r6 != 0) goto L62
                android.content.Intent r6 = r8.mIntent
                java.util.ArrayList<android.net.Uri> r7 = r8.mStreams
                java.lang.Object r2 = r7.get(r2)
                android.os.Parcelable r2 = (android.os.Parcelable) r2
                r6.putExtra(r5, r2)
                goto L67
            L62:
                android.content.Intent r2 = r8.mIntent
                r2.removeExtra(r5)
            L67:
                r8.mStreams = r1
            L69:
                if (r0 == 0) goto L89
                if (r3 != 0) goto L89
                android.content.Intent r1 = r8.mIntent
                r1.setAction(r4)
                java.util.ArrayList<android.net.Uri> r1 = r8.mStreams
                if (r1 == 0) goto L84
                boolean r1 = r1.isEmpty()
                if (r1 != 0) goto L84
                android.content.Intent r1 = r8.mIntent
                java.util.ArrayList<android.net.Uri> r2 = r8.mStreams
                r1.putParcelableArrayListExtra(r5, r2)
                goto L89
            L84:
                android.content.Intent r1 = r8.mIntent
                r1.removeExtra(r5)
            L89:
                android.content.Intent r1 = r8.mIntent
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.ShareCompat.IntentBuilder.getIntent():android.content.Intent");
        }

        Activity getActivity() {
            return this.mActivity;
        }

        private void combineArrayExtra(String extra, ArrayList<String> add) {
            String[] currentAddresses = this.mIntent.getStringArrayExtra(extra);
            int currentLength = currentAddresses != null ? currentAddresses.length : 0;
            String[] finalAddresses = new String[add.size() + currentLength];
            add.toArray(finalAddresses);
            if (currentAddresses != null) {
                System.arraycopy(currentAddresses, 0, finalAddresses, add.size(), currentLength);
            }
            this.mIntent.putExtra(extra, finalAddresses);
        }

        private void combineArrayExtra(String extra, String[] add) {
            Intent intent = getIntent();
            String[] old = intent.getStringArrayExtra(extra);
            int oldLength = old != null ? old.length : 0;
            String[] result = new String[add.length + oldLength];
            if (old != null) {
                System.arraycopy(old, 0, result, 0, oldLength);
            }
            System.arraycopy(add, 0, result, oldLength, add.length);
            intent.putExtra(extra, result);
        }

        public Intent createChooserIntent() {
            return Intent.createChooser(getIntent(), this.mChooserTitle);
        }

        public void startChooser() {
            this.mActivity.startActivity(createChooserIntent());
        }

        public IntentBuilder setChooserTitle(CharSequence title) {
            this.mChooserTitle = title;
            return this;
        }

        public IntentBuilder setChooserTitle(int resId) {
            return setChooserTitle(this.mActivity.getText(resId));
        }

        public IntentBuilder setType(String mimeType) {
            this.mIntent.setType(mimeType);
            return this;
        }

        public IntentBuilder setText(CharSequence text) {
            this.mIntent.putExtra("android.intent.extra.TEXT", text);
            return this;
        }

        public IntentBuilder setHtmlText(String htmlText) {
            this.mIntent.putExtra(IntentCompat.EXTRA_HTML_TEXT, htmlText);
            if (!this.mIntent.hasExtra("android.intent.extra.TEXT")) {
                setText(Html.fromHtml(htmlText));
            }
            return this;
        }

        public IntentBuilder setStream(Uri streamUri) {
            if (!this.mIntent.getAction().equals("android.intent.action.SEND")) {
                this.mIntent.setAction("android.intent.action.SEND");
            }
            this.mStreams = null;
            this.mIntent.putExtra("android.intent.extra.STREAM", streamUri);
            return this;
        }

        public IntentBuilder addStream(Uri streamUri) {
            Uri currentStream = (Uri) this.mIntent.getParcelableExtra("android.intent.extra.STREAM");
            if (currentStream == null) {
                return setStream(streamUri);
            }
            if (this.mStreams == null) {
                this.mStreams = new ArrayList<>();
            }
            if (currentStream != null) {
                this.mIntent.removeExtra("android.intent.extra.STREAM");
                this.mStreams.add(currentStream);
            }
            this.mStreams.add(streamUri);
            return this;
        }

        public IntentBuilder setEmailTo(String[] addresses) {
            if (this.mToAddresses != null) {
                this.mToAddresses = null;
            }
            this.mIntent.putExtra("android.intent.extra.EMAIL", addresses);
            return this;
        }

        public IntentBuilder addEmailTo(String address) {
            if (this.mToAddresses == null) {
                this.mToAddresses = new ArrayList<>();
            }
            this.mToAddresses.add(address);
            return this;
        }

        public IntentBuilder addEmailTo(String[] addresses) {
            combineArrayExtra("android.intent.extra.EMAIL", addresses);
            return this;
        }

        public IntentBuilder setEmailCc(String[] addresses) {
            this.mIntent.putExtra("android.intent.extra.CC", addresses);
            return this;
        }

        public IntentBuilder addEmailCc(String address) {
            if (this.mCcAddresses == null) {
                this.mCcAddresses = new ArrayList<>();
            }
            this.mCcAddresses.add(address);
            return this;
        }

        public IntentBuilder addEmailCc(String[] addresses) {
            combineArrayExtra("android.intent.extra.CC", addresses);
            return this;
        }

        public IntentBuilder setEmailBcc(String[] addresses) {
            this.mIntent.putExtra("android.intent.extra.BCC", addresses);
            return this;
        }

        public IntentBuilder addEmailBcc(String address) {
            if (this.mBccAddresses == null) {
                this.mBccAddresses = new ArrayList<>();
            }
            this.mBccAddresses.add(address);
            return this;
        }

        public IntentBuilder addEmailBcc(String[] addresses) {
            combineArrayExtra("android.intent.extra.BCC", addresses);
            return this;
        }

        public IntentBuilder setSubject(String subject) {
            this.mIntent.putExtra("android.intent.extra.SUBJECT", subject);
            return this;
        }
    }

    public static class IntentReader {
        private static final String TAG = "IntentReader";
        private Activity mActivity;
        private ComponentName mCallingActivity;
        private String mCallingPackage;
        private Intent mIntent;
        private ArrayList<Uri> mStreams;

        public static IntentReader from(Activity activity) {
            return new IntentReader(activity);
        }

        private IntentReader(Activity activity) {
            this.mActivity = activity;
            this.mIntent = activity.getIntent();
            this.mCallingPackage = ShareCompat.getCallingPackage(activity);
            this.mCallingActivity = ShareCompat.getCallingActivity(activity);
        }

        public boolean isShareIntent() {
            String action = this.mIntent.getAction();
            return "android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action);
        }

        public boolean isSingleShare() {
            return "android.intent.action.SEND".equals(this.mIntent.getAction());
        }

        public boolean isMultipleShare() {
            return "android.intent.action.SEND_MULTIPLE".equals(this.mIntent.getAction());
        }

        public String getType() {
            return this.mIntent.getType();
        }

        public CharSequence getText() {
            return this.mIntent.getCharSequenceExtra("android.intent.extra.TEXT");
        }

        public String getHtmlText() {
            String result = this.mIntent.getStringExtra(IntentCompat.EXTRA_HTML_TEXT);
            if (result == null) {
                CharSequence text = getText();
                if (text instanceof Spanned) {
                    return Html.toHtml((Spanned) text);
                }
                if (text != null) {
                    return ShareCompat.IMPL.escapeHtml(text);
                }
                return result;
            }
            return result;
        }

        public Uri getStream() {
            return (Uri) this.mIntent.getParcelableExtra("android.intent.extra.STREAM");
        }

        public Uri getStream(int index) {
            if (this.mStreams == null && isMultipleShare()) {
                this.mStreams = this.mIntent.getParcelableArrayListExtra("android.intent.extra.STREAM");
            }
            ArrayList<Uri> arrayList = this.mStreams;
            if (arrayList != null) {
                return arrayList.get(index);
            }
            if (index == 0) {
                return (Uri) this.mIntent.getParcelableExtra("android.intent.extra.STREAM");
            }
            throw new IndexOutOfBoundsException("Stream items available: " + getStreamCount() + " index requested: " + index);
        }

        public int getStreamCount() {
            if (this.mStreams == null && isMultipleShare()) {
                this.mStreams = this.mIntent.getParcelableArrayListExtra("android.intent.extra.STREAM");
            }
            ArrayList<Uri> arrayList = this.mStreams;
            if (arrayList != null) {
                return arrayList.size();
            }
            return this.mIntent.hasExtra("android.intent.extra.STREAM") ? 1 : 0;
        }

        public String[] getEmailTo() {
            return this.mIntent.getStringArrayExtra("android.intent.extra.EMAIL");
        }

        public String[] getEmailCc() {
            return this.mIntent.getStringArrayExtra("android.intent.extra.CC");
        }

        public String[] getEmailBcc() {
            return this.mIntent.getStringArrayExtra("android.intent.extra.BCC");
        }

        public String getSubject() {
            return this.mIntent.getStringExtra("android.intent.extra.SUBJECT");
        }

        public String getCallingPackage() {
            return this.mCallingPackage;
        }

        public ComponentName getCallingActivity() {
            return this.mCallingActivity;
        }

        public Drawable getCallingActivityIcon() {
            if (this.mCallingActivity == null) {
                return null;
            }
            PackageManager pm = this.mActivity.getPackageManager();
            try {
                return pm.getActivityIcon(this.mCallingActivity);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Could not retrieve icon for calling activity", e);
                return null;
            }
        }

        public Drawable getCallingApplicationIcon() {
            if (this.mCallingPackage == null) {
                return null;
            }
            PackageManager pm = this.mActivity.getPackageManager();
            try {
                return pm.getApplicationIcon(this.mCallingPackage);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Could not retrieve icon for calling application", e);
                return null;
            }
        }

        public CharSequence getCallingApplicationLabel() {
            if (this.mCallingPackage == null) {
                return null;
            }
            PackageManager pm = this.mActivity.getPackageManager();
            try {
                return pm.getApplicationLabel(pm.getApplicationInfo(this.mCallingPackage, 0));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Could not retrieve label for calling application", e);
                return null;
            }
        }
    }
}
