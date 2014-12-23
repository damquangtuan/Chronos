package com.javaclass.androidcalendar;

        import android.app.Activity;
        import android.os.Bundle;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.webkit.WebSettings;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;
        import android.widget.EditText;

public class WeatherActivity extends Activity {
    private WebView webview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        webview = (WebView)findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient());
        WebSettings set = webview.getSettings();
        set.setJavaScriptEnabled(true); //Javascript enable in web
        set.setBuiltInZoomControls(true); //ZoomControl enable
        webview.loadUrl("https://weather.yahoo.com");

//        findViewById(R.id.btnStart).setOnClickListener(onclick);
    }

//    OnClickListener onclick = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            System.out.println("click");
//            String url = null;
//            EditText add = (EditText)findViewById(R.id.add);
//            url = add.getText().toString();
//            myWebView.loadUrl(url);
//        }
//    };

//    class WebClient extends WebViewClient {
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//            return true;
//        }
//    }
}