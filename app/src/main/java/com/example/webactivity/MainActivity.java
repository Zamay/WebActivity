package com.example.webactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

import static com.example.webactivity.ConnectivityHelper.hasActiveInternetConnection;
import static com.example.webactivity.ConnectivityHelper.isNetworkConnected;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private boolean backPressedOnce = false;
    private Handler statusUpdateHandler = new Handler();
    private Runnable statusUpdateRunnable;
    private ProgressBar spinner;

    private static String promoType, promoUrl;
    private static String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static String url = "http://android-app-rest.zinenko.net/api/entry?package=" + PACKAGE_NAME;

    public RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        queue = Volley.newRequestQueue(this);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().getUserAgentString();
        webView.setWebViewClient(new CustomWebViewClient());

        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                spinner.setVisibility(View.GONE);
            }
        });

//        checkConnection();

        stringRequestGet();
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

    public void parseData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            promoType = jsonObject.getString("promoType");
            promoUrl = jsonObject.getString("promoUrl");
            String responseUrl = "";

            if (promoType.equals("l")) {
                responseUrl = promoUrl;
            } else if (promoType.equals("p")) {
                responseUrl = promoUrl;
            } else if (promoType.equals("w")) {
                responseUrl = "file:///android_asset/game-js.html";
            }

//            Log.d("url", responseUrl);
            webView.loadUrl(responseUrl);
        } catch (JSONException e) {
            Log.e("parseDataError", e.getMessage());
            e.printStackTrace();
        }
    }

//    public void checkConnection() {
//
//        if(hasActiveInternetConnection(getApplicationContext())) {
//            Log.d("internet status","Internet Access");
//            stringRequestGet();
//        } else {
//            Log.d("internet status","no Internet Access");
//            Toast.makeText(getApplicationContext(),
//                    "Отсутствует интернет соединение", Toast.LENGTH_SHORT).show();
//        }
//    }

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
