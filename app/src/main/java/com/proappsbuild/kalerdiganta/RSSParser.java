package com.proappsbuild.kalerdiganta;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RSSParser {

    public List<NewsItem> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        List<NewsItem> items = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, null);

        int eventType = parser.getEventType();
        NewsItem currentItem = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("item".equals(tagName)) {
                        currentItem = new NewsItem();
                    } else if (currentItem != null) {
                        if ("title".equals(tagName)) {
                            currentItem.setTitle(parser.nextText());
                        } else if ("link".equals(tagName)) {
                            currentItem.setLink(parser.nextText());
                        } else if ("pubDate".equals(tagName)) {
                            currentItem.setPubDate(parser.nextText());
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if ("item".equals(tagName) && currentItem != null) {
                        items.add(currentItem);
                    }
                    break;
            }
            eventType = parser.next();
        }

        return items;
    }
}
