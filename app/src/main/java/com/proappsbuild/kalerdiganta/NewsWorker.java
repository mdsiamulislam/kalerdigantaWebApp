package com.proappsbuild.kalerdiganta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.proappsbuild.kalerdiganta.R;

import org.xmlpull.v1.XmlPullParserException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NewsWorker extends Worker {

    private static final String LAST_NEWS_PREF = "last_news_title";
    private static final String NEWS_CHANNEL_ID = "news_channel";

    public NewsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Fetch RSS feed
            URL url = new URL("https://kalerdiganta.com/feed/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            // Parse the RSS feed
            RSSParser rssParser = new RSSParser();
            List<NewsItem> newsItems = rssParser.parse(inputStream);

            // Check for new news
            if (newsItems != null && !newsItems.isEmpty()) {
                NewsItem latestNews = newsItems.get(0);
                String lastTitle = getLastNewsTitle();

                if (!latestNews.getTitle().equals(lastTitle)) {
                    // Send notification
                    sendNotification(latestNews.getTitle(), latestNews.getLink());

                    // Store the latest news title
                    saveLastNewsTitle(latestNews.getTitle());
                }
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void sendNotification(String title, String link) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NEWS_CHANNEL_ID, "News Channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NEWS_CHANNEL_ID)
                .setContentTitle("New News: " + title)
                .setContentText("Click to read more")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }

    private String getLastNewsTitle() {
        return getApplicationContext().getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
                .getString(LAST_NEWS_PREF, "");
    }

    private void saveLastNewsTitle(String title) {
        getApplicationContext().getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(LAST_NEWS_PREF, title)
                .apply();
    }
}
