package com.ibashkimi.wheel.firebase.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class Content implements Parcelable {

    public static final Creator<Content> CREATOR = new Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };
    private String textContent;
    private String imageUrl;

    public Content() {
    }

    public Content(@Nullable String textContent, @Nullable String imageUrl) {
        this.textContent = textContent;
        this.imageUrl = imageUrl;
    }

    protected Content(Parcel in) {
        textContent = in.readString();
        imageUrl = in.readString();
    }

    public boolean containsImage() {
        return imageUrl != null;
    }

    public boolean containsText() {
        return textContent != null;
    }

    /*public boolean isValid() {
        return true;
    }*/

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Nullable
    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public String toString() {
        return textContent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(textContent);
        dest.writeString(imageUrl);
    }
}
