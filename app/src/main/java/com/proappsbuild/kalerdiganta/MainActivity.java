package com.proappsbuild.kalerdiganta;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private WebView webView;
    private Animation buttonClickAnimation;
    private ProgressBar progressBar;
    LinearLayout no_network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the window from the activity
        Window window = getWindow();

// Clear the FLAG_TRANSLUCENT_STATUS flag if it's set
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// Add the FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window to enable coloring the status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// Finally, change the status bar color. Ensure that the context is passed correctly.
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.red)); // `this` refers to the activity context


        webView = findViewById(R.id.webview);
        // Initialize buttons
        ImageButton buttonHome = findViewById(R.id.button_home);
        ImageButton buttonNews = findViewById(R.id.button_news);
        ImageButton buttonSettings = findViewById(R.id.button_settings);

        no_network = findViewById(R.id.no_network);

        progressBar = findViewById(R.id.progress_bar);

        // Load the animation
        buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation);








        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);// Enable JavaScript
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);


        // Set WebViewClient to load pages inside the WebView
        webView.setWebViewClient(new WebViewClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Show the progress bar when page starts loading
                progressBar.setVisibility(View.VISIBLE);
                if (!isNetworkAvailable()) {
                    no_network.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    no_network.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Hide the progress bar when page finishes loading
                progressBar.setVisibility(View.GONE);
            }
        });

        // Load the desired URL
        webView.loadUrl("https://kalerdiganta.com/");



        buttonHome.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation);
            webView.loadUrl("https://kalerdiganta.com");
        });
        buttonNews.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation); // Start the animation

            // Show a dialog with options
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(" বিষয় ভিত্তিক খবর ");

            // Options for Dhaka and Chattogram
//            String[] options = {"ঢাকা বিভাগ", "চট্টগ্রাম বিভাগ", "রাজশাহী বিভাগ", "খুলনা বিভাগ", "বরিশাল বিভাগ", "ময়মনসিংহ বিভাগ", "সিলেট বিভাগ", "রংপুর বিভাগ"};
            String[] options = {"জাতীয়", "রাজনীতি", "অর্থনীতি", "আন্তর্জাতিক", "বিনোদন", "ইসলামী বিশ্ব", "ইসলামিক", "বিশেষ প্রতিবেদন"};

            builder.setItems(options, (dialog, which) -> {
                // Perform actions based on the selected option
                switch (which) {
                    case 0:
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%9c%e0%a6%be%e0%a6%a4%e0%a7%80%e0%a6%af%e0%a6%bc");
                        break;
                    case 1:
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%b0%e0%a6%be%e0%a6%9c%e0%a6%a8%e0%a7%80%e0%a6%a4%e0%a6%bf");
                        break;
                    case 2: // Dhaka selected
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%85%e0%a6%b0%e0%a7%8d%e0%a6%a5%e0%a6%a8%e0%a7%80%e0%a6%a4%e0%a6%bf");
                        break;
                    case 3:
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%86%e0%a6%a8%e0%a7%8d%e0%a6%a4%e0%a6%b0%e0%a7%8d%e0%a6%9c%e0%a6%be%e0%a6%a4%e0%a6%bf%e0%a6%95");
                        break;
                    case 4:
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%ac%e0%a6%bf%e0%a6%a8%e0%a7%8b%e0%a6%a6%e0%a6%a8");
                        break;
                    case 5:
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%87%e0%a6%b8%e0%a6%b2%e0%a6%be%e0%a6%ae%e0%a7%80-%e0%a6%ac%e0%a6%bf%e0%a6%b6%e0%a7%8d%e0%a6%ac");
                        break;
                    case 6:
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%87%e0%a6%b8%e0%a6%b2%e0%a6%be%e0%a6%ae%e0%a6%bf%e0%a6%95");
                        break;
                    case 7:
                        webView.loadUrl("https://kalerdiganta.com/category/%e0%a6%b0%e0%a6%82%e0%a6%aa%e0%a7%81%e0%a6%b0https://kalerdiganta.com/category/%e0%a6%ac%e0%a6%bf%e0%a6%b6%e0%a7%87%e0%a6%b7-%e0%a6%aa%e0%a7%8d%e0%a6%b0%e0%a6%a4%e0%a6%bf%e0%a6%ac%e0%a7%87%e0%a6%a6%e0%a6%a8");
                }
            });

            // Show the dialog
            builder.show();
        });

        buttonSettings.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("This Option is Under Development")
                    .setMessage("We're working hard to bring new features! In the meantime, you can:\n\n" +
                            "1. Provide feedback on what you'd like to see.\n" +
                            "2. Check for updates regularly.\n" +
                            "3. Return to the home screen.")
                    .setPositiveButton("Feedback", (dialog, which) -> {
                        // Handle feedback action (e.g., open feedback form or send feedback)
                        Toast.makeText(MainActivity.this, "Feedback option is not available yet.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Check for Updates", (dialog, which) -> {
                        // Handle update check action (e.g., open update page)
                        Toast.makeText(MainActivity.this, "Check for updates option is not available yet.", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Return to Home", (dialog, which) -> {
                        // Handle return to home action
                        // For example, you could navigate to the home fragment or activity
                        Toast.makeText(MainActivity.this, "Returning to home.", Toast.LENGTH_SHORT).show();
                        // Uncomment the next line to actually navigate if needed
                        // startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    })
                    .show();
        });
// Adjust the URL as needed



        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                scheduleNewsFetchWorker();
            }
        } else {
            scheduleNewsFetchWorker();
        }
    }

    private void scheduleNewsFetchWorker() {
        PeriodicWorkRequest newsFetchWorkRequest = new PeriodicWorkRequest.Builder(NewsFetchWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NewsFetchWorker",
                ExistingPeriodicWorkPolicy.REPLACE,
                newsFetchWorkRequest
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNewsFetchWorker();
            } else {
                // Handle the case where permission is denied
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onBackPressed() {

        // Check if the WebView can go back
        if (webView.canGoBack()) {
            webView.goBack(); // Go back in WebView history
        } else {
            super.onBackPressed(); // Call the default back press behavior
        }
    }

}
