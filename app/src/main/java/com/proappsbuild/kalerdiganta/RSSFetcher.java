package com.proappsbuild.kalerdiganta;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RSSFetcher {

    private static final String RSS_URL = "https://kalerdiganta.com/feed/";

    public List<NewsItem> fetch() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(RSS_URL)
                .build();
        try(Response response = client.newCall(request).execute()){
            if(response.isSuccessful()){
                InputStream stream = response.body().byteStream();
                RSSParser parser = new RSSParser();
                return parser.parse(stream);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
