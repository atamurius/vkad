package ua.atamurius.vk.music;

import ua.atamurius.vk.music.ui.MainFrame;
import ua.atamurius.vk.music.ui.PageFileChooser;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static ua.atamurius.vk.music.ui.MainFrame.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;

public class Main {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new Main().createUI();
    }

    private Downloader downloader = new Downloader();

    private final I18n l = new I18n(getClass());

    private MainFrame frame;
    private PageFileChooser chooser;
    private MusicPageParser pageParser = new MusicPageParser();
    private Records records = new Records();

    private void createUI() {
        SwingUtilities.invokeLater(new Runnable() {
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
                                frame.close();
                                System.exit(0);
                                break;
                            case ACTION_START_DOWNLOAD:
                                downloadRecords();
                                break;
                            case ACTION_STOP_DOWNLOAD:
                                downloader.cancel();
                                break;
                        }
                    }
                });
                chooser = new PageFileChooser();
                frame.show();
            }
        });
    }

    private void downloadRecords() {
        frame.setInProgress(true);
        File destination = frame.getDestination();
        for (final Records.Item item : records) {
            downloader.download(
                    item.getUrl(),
                    new File(destination, item.getFileName()),
                    new Downloader.ProgressListener() {
                        @Override
                        public void progressChanged(long current, long total) {
                            item.setProgress((int)(current * 100 / total));
                            records.notifyObservers();
                        }

                        @Override
                        public void finished(boolean success) {
                            item.setProgress(success ? 100 : null);
                            records.notifyObservers();

                            frame.increaseProgress();
                        }
                    });
        }
    }

    private void loadLinks(File file) {
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

}
