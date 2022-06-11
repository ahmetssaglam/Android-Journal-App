package data_classes;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Journal {

    public String heading;
    public String mainContent;
    public String date;
    public String mood;
    public byte[] imageArray;
    public Double latitude;
    public Double longitude;

    public String password;

    public Journal() {

    }

    public Journal(String heading, String mainContent, String date, String mood, byte[] imageArray, Double latitude, Double longitude) {
        this.heading = heading;
        this.mainContent = mainContent;
        this.date = date;
        this.mood = mood;
        this.imageArray = imageArray;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Journal(String heading, String mainContent, String date, String mood, byte[] imageArray, Double latitude, Double longitude, String password) {
        this.heading = heading;
        this.mainContent = mainContent;
        this.date = date;
        this.mood = mood;
        this.imageArray = imageArray;
        this.latitude = latitude;
        this.longitude = longitude;
        this.password = password;
    }


    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getMainContent() {
        return mainContent;
    }

    public void setMainContent(String mainContent) {
        this.mainContent = mainContent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public byte[] getImageArray() {
        return imageArray;
    }

    public void setImageArray(byte[] imageArray) {
        this.imageArray = imageArray;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @NonNull
    @Override
    public String toString() {
        return "Journal{" +
                "heading='" + heading + '\'' +
                ", mainContent='" + mainContent + '\'' +
                ", date='" + date + '\'' +
                ", mood='" + mood + '\'' +
                ", imageArray=" + Arrays.toString(imageArray) +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
