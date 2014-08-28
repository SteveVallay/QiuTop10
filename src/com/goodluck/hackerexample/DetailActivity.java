package com.goodluck.hackerexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class DetailActivity extends Activity {

    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.details);
        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        Intent intent = getIntent();
        if (intent != null) {
            String data = intent.getExtras().getString("data");
            if (data != null) {
                log("data:" + data);
                mWebView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    public void log(String msg){
        Log.d("HACKER", msg);
    }
}
