package com.example.webactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
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

        try {
            if (isNetworkConnected(getApplicationContext())) {
//                Toast.makeText(getApplicationContext(),
//                        "Инет есть!", Toast.LENGTH_SHORT).show();
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (!isInterrupted()) {
                                Thread.sleep(10000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //CALL ANY METHOD OR ANY URL OR FUNCTION or any view
                                        Toast.makeText(getApplicationContext(),
                                                "Отсутствует интернет соединение", Toast.LENGTH_SHORT).show();
                                        finish();
                                        startActivity(getIntent());
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                }.start();
//                Toast.makeText(getApplicationContext(),
//                    "Отсутствует интернет соединение", Toast.LENGTH_SHORT).show();
//                finish();
//                startActivity(getIntent());
            }
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

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

    public static boolean isNetworkConnected(Context context) {
        boolean result = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // для 28+ версии
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = true;
                    }
                }
            }
        } else {
            // до 28+ версии
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = true;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    // Кнопка назад
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
