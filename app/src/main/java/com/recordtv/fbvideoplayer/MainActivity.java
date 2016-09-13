package com.recordtv.fbvideoplayer;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.ramazeta.fbvideoplayer.FacebookPlayer;

public class MainActivity extends AppCompatActivity {
    private FacebookPlayer mWebView;
    private Button btnPlay,btnStop,btnMute,btnUnMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mWebView = (FacebookPlayer) findViewById(R.id.fbVideoPlayer);
        btnPlay = (Button)findViewById(R.id.play);
        btnStop = (Button)findViewById(R.id.stop);
        btnMute = (Button)findViewById(R.id.mute);
        btnUnMute = (Button)findViewById(R.id.unmute);

        initWebView();
        initButton();
    }

    private void initWebView(){
        mWebView.setAutoPlay(true);
        mWebView.setShowCaptions(false);
        mWebView.setShowText(false);
        mWebView.initialize("579831495370908", "https://www.facebook.com/facebook/videos/10153231379946729/");
        mWebView.setAutoPlayerHeight(this);
    }

    private void initButton(){
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.play();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.pause();
            }
        });

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.mute();
            }
        });

        btnUnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.unmute();
            }
        });
    }
}
