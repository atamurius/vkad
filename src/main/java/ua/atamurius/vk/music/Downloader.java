package ua.atamurius.vk.music;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import static java.lang.Thread.currentThread;

public class Downloader {

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);

    public static final int BUFFER_SIZE = 256 * 1024;

    public interface ProgressListener {
        void progress(long current, long total);
        void success();
        boolean failed();
    }

    private ExecutorService executor;

    private static int threadCount() {
        String threads = System.getProperty(Downloader.class.getName() +".threads");
        if (threads != null) {
            try {
                return Integer.parseInt(threads);
            }
            catch (NumberFormatException e) {
                log.error("Wrong threads number {}", threads);
            }
        }
        return 10; // by default
    }

    private class Task implements Runnable {

        final String url;
        final File file;
        final ProgressListener listener;

        public Task(String url, File file, ProgressListener listener) {
            this.url = url;
            this.file = file;
            this.listener = listener;
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                log.debug("download started: {} -> {}", url, file);
                success = download();
                if (! success && file.exists()) {
                    file.delete();
                }
                log.debug("download {} {}", file.getName(), success ? "successful" : "broken");
            }
            catch (Exception e) {
                log.error("download {} failed", file.getName(), e);
            }
            if (success) {
                listener.success();
            }
            else {
                if (listener.failed()) {
                    executor.submit(this);
                }
            }
        }

        boolean download() throws IOException {
            long total;
            long finished = 0;
            URLConnection conn = new URL(url).openConnection();
            try (InputStream input = conn.getInputStream();
                 OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {

                total = conn.getContentLengthLong();
                listener.progress(finished, total);

                int lastRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while (!currentThread().isInterrupted() && (lastRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, lastRead);
                    finished += lastRead;
                    log.trace("{}: {} bytes read ({}/{})", file.getName(), lastRead, finished, total);
                    listener.progress(finished, total);
                }
            }
            return (finished == total && total > 0);
        }
    }

    public void download(final String srcUrl, final File dstFile, final ProgressListener listener) {
        if (executor == null || executor.isShutdown()) {
           executor = Executors.newFixedThreadPool(threadCount());
        }
        executor.submit(new Task(srcUrl, dstFile, listener));
    }

    public void cancel() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
