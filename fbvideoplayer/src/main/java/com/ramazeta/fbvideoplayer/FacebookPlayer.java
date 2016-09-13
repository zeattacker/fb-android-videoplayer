package com.ramazeta.fbvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * Created by zeta on 9/13/16.
 */
public class FacebookPlayer extends WebView {
    private FacebookBridging bridging = new FacebookBridging();
    private boolean AUTO_PLAY = false;
    private boolean SHOW_TEXT = false;
    private boolean SHOW_CAPTIONS = false;
    private FacebookListener fbListener;

    public FacebookPlayer(Context context) {
        super(context);
    }

    public FacebookPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initialize(String app_id,String videoUrl,FacebookListener listener){
        if(listener != null){
            this.fbListener = listener;
        }

        initialize(app_id, videoUrl);
    }

    public void initialize(String app_id,String videoUrl){
        WebSettings set = this.getSettings();
        set.setJavaScriptEnabled(true);
        set.setUseWideViewPort(true);
        set.setLoadWithOverviewMode(true);
        set.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        set.setCacheMode(WebSettings.LOAD_NO_CACHE);
        set.setPluginState(WebSettings.PluginState.ON);
        set.setPluginState(WebSettings.PluginState.ON_DEMAND);
        set.setAllowContentAccess(true);
        set.setAllowFileAccess(true);
        set.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0");

        this.setLayerType(View.LAYER_TYPE_NONE, null);
        this.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        this.addJavascriptInterface(bridging, "FacebookInterface");
        this.loadDataWithBaseURL("http://facebook.com", getVideoHTML(app_id, videoUrl), "text/html", "utf-8", null);
        this.setLongClickable(true);
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            setWebContentsDebuggingEnabled(true);
        }

        this.setWebChromeClient(new MyChromeClient());
    }

    public boolean isAutoPlay() {
        return AUTO_PLAY;
    }

    public void setAutoPlay(boolean AUTO_PLAY) {
        this.AUTO_PLAY = AUTO_PLAY;
    }

    public boolean isShowCaptions() {
        return SHOW_CAPTIONS;
    }

    public void setShowCaptions(boolean SHOW_CAPTIONS) {
        this.SHOW_CAPTIONS = SHOW_CAPTIONS;
    }

    public boolean isShowText() {
        return SHOW_TEXT;
    }

    public void setShowText(boolean SHOW_TEXT) {
        this.SHOW_TEXT = SHOW_TEXT;
    }

    private final class MyChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d("FBVPlayer", consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    public void seek(double second){
        this.loadUrl("javascript:seekVideo(" + second + ")");
    }

    public void play(){
        this.loadUrl("javascript:playVideo()");
    }

    public void pause(){
        this.loadUrl("javascript:pauseVideo()");
    }

    public void mute(){
        this.loadUrl("javascript:muteVideo()");
    }

    public void unmute(){
        this.loadUrl("javascript:unMuteVideo()");
    }

    public void setVolume(float vol){
        this.loadUrl("javascript:setVolume(" + vol + ")");
    }

    public void getDuration(){ this.loadUrl("javascript:console.log(getDuration())");}

    public void getVolume(){ this.loadUrl("javascript:console.log(getVolume())");}

    public void isMuted() { this.loadUrl("javascript:console.log(isMuted())"); }

    public void getCurrentPosition() { this.loadUrl("javascript:console.log(getCurrentPosition())"); }

    private class FacebookBridging {
        @JavascriptInterface
        public void onStartBuffer(String arg){
            Log.d("FBPlayer", "On Start Buffer : " + arg);
            fbListener.onStartBuffer();
        }

        @JavascriptInterface
        public void onFinishBuffering(String arg){
            Log.d("FBPlayer", "On Finish Buffer : " + arg);
            fbListener.onFinishBuffering();
        }

        @JavascriptInterface
        public void onStartPlaying(String arg){
            Log.d("FBPlayer", "On Start Playing : " + arg);
            fbListener.onStartPlaying();
        }

        @JavascriptInterface
        public void onFinishPlaying(String arg){
            Log.d("FBPlayer", "On Finish Playing : " + arg);
            fbListener.onFinishBuffering();
        }

        @JavascriptInterface
        public void onPaused(String arg){
            Log.d("FBPlayer", "On Paused : " + arg);
            fbListener.onPaused();
        }

        @JavascriptInterface
        public void onError(String arg){
            Log.d("FBPlayer", "On Error : " + arg);
            fbListener.onError();
        }
    }

    public interface FacebookListener {
        void onStartBuffer();

        void onFinishBuffering();

        void onStartPlaying();

        void onFinishPlaying();

        void onPaused();

        void onError();

    }

    private static Field sConfigCallback;

    static {
        try {
            sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
            sConfigCallback.setAccessible(true);
        } catch (Exception e) {
            // ignored
        }
    }

    private class MyWebViewClient extends WebViewClient {
        protected WeakReference<Activity> activityRef;

        public MyWebViewClient(Activity activity) {
            this.activityRef = new WeakReference<Activity>(activity);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                final Activity activity = activityRef.get();
                if (activity != null)
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (RuntimeException ignored) {
                // ignore any url parsing exceptions
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private String getVideoHTML(String videoId,String videoUrl){
        try {
            InputStream in = getResources().openRawResource(R.raw.players);
            if (in != null) {
                InputStreamReader stream = new InputStreamReader(in, "utf-8");
                BufferedReader buffer = new BufferedReader(stream);
                String read;
                StringBuilder sb = new StringBuilder("");

                while ((read = buffer.readLine()) != null) {
                    sb.append(read + "\n");
                }

                in.close();

                String html = sb.toString()
                        .replace("{app_id}", videoId)
                        .replace("{video_url}", videoUrl)
                        .replace("{auto_play}", valueOf(isAutoPlay()))
                        .replace("{show_text}", valueOf(isShowText()))
                        .replace("{show_captions}", valueOf(isShowCaptions()));
                Log.d("FBVPLAYER", html);
                return html;
            }
        } catch (Exception e){

        }

        return "";
    }

    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }

    public void setAutoPlayerHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.getLayoutParams().height = (int) (displayMetrics.widthPixels * 0.5625);
    }
}
