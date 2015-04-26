package ua.atamurius.vk.music;

import java.util.*;

import static java.util.Collections.unmodifiableList;

public class Records extends Observable implements Iterable<Records.Item> {

    public class Item {
        private String title;
        private String author;
        private String url;

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

        public String getFileName() {
            int p = url.lastIndexOf('.');
            String ext = (p == -1) ? ".mp3" : url.substring(p);
            return (author + " - " + title + ext).replaceAll("[\u0000-\u001F\\*/:<>\\?\\\\|]+", "");
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
