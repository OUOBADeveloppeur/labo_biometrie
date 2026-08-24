package com.hf.ui.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hf.ui.R;

import java.util.Map;

/**
 * @author tx
 * @date 2023/6/21 10:18
 * @target WEBVIEW壳子
 */
public abstract class EasyWebActivity<T extends EasyWebActivity.EasyWebviewFunction> extends EasyActivity {

    public WebView webView;

    private String url = "";

    public static void start(Context context, Class c, String url) {
        Intent intent = new Intent(context, c);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public abstract T getFunctionBridge();

    public static abstract class EasyWebviewFunction {
        public EasyActivity activity;

        public EasyWebviewFunction() {
        }

        public EasyWebviewFunction(EasyActivity easyActivity) {
            this.activity = easyActivity;
        }

        @JavascriptInterface
        public void finish() {
            activity.finish();
        }

        public abstract WebView getWebview();

        public void sendMessage(String msg) {
            getWebview().evaluateJavascript(
                    "javascript:receiveMessage(" + msg + ")",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
        }
    }

    @Override
    public void onMessageRecieve(String name, EasyEvent msg) {

    }

    @Override
    public void create() {
        setContentView(R.layout.activity_easy_web);

        webView = findViewById(R.id.webview);

        url = getIntent().getStringExtra("url");
        setWebView();
    }

    private void setWebView() {
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        CookieManager cookieManager = CookieManager.getInstance();
        Map<String, String> header = easyRequester.getHeader();
        for (String key : header.keySet()) {
            cookieManager.setCookie(getIntent().getStringExtra("url"), String.format(key + "=%s", header.get(key)));
        }
        webView.setNetworkAvailable(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setBlockNetworkLoads(false);
        webView.getSettings().setBlockNetworkImage(false);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.addJavascriptInterface(getFunctionBridge(), "HFJSFunction");
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        //设置定位的数据库路径
        webView.getSettings().setGeolocationDatabasePath(dir);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                    return true;
                } else {
                    view.loadUrl(getIntent().getStringExtra("url"));
                    return false;
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            // 处理javascript中的alert
            public boolean onJsAlert(WebView view, String url, String message,
                                     final JsResult result) {
                return true;
            }

            // 处理javascript中的confirm
            public boolean onJsConfirm(WebView view, String url,
                                       String message, final JsResult result) {
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });
        webView.loadUrl(url);
    }

    @Override
    public void restart() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }
}
