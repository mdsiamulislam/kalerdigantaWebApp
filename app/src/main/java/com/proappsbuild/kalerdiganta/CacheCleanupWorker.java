package com.proappsbuild.kalerdiganta;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;

public class CacheCleanupWorker extends Worker {

    private static final String CACHE_PREFS = "cache_prefs";
    private static final String LAST_CACHE_CLEAN = "last_cache_clean";

    public CacheCleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();

            // Clear WebView cache
            clearWebViewCache(context);

            // Update last cleanup time
            SharedPreferences prefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
            prefs.edit().putLong(LAST_CACHE_CLEAN, System.currentTimeMillis()).apply();

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void clearWebViewCache(Context context) {
        try {
            // Clear WebView databases
            String[] databases = {"webview.db", "webviewCache.db", "webview.db-shm", "webview.db-wal"};
            for (String database : databases) {
                context.deleteDatabase(database);
            }

            // Clear cache directory
            File cacheDir = context.getCacheDir();
            if (cacheDir.exists()) {
                deleteDir(cacheDir);
            }

            // Clear WebView cache directories
            File appDataDir = new File(context.getApplicationInfo().dataDir);

            // Common WebView cache directories
            String[] webViewDirs = {
                    "app_webview",
                    "app_chrome",
                    "cache/WebView",
                    "databases"
            };

            for (String dirName : webViewDirs) {
                File webViewCacheDir = new File(appDataDir, dirName);
                if (webViewCacheDir.exists()) {
                    deleteDir(webViewCacheDir);
                }
            }

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
}