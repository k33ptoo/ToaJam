package com.keeptoo.toajam.home.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.keeptoo.toajam.R;

public class TwitterFragment extends Fragment {

    private static final String baseURl = "http://twitter.com";
    private static final String widgetInfo = "<a class=\"twitter-timeline\" href=\"https://twitter.com/Ma3Route?ref_src=twsrc%5Etfw\">Tweets by Ma3Route</a> <script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>";


    public TwitterFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_twitter, container, false);
        final LinearLayout linearLayout = view.findViewById(R.id.ly_loader);
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshTwitter);
        final WebView webView = view.findViewById(R.id.timeline_webview);
        final LottieAnimationView animationView = view.findViewById(R.id.animation_view_loader);

        loadWebContent(webView);

        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadWebContent(webView);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                webView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(true);
                showLoading(animationView);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                webView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                //show some delay
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(animationView);
                    }
                }, 2000);
            }
        });

        return view;
    }

    private void showLoading(LottieAnimationView animationView) {

        animationView.setAnimation("spider.json");
        animationView.loop(true);
        animationView.playAnimation();
    }

    private void loadWebContent(WebView webView) {
        webView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(baseURl, widgetInfo, "text/html", "UTF-8", null);

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}