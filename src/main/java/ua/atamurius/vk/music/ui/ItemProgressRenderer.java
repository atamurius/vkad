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

        String text = null;
        Records.Item item = (Records.Item) value;
        switch (item.getStatus()) {
        case IN_PROGRESS:
            progress.setValue(item.getProgress());
            return progress;
        case ERROR:
            text = "table.download.error";
            break;
        case SUCCESS:
            text = "table.download.success";
            break;
        case WAITING:
            text = "table.download.not_started";
            break;
        }
        return super.getTableCellRendererComponent(
                table,
                l.l(text),
                isSelected,
                hasFocus,
                row,
                column);

    }
}
