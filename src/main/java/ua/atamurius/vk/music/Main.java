package ua.atamurius.vk.music;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.atamurius.vk.music.json.JsonItem;
import ua.atamurius.vk.music.mp3.Mp3;
import ua.atamurius.vk.music.ui.FetchListWizard;
import ua.atamurius.vk.music.ui.MainFrame;
import ua.atamurius.vk.music.ui.PageFileChooser;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static ua.atamurius.vk.music.Records.Status.ERROR;
import static ua.atamurius.vk.music.Records.Status.SUCCESS;
import static ua.atamurius.vk.music.ui.MainFrame.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new Main().createUI();
        }
        catch (Exception e) {
            log.error("Cannot start app", e);
            showMessageDialog(null, "Cannot start: " + e.getLocalizedMessage());
        }
    }

    private Downloader downloader = new Downloader();

    private final I18n l = new I18n(getClass());

    private MainFrame frame;
    private FetchListWizard wizard;
    private PageFileChooser chooser;
    private MusicPageParser pageParser = new MusicPageParser();
    private Records records = new Records();

    private void createUI() {
        invokeLater(new Runnable() {
            public void run() {
                frame = new MainFrame(records, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        switch (event.getActionCommand()) {
                            case ACTION_EXTRACT_LINKS:
                                File file = chooser.choose(frame.getRootFrame());
                                if (file != null)
                                    loadLinks(file);
                                break;
                            case ACTION_WINDOW_CLOSING:
                                downloader.cancel();
                                frame.close();
                                System.exit(0);
                                break;
                            case ACTION_START_DOWNLOAD:
                                downloadRecords();
                                break;
                            case ACTION_STOP_DOWNLOAD:
                                downloader.cancel();
                                frame.stopProgress();
                                break;
                            case ACTION_EXTRACT_WIZARD:
                                wizard.show();
                                break;
                        }
                    }
                });
                wizard = new FetchListWizard(frame.getRootFrame(), new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        readRecordsFromJson(wizard.getInputData());
                        wizard.hide();
                    }
                });
                chooser = new PageFileChooser();
                frame.show();
            }
        });
    }

    private void readRecordsFromJson(String json) {
        final JsonItem[] items = JsonItem.parse(json);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                for (JsonItem item : items) {
                    log.debug("Item found: {}", item);
                    records.add(item.getUrl(), item.getArtist(), item.getTitle());
                }
                records.notifyObservers();
            }
        });
    }

    private void downloadRecords() {
        frame.startProgress(records.size());
        final AtomicInteger successful = new AtomicInteger();
        File destination = frame.getDestination();
        for (final Records.Item item : records) {
            item.setStatus(Records.Status.WAITING);
            final File file = new File(destination, item.getFileName());
            downloader.download(item.getUrl(), file,
                    new Downloader.ProgressListener() {
                        int retries = 5;
                        public void progress(final long current, final long total) {
                            invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    item.setStatus(Records.Status.IN_PROGRESS);
                                    item.setProgress((int) (current * 100 / total));
                                    records.notifyObservers();
                                }
                            });
                        }
                        public void success() {
                            invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    item.setStatus(SUCCESS);
                                    records.notifyObservers();
                                    frame.setProgress(successful.incrementAndGet());
                                }
                            });
                            writeMetaData(file, item);
                        }
                        public boolean failed() {
                            invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    item.setStatus(ERROR);
                                    records.notifyObservers();
                                }
                            });
                            if (retries-- == 0) {
                                invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        frame.setProgress(successful.incrementAndGet());
                                    }
                                });
                                return false;
                            }
                            else {
                                return true;
                            }
                        }
                    });
        }
    }

    private void writeMetaData(File file, Records.Item item) {
        try {
            Mp3 mp3 = new Mp3(file);
            mp3.setTitle(item.getTitle());
            mp3.setArtist(item.getAuthor());
            if (item.getAlbum() != null) {
                mp3.setAlbum(item.getAlbum());
            }
            mp3.close();
        }
        catch (Exception e) {
            log.error("Cannot write tag to {}", file, e);
        }
    }

    private void loadLinks(final File file) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    pageParser.parse(file, new MusicPageParser.ItemConsumer() {
                        @Override
                        public void consume(String url, String author, String title) {
                            records.add(url, author, title);
                        }
                    });
                    records.notifyObservers();
                } catch (IOException e) {
                    showMessageDialog(
                            frame.getRootFrame(),
                            e.getLocalizedMessage(),
                            l.l("frame.error.file_loading_failed"),
                            ERROR_MESSAGE);
                }
            }
        });
    }

}
