package ua.atamurius.vk.music.json;

import com.google.gson.Gson;

public class JsonItem {

    private String url;
    private String artist;
    private String title;

    public static JsonItem[] parse(String json) {
        return new Gson().fromJson(json, JsonItem[].class);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "{" +
                "url: '" + url + '\'' +
                ", artist: '" + artist + '\'' +
                ", title: '" + title + '\'' +
                '}';
    }
}
