package ua.atamurius.vk.music.ui;

import ua.atamurius.vk.music.I18n;
import ua.atamurius.vk.music.Records;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static java.awt.BorderLayout.*;
import static java.awt.FlowLayout.LEADING;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

public class MainFrame {

    public static final String ACTION_EXTRACT_LINKS = "extract_links";
    public static final String ACTION_START_DOWNLOAD = "start_download";
    public static final String ACTION_STOP_DOWNLOAD = "stop_download";
    public static final String ACTION_WINDOW_CLOSING = "window_closing";

    private final I18n l = new I18n(getClass());

    private final JFrame root;
    private JButton btnExtract, btnSelectFolder, btnClearTable, btnRemoveSelected, btnStart, btnStop;
    private JProgressBar progress;
    private JLabel lblDestination;
    private JTable table;
    private File destination = new File(".");

    @SuppressWarnings("serial")
    public MainFrame(final Records records, final ActionListener dispatcher) {
        root = new JFrame(l.l("frame.title")) {{
            add(new JPanel(new GridLayout(2,1)) {{
                add(new JPanel(new FlowLayout(LEADING)) {{
                    add(new JLabel(l.l("frame.destination")) {{
                       setFont(getFont().deriveFont(Font.BOLD));
                    }},
                    LINE_START);
                    add(lblDestination = new JLabel(destination.getAbsolutePath()) {{
                        setFont(new Font("monospace", Font.PLAIN, getFont().getSize()));
                    }});
                    add(btnSelectFolder = new JButton(l.l("frame.button.select_folder")) {{
                        setToolTipText(l.l("frame.button.select_folder.tip"));
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                                selectFolder();
                            }
                        });
                    }},
                    LINE_END);
                }});
                add(new JPanel(new FlowLayout(LEADING)) {{
                   add(btnExtract = new JButton(l.l("frame.button.extract_links")) {{
                       setToolTipText(l.l("frame.button.extract_links.tip"));
                       setActionCommand(ACTION_EXTRACT_LINKS);
                       addActionListener(dispatcher);
                   }});
                   add(btnClearTable = new JButton(l.l("frame.button.clear_table")) {{
                       addActionListener(new ActionListener() {
                           @Override
                           public void actionPerformed(ActionEvent e) {
                               records.clear();
                               records.notifyObservers();
                           }
                       });
                   }});
                   add(btnRemoveSelected = new JButton(l.l("frame.button.remove_selected")) {{
                       setToolTipText(l.l("frame.button.remove_selected.tip"));
                       addActionListener(new ActionListener() {
                           @Override
                           public void actionPerformed(ActionEvent e) {
                               removeSelected();
                           }
                       });
                   }});
                   add(btnStart = new JButton(l.l("frame.button.download")) {{
                       setActionCommand(ACTION_START_DOWNLOAD);
                       addActionListener(dispatcher);
                   }});
                   add(btnStop = new JButton(l.l("frame.button.stop")) {{
                       setActionCommand(ACTION_STOP_DOWNLOAD);
                       addActionListener(dispatcher);
                   }});
                }});
            }},
            PAGE_START);

            add(new JScrollPane(table = new JTable() {{
                setModel(new RecordsTableModel(records));
                setDefaultRenderer(Records.Item.class, new ItemProgressRenderer());
                setGridColor(Color.lightGray);
                setIntercellSpacing(new Dimension(7,3));
                setAutoCreateRowSorter(true);
                getColumnModel().getColumn(0).setMaxWidth(30);
            }}));

            add(progress = new JProgressBar(0, records.size()), PAGE_END);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    dispatcher.actionPerformed(new ActionEvent(e, 0, ACTION_WINDOW_CLOSING));
                }
            });
            setSize(800, 600);
        }};
        setInProgress(false);
    }

    private void removeSelected() {/*
        int inds =
        for (int row: table.getSelectedRows()) {
            items.add( records table.convertRowIndexToModel(row));
        }
        Arrays.so
        for (int i : selectedRows)
            records.remove(i);
        records.notifyObservers();
*/
    }

    public void setInProgress(boolean inProgress) {
        btnExtract.setEnabled(! inProgress);
        btnSelectFolder.setEnabled(! inProgress);
        btnClearTable.setEnabled(! inProgress);
        btnRemoveSelected.setEnabled(! inProgress);
        btnStart.setEnabled(! inProgress);
        btnStop.setEnabled(inProgress);
        ((RecordsTableModel) table.getModel()).setEditable(! inProgress);
        progress.setValue(0);
    }

    public void show() {
        root.setVisible(true);
    }

    public Component getRootFrame() {
        return root;
    }

    public void close() {
        root.dispose();
    }

    private void selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(DIRECTORIES_ONLY);
        if (APPROVE_OPTION == chooser.showOpenDialog(root)) {
            destination = chooser.getSelectedFile();
            lblDestination.setText(destination.getAbsolutePath());
        }
    }

    public File getDestination() {
        return destination;
    }

    public void increaseProgress() {
        progress.setValue( progress.getValue() + 1 );
        if (progress.getValue() == progress.getMaximum()) {
            setInProgress(false);
        }
    }
}
