package com.challenge.quotes.model;

import android.text.TextUtils;
import android.widget.EditText;

/**
 * Created by kbw815 on 9/2/15.
 */
public class Item {
    private int index;
    private String quote;
    private String author;

    public static Item fromCSV(String csv)  {
        if (TextUtils.isEmpty(csv))
            return null;

        Item item = new Item();
        String[] s = csv.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (s.length != 3)
            return null;
        try {
            item.setIndex(Integer.parseInt(s[0]));
            item.setQuote(s[1]);
            item.setAuthor(s[2]);
            return item;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }
}
