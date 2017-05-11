package com.example.android.newsreader;

import android.content.res.Resources;

/**
 * Created by toddskinner on 4/24/17.
 */

public class Article {
    private String mDescription;
    private String mWebPublicationDate;
    private String mWebTitle;
    private String mWebUrl;
    private String mThumbnailUrl;

    public Article (String description, String webPublicationDate, String webTitle, String webUrl, String thumbnailUrl){
        mDescription = description;
        mWebPublicationDate = webPublicationDate;
        mWebTitle = webTitle;
        mWebUrl = webUrl;
        mThumbnailUrl = thumbnailUrl;
    }

    public String getDescription(){
        return mDescription;
    }

    public String getWebPublicationDate(){
        if(mWebPublicationDate != null && !mWebPublicationDate.isEmpty()){
            if(mWebPublicationDate.length() > 10 ){
                int splitIndex = mWebPublicationDate.indexOf("T");
                String extractDate = mWebPublicationDate.substring(0, splitIndex);
                String year = extractDate.substring(0,4);
                String month = extractDate.substring(5,7);
                String day = extractDate.substring(8,10);
                String formattedDate = month + "-" + day + "-" + year;
                mWebPublicationDate = formattedDate;
            } else {
                return mWebPublicationDate;
            }

        } else {
            mWebPublicationDate = Resources.getSystem().getString(R.string.not_dated);
        }
        return mWebPublicationDate;
    }

    public String getWebTitle(){
        return mWebTitle;
    }

    public String getWebUrl(){
        return mWebUrl;
    }

    public String getThumbnailUrl(){
        return mThumbnailUrl;
    }
}


