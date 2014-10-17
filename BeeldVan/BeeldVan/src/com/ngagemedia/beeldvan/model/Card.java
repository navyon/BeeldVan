package com.ngagemedia.beeldvan.model;

/**
 * Created by admin on 9/19/2014.
 */
public class Card {
    private String line1;
    private String line2;
    private String date;
    private String url;

    public Card(String url, String line1, String line2, String date) {
        this.line1 = line1;
        this.line2 = line2;
        this.date = date;
        this.url = url;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

}