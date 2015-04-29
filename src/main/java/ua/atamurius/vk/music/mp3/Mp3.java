package ua.atamurius.vk.music.mp3;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static java.lang.Math.min;

public class Mp3 {

    private final RandomAccessFile file;

    private static final int TAG_SIZE = 128;
    private static final byte[] TAG_SIGN = { 'T', 'A', 'G' };

    private enum Fields {
        TITLE(30), ARTIST(30), ALBUM(30);
        final int size;

        private Fields(int size) {
            this.size = size;
        }

        public int offset() {
            if (ordinal() == 0) {
                return TAG_SIGN.length;
            }
            else {
                Fields prev = values()[ordinal() - 1];
                return prev.offset() + prev.size;
            }
        }
    }

    public Mp3(File file) throws IOException {
        this.file = new RandomAccessFile(file, "rw");
        if (! hasID3v1()) {
            addID3v1();
        }
    }

    public boolean hasID3v1() throws IOException {
        if (file.length() < TAG_SIZE) {
            return false;
        }
        file.seek(file.length() - TAG_SIZE);
        for (int i = 0; i < TAG_SIGN.length; i++) {
            if (file.readByte() != TAG_SIGN[i])
                return false;
        }
        return true;
    }

    private void addID3v1() throws IOException {
        file.seek(file.length());
        file.write(TAG_SIGN);
        for (int i = TAG_SIGN.length; i < TAG_SIZE; i++) {
            file.writeByte(0);
        }
    }

    private void writeString(String value, Fields field) throws IOException {
        file.seek(file.length() - TAG_SIZE + field.offset());
        byte[] bytes = value.getBytes();
        file.write(bytes, 0, min(bytes.length, field.size));
        for (int i = bytes.length; i < field.size; i++) {
            file.writeByte(0);
        }
    }

    private String readString(Fields field) throws IOException {
        file.seek(file.length() - TAG_SIZE + field.offset());
        byte[] bytes = new byte[field.size];
        file.read(bytes);
        return new String(bytes);
    }

    public void setTitle(String title) throws IOException {
        writeString(title, Fields.TITLE);
    }

    public void setArtist(String artist) throws IOException {
        writeString(artist, Fields.ARTIST);
    }

    public void setAlbum(String album) throws IOException {
        writeString(album, Fields.ALBUM);
    }

    public void close() throws IOException {
        file.close();
    }
}














