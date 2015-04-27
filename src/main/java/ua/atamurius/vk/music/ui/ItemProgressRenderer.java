package ua.atamurius.vk.music.ui;

import ua.atamurius.vk.music.I18n;
import ua.atamurius.vk.music.Records;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ItemProgressRenderer extends DefaultTableCellRenderer {

    private final I18n l = new I18n(MainFrame.class);

    private JProgressBar progress = new JProgressBar(0,100);

    public ItemProgressRenderer() {
        setOpaque(true);
        setForeground(Color.gray);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        Integer status = ((Records.Item) value).getProgress();
        if (status == null || status == -1) {
            return super.getTableCellRendererComponent(
                    table,
                    l.l(status == null ? "table.download.not_started" : "table.download.error"),
                    isSelected,
                    hasFocus,
                    row,
                    column);
        }
        else {
            progress.setValue(status);
            return progress;
        }
    }
}
