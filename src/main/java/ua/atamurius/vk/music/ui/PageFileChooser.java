package ua.atamurius.vk.music.ui;

import ua.atamurius.vk.music.I18n;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

import static javax.swing.JFileChooser.APPROVE_OPTION;

public class PageFileChooser {

    private final I18n l = new I18n(getClass());
    private final JFileChooser chooser;

    private final FileFilter filter = new FileFilter() {
        public String getDescription() {
            return l.l("files.html_page");
        }

        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                    || (f.isFile() && (
                            f.getName().endsWith(".html")
                        ||  f.getName().endsWith(".htm")));
        }
    };

    public PageFileChooser() {
        chooser = new JFileChooser();
        chooser.setDialogTitle(l.l("files.title"));
        chooser.setFileFilter(filter);
    }

    public File choose(Component root) {
        if (APPROVE_OPTION == chooser.showOpenDialog(root)) {
            return chooser.getSelectedFile();
        }
        else {
            return null;
        }
    }
}
