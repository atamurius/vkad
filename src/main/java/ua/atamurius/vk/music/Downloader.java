package ua.atamurius.vk.music;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import static java.lang.Thread.currentThread;

public class Downloader {

    public static final int BUFFER_SIZE = 1024 * 1024;

    public interface ProgressListener {
        void progressChanged(long current, long total);
        void finished(boolean success);
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Collection<Future<?>> tasks = new ConcurrentSkipListSet<>();

    public void download(final String srcUrl, final File dstFile, final ProgressListener listener) {
        tasks.add(executor.submit(new Runnable() {
            @Override
            public void run() {
                long total = 0;
                long finished = 0;
                try {
                    URLConnection conn = new URL(srcUrl).openConnection();
                    total = conn.getContentLengthLong();
                    listener.progressChanged(finished, total);
                    try (InputStream input = conn.getInputStream();
                         OutputStream output = new BufferedOutputStream(new FileOutputStream(dstFile))) {

                        System.out.printf("download '%s' started%n", dstFile.getName());
                        int lastRead;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while (!currentThread().isInterrupted() && (lastRead = input.read(buffer)) > 0) {
                            output.write(buffer, 0, lastRead);
                            finished += lastRead;
                            listener.progressChanged(finished, total);
                        }
                    } finally {
                        if (finished < total && dstFile.exists()) {
                            dstFile.delete();
                        }
                    }
                } catch (Exception e) {
                    System.err.printf("download '%s' failed%n", dstFile.getName());
                    e.printStackTrace();
                }
                System.out.printf("download '%s' finished at %d/%d%n", dstFile.getName(), finished, total);
                listener.finished(finished == total);
            }
        }));
    }

    public int getActiveTaskCount() {
        for (Iterator<Future<?>> futures = tasks.iterator(); futures.hasNext(); ) {
            if (futures.next().isDone()) {
                futures.remove();
            }
        }
        return tasks.size();
    }

    public void cancel() {
        for (Future<?> future: tasks) {
            future.cancel(true);
        }
        int active;
        do {
            Thread.yield();
            active = getActiveTaskCount();
            System.out.printf("Active tasks: %d%n", active);
        }
        while (active > 0);
    }
}
