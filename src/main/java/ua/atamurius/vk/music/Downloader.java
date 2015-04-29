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
        void progressChanged(long current, long total);
        void finished(boolean success);
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Collection<Future<?>> tasks = new ConcurrentLinkedQueue<>();

    private final AtomicInteger tasksFinished = new AtomicInteger();

    public void reset() {
        tasksFinished.set(0);
    }

    public void download(final String srcUrl, final File dstFile, final ProgressListener listener) {
        tasks.add(executor.submit(new Runnable() {
            @Override
            public void run() {
                long total = 0;
                long finished = 0;
                try {
                    log.debug("URL: {}", srcUrl);
                    URLConnection conn = new URL(srcUrl).openConnection();
                    try (InputStream input = conn.getInputStream();
                         OutputStream output = new BufferedOutputStream(new FileOutputStream(dstFile))) {

                        total = conn.getContentLengthLong();
                        listener.progressChanged(finished, total);

                        log.debug("download '{}' started", dstFile.getName());
                        int lastRead;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while (!currentThread().isInterrupted() && (lastRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, lastRead);
                            finished += lastRead;
                            log.trace("{}: {} bytes read ({}/{})", dstFile.getName(), lastRead, finished, total);
                            listener.progressChanged(finished, total);
                        }
                    }
                    finally {
                        if ((finished != total || total == 0) && dstFile.exists()) {
                            log.error("Unsuccessful download, deleting file {}", dstFile);
                            dstFile.delete();
                        }
                    }
                } catch (Exception e) {
                    log.error("download '{}' failed", dstFile.getName(), e);
                }
                log.debug("download '{}' finished at {}/{}", dstFile.getName(), finished, total);
                tasksFinished.incrementAndGet();
                listener.finished(finished == total && total > 0);
            }
        }));
    }

    public int getActiveTaskCount() {
        int count = 0;
        for (Iterator<Future<?>> futures = tasks.iterator(); futures.hasNext(); ) {
            if (futures.next().isDone()) {
                futures.remove();
            }
            else {
                count++;
            }
        }
        return count;
    }

    public void cancel() {
        for (Future<?> future: tasks) {
            future.cancel(true);
        }
        int active;
        do {
            Thread.yield();
            active = getActiveTaskCount();
            log.debug("Active tasks: {}", active);
        }
        while (active > 0);
    }

    public int getFinishedTasksCount() {
        return tasksFinished.get();
    }
}
