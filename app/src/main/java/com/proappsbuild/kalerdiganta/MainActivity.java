package com.proappsbuild.kalerdiganta;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private WebView webView;
    private Animation buttonClickAnimation;
    private ProgressBar progressBar;
    LinearLayout no_network;

    // Cache management
    private static final String CACHE_PREFS = "cache_prefs";
    private static final String LAST_CACHE_CLEAN = "last_cache_clean";
    private static final long CACHE_EXPIRY_DAYS = 7;
    private static final long CACHE_EXPIRY_MILLIS = CACHE_EXPIRY_DAYS * 24 * 60 * 60 * 1000L;

    // Network state receiver
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                updateCacheMode();

                if (isNetworkAvailable()) {
                    // Network is back - hide offline UI and reload if needed
                    if (no_network.getVisibility() == View.VISIBLE) {
                        no_network.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                        webView.reload();
                        Toast.makeText(MainActivity.this, "ইন্টারনেট সংযোগ ফিরে এসেছে", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Network lost - update cache mode
                    webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                }
            }
        }
    };

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

        // Setup WebView with offline caching
        setupWebViewWithCache();

        // Clean old cache if needed
        cleanOldCacheIfNeeded();

        // Set WebViewClient to load pages inside the WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Show the progress bar when page starts loading
                progressBar.setVisibility(View.VISIBLE);
                no_network.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Hide the progress bar when page finishes loading
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                if (!isNetworkAvailable()) {
                    // Show offline UI and try to load from cache
                    showOfflineUI();
                } else {
                    // Network available but still error - show error message
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "পেজ লোড করতে সমস্যা হচ্ছে। আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (!isNetworkAvailable()) {
                    showOfflineUI();
                }
            }
        });

        String url = getIntent().getStringExtra("newsLink");
        // Load the desired URL with proper cache handling
        if (url != null) {
            loadUrlWithCacheHandling(url);
        } else {
            loadUrlWithCacheHandling("https://kalerdiganta.com");
        }

        buttonHome.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation);
            loadUrlWithCacheHandling("https://kalerdiganta.com");
        });

        buttonNews.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation); // Start the animation

            // Show a dialog with options
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(" বিষয় ভিত্তিক খবর ");

            String[] options = {"জাতীয়", "রাজনীতি", "অর্থনীতি", "আন্তর্জাতিক", "শিল্প-সাহিত্য", "ইসলামী বিশ্ব", "ইসলামিক", "সারাদেশ"};

            builder.setItems(options, (dialog, which) -> {
                // Perform actions based on the selected option
                switch (which) {
                    case 0:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/national");
                        break;
                    case 1:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/politics");
                        break;
                    case 2:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/economy");
                        break;
                    case 3:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/international");
                        break;
                    case 4:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/art-literature");
                        break;
                    case 5:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/islamic-world");
                        break;
                    case 6:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/islamic");
                        break;
                    case 7:
                        loadUrlWithCacheHandling("https://kalerdiganta.com/news/category/bangladesh");
                }
            });

            // Show the dialog
            builder.show();
        });

        buttonSettings.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Settings")
                    .setMessage("Choose an option:")
                    .setPositiveButton("Clear Cache", (dialog, which) -> {
                        clearWebViewCache();
                        Toast.makeText(MainActivity.this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Check for Updates", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNeutralButton("Feedback", (dialog, which) -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"soaib.softdev@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Kalerdiganta App");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Write your feedback here...");
                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send Feedback Email"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(MainActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        });

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

        // Register network state receiver
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    private void setupWebViewWithCache() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Enable modern caching features
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(true);

        // Set cache mode based on network availability
        if (isNetworkAvailable()) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        // Enable service worker for better caching (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webSettings.setOffscreenPreRaster(true);
        }
    }

    private void loadUrlWithCacheHandling(String url) {
        // Always update cache mode based on current network status
        updateCacheMode();

        if (isNetworkAvailable()) {
            // Online: Load normally with cache support
            webView.loadUrl(url);
        } else {
            // Offline: Try to load from cache
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            webView.loadUrl(url);

            // Set a timeout to check if page loaded from cache
            webView.postDelayed(() -> {
                if (webView.getProgress() < 100) {
                    showOfflineUI();
                }
            }, 3000); // 3 second timeout
        }
    }

    private void updateCacheMode() {
        if (isNetworkAvailable()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
    }

    private void showOfflineUI() {
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        no_network.setVisibility(View.VISIBLE);
        Toast.makeText(this, "ইন্টারনেট সংযোগ নেই। ক্যাশে করা কন্টেন্ট খুঁজে পাওয়া যায়নি।", Toast.LENGTH_LONG).show();
    }

    private void showOfflineMessage() {
        Toast.makeText(this, "অফলাইনে আছেন। ক্যাশে করা কন্টেন্ট দেখানো হচ্ছে।", Toast.LENGTH_LONG).show();
    }

    private void cleanOldCacheIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        long lastCleanTime = prefs.getLong(LAST_CACHE_CLEAN, 0);
        long currentTime = System.currentTimeMillis();

        // Clean cache every 7 days
        if (currentTime - lastCleanTime > CACHE_EXPIRY_MILLIS) {
            clearWebViewCache();
            prefs.edit().putLong(LAST_CACHE_CLEAN, currentTime).apply();

            // Schedule automatic cache cleanup
            scheduleAutoCacheCleanup();
        }
    }

    private void clearWebViewCache() {
        // Clear WebView cache and data
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();

        // Clear WebView databases
        webView.getContext().deleteDatabase("webview.db");
        webView.getContext().deleteDatabase("webviewCache.db");

        // Clear application cache
        try {
            File cacheDir = getCacheDir();
            if (cacheDir.exists()) {
                deleteDir(cacheDir);
            }

            // Clear WebView specific cache directories
            File appDir = new File(getApplicationInfo().dataDir);
            File webViewDir = new File(appDir, "app_webview");
            if (webViewDir.exists()) {
                deleteDir(webViewDir);
            }

            // Clear shared preferences cache data
            SharedPreferences prefs = getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void scheduleAutoCacheCleanup() {
        // Create a periodic work request to clean cache every 7 days
        PeriodicWorkRequest cacheCleanupRequest = new PeriodicWorkRequest.Builder(
                CacheCleanupWorker.class, 7, TimeUnit.DAYS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CacheCleanupWorker",
                ExistingPeriodicWorkPolicy.REPLACE,
                cacheCleanupRequest
        );
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

    @Override
    protected void onResume() {
        super.onResume();
        // Update cache mode and UI based on current network status
        updateCacheMode();

        // Hide offline UI if network is back
        if (isNetworkAvailable() && no_network.getVisibility() == View.VISIBLE) {
            no_network.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            // Reload current page if we were showing offline UI
            webView.reload();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister network receiver
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }

        if (webView != null) {
            webView.destroy();
        }
    }
}