package com.proappsbuild.kalerdiganta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.core.app.NotificationCompat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class NewsFetchWorker extends Worker {

    private static final String RSS_FEED_URL = "https://kalerdiganta.com/feed/";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String LAST_NEWS_TITLE = "lastNewsTitle";

    public NewsFetchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NewsItem latestNews = fetchLatestNewsFromFeed();

        if (latestNews != null) {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String lastStoredTitle = preferences.getString(LAST_NEWS_TITLE, null);

            // Compare the last stored news title with the fetched one
            if (lastStoredTitle == null || !latestNews.title.equals(lastStoredTitle)) {
                // Update stored news title
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(LAST_NEWS_TITLE, latestNews.title);
                editor.apply();

                // Notify the user of the new news
                showNotification(latestNews.title, latestNews.link);
            }
        }
        return Result.success();
    }

    private NewsItem fetchLatestNewsFromFeed() {
        NewsItem latestNews = null;
        try {
            URL url = new URL(RSS_FEED_URL);
            InputStream inputStream = url.openConnection().getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element root = doc.getDocumentElement();

            NodeList items = root.getElementsByTagName("item");
            if (items.getLength() > 0) {
                Element item = (Element) items.item(0); // Get the first news item
                String title = item.getElementsByTagName("title").item(0).getTextContent();
                String link = item.getElementsByTagName("link").item(0).getTextContent();
                latestNews = new NewsItem(title, link);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latestNews;
    }

    private void showNotification(String newsTitle, String newsLink) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "news_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Latest News", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent to open WebViewActivity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("newsLink", newsLink); // Pass the news link to the activity

        // Wrap the intent in a PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.logo) // Replace with your notification icon
                .setContentTitle("New News")
                .setContentText(newsTitle)
                .setContentIntent(pendingIntent) // Attach the pending intent
                .setAutoCancel(true) // Close the notification after clicking
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }


    // Helper class to store news title and link
    private static class NewsItem {
        String title;
        String link;

        NewsItem(String title, String link) {
            this.title = title;
            this.link = link;
        }
    }
}
