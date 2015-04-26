package ua.atamurius.vk.music;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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
    private final Collection<Future<?>> tasks = new ArrayList<>();

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

                        int lastRead;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while (!currentThread().isInterrupted() && (lastRead = input.read(buffer)) > 0) {
                            output.write(buffer, 0, lastRead);
                            finished += lastRead;
                            System.out.printf("downloaded '%s' %d%%%n", dstFile.getName(), finished*100/total);
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
                System.err.printf("download '%s' finished at %d/%d%n", dstFile.getName(), finished, total);
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
        for (Iterator<Future<?>> futures = tasks.iterator(); futures.hasNext(); ) {
            futures.next().cancel(true);
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
