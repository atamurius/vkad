package ua.atamurius.vk.music.ui;

import ua.atamurius.vk.music.Downloader;
import ua.atamurius.vk.music.I18n;
import ua.atamurius.vk.music.MusicPageParser;
import ua.atamurius.vk.music.Records;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
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

    private Downloader queue = new Downloader();

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
                                if (file != null) {
                                    loadLinks(file);
                                }
                                break;
                            case ACTION_START_DOWNLOAD:
                            case ACTION_WINDOW_CLOSING:
                                frame.close();
                                System.exit(0);
                                break;
                            case ACTION_STOP_DOWNLOAD:
                        }
                    }
                });
                chooser = new PageFileChooser();
                frame.show();
            }
        });
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
            JOptionPane.showMessageDialog(
                    frame.getRootFrame(),
                    e.getLocalizedMessage(),
                    l.l("frame.error.file_loading_failed"),
                    ERROR_MESSAGE);
        }
    }

}
