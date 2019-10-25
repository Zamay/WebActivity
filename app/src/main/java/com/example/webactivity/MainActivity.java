package com.example.webactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Handler statusUpdateHandler = new Handler();
    private Runnable statusUpdateRunnable;
    public ProgressBar spinner;
    private RequestQueue queue;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;

    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String APP_PREFERENCES = "mysettings";
    private static final String APP_PREFERENCES_W = "whitePage";

    private boolean backPressedOnce = false;
    private boolean whitePage = false;
    private static String promoType, promoUrl;
    private static final String whitePageUrl = "file:///android_asset/index.html";
    private static final String url = "https://android-app-rest.pencompcock.online/params?id=" + PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        queue = Volley.newRequestQueue(this);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();

        webView = findViewById(R.id.webView);

        if(mSettings.contains(APP_PREFERENCES_W)) {
            whitePage = mSettings.getBoolean(APP_PREFERENCES_W, false);
            Log.d("whitePage", "... " + whitePage);
        }
        webViewSettings();

        if(whitePage) {
            Log.d("test", "whitePage true");
            webView.loadUrl(whitePageUrl);
        } else {
            Log.d("test", "whitePage false");
            stringRequestGet();
            editor.putBoolean(APP_PREFERENCES_W, true);
            editor.apply();
        }
    }

    private void webViewSettings() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
//        webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");
//        webView.getSettings().setAppCachePath("/data/data/" + getPackageName() + "/cache");
        webView.getSettings().setAppCachePath(getCacheDir().getAbsolutePath() + "/cache");
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebViewClient(new CustomWebViewClient(){
            @Override public void onPageFinished(WebView view, String url) {
                spinner.setVisibility(View.GONE);
                webView.saveWebArchive("file://test.mht");
                Toast.makeText(getApplicationContext(), "Страница загружена!", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("url", url);
                Toast.makeText(getApplicationContext(), "Начата загрузка страницы", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void stringRequestGet() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Successful response", "Successful response");
                        parseData(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", String.valueOf(error));
            }
        });
        queue.add(stringRequest);
    }

    private void parseData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            promoType = jsonObject.getString("promoType");
            promoUrl = jsonObject.getString("promoUrl");
            String loadUrl = "";

            if (promoType.equals("l")) {
                loadUrl = promoUrl;
            } else if (promoType.equals("p")) {
                loadUrl = promoUrl;
            } else if (promoType.equals("w")) {
                whitePage = true;
                loadUrl = whitePageUrl;
                editor.putBoolean(APP_PREFERENCES_W, whitePage);
                editor.apply();
            }

//            Log.d("url", loadUrl);
            webView.loadUrl(loadUrl);
        } catch (JSONException e) {
            Log.e("parseDataError", e.getMessage());
            e.printStackTrace();
        }
    }

    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (backPressedOnce) {
                super.onBackPressed();
            }

            backPressedOnce = true;
            final Toast toast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT);
            toast.show();

            statusUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    backPressedOnce = false;
                    toast.cancel();
                }
            };
            statusUpdateHandler.postDelayed(statusUpdateRunnable, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusUpdateHandler != null) {
            statusUpdateHandler.removeCallbacks(statusUpdateRunnable);
        }
    }
}
