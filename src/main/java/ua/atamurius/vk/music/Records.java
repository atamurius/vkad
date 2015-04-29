package ua.atamurius.vk.music;

import java.io.File;
import java.util.*;

import static java.util.Collections.unmodifiableList;

public class Records extends Observable implements Iterable<Records.Item> {

    public enum Status { WAITING, IN_PROGRESS, ERROR, SUCCESS }

    public class Item {
        private String title;
        private String author;
        private String album;
        private String url;
        private int progress;
        private Status status = Status.WAITING;

        private Item(String title, String author, String url) {
            this.title = title;
            this.author = author;
            this.url = url;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
            setChanged();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
            setChanged();
        }

        public String getUrl() {
            return url;
        }

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
            setChanged();
        }

        public String getFileName() {
            int p = url.lastIndexOf('.');
            int e = url.lastIndexOf('?');
            String ext = (p == -1) ? ".mp3" : url.substring(p, e == -1 ? url.length() : e);
            return (author + " - " + title + ext).replaceAll("[\u0000-\u001F\\*/:<>\\?\\\\|]+", "");
        }

        public int getProgress() {
            return progress;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
            setChanged();
        }

        public void setProgress(int progress) {
            this.progress = progress;
            setChanged();
        }

        @Override
        public String toString() {
            return title +" by "+ author +" ("+ url +")";
        }
    }

    private final List<Item> items = new ArrayList<>();
    private final List<Item> unmodifiableItems = unmodifiableList(items);

    public Item add(String url, String author, String title) {
        Item item = new Item(title, author, url);
        items.add(item);
        setChanged();
        return item;
    }

    @Override
    public Iterator<Item> iterator() {
        return unmodifiableItems.iterator();
    }

    public void remove(int index) {
        items.remove(index);
        setChanged();
    }

    public int size() {
        return items.size();
    }

    public Item get(int index) {
        return items.get(index);
    }

    public void clear() {
        items.clear();
        setChanged();
    }
}
