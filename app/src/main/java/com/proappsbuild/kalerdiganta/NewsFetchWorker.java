package com.proappsbuild.kalerdiganta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
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
        String latestNewsTitle = fetchLatestNewsFromFeed();

        if (latestNewsTitle != null) {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String lastStoredTitle = preferences.getString(LAST_NEWS_TITLE, null);

            // Compare the last stored news title with the fetched one
            if (lastStoredTitle == null || !latestNewsTitle.equals(lastStoredTitle)) {
                // Update stored news title
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(LAST_NEWS_TITLE, latestNewsTitle);
                editor.apply();

                // Notify the user of the new news
                showNotification(latestNewsTitle);
            }
        }
        return Result.success();
    }

    private String fetchLatestNewsFromFeed() {
        String latestNewsTitle = null;
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
                latestNewsTitle = item.getElementsByTagName("title").item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latestNewsTitle;
    }

    private void showNotification(String newsTitle) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "news_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Latest News", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.logo) // Replace with your notification icon
                .setContentTitle("New News")
                .setContentText(newsTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}
