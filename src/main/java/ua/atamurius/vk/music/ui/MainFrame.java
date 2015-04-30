package ua.atamurius.vk.music.ui;

import ua.atamurius.vk.music.I18n;
import ua.atamurius.vk.music.Records;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import static java.awt.BorderLayout.*;
import static java.awt.FlowLayout.LEADING;
import static java.awt.event.KeyEvent.VK_DELETE;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static ua.atamurius.vk.music.Records.Status.IN_PROGRESS;

public class MainFrame {

    public static final String ACTION_EXTRACT_LINKS = "extract_links";
    public static final String ACTION_START_DOWNLOAD = "start_download";
    public static final String ACTION_STOP_DOWNLOAD = "stop_download";
    public static final String ACTION_WINDOW_CLOSING = "window_closing";
    public static final String ACTION_EXTRACT_WIZARD = "extract_wizard";

    private final I18n l = new I18n(getClass());

    private final JFrame root;
    private JButton btnExtract, btnSelectFolder, btnStart, btnStop, btnExtractWzrd;
    private JProgressBar progress;
    private JLabel lblDestination;
    private JTable table;
    private File destination = new File(".");

    @SuppressWarnings("serial")
    public MainFrame(final Records records, final ActionListener dispatcher) {
        root = new JFrame(l.l("frame.title")) {{
            add(new JPanel(new FlowLayout(LEADING)) {{
                add(new JLabel(l.l("frame.destination")) {{
                   setFont(getFont().deriveFont(Font.BOLD));
                }});
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
                }});
                add(btnExtract = new JButton(l.l("frame.button.extract_links")) {{
                    setToolTipText(l.l("frame.button.extract_links.tip"));
                    setActionCommand(ACTION_EXTRACT_LINKS);
                    addActionListener(dispatcher);
                }});
                add(btnExtractWzrd = new JButton(l.l("frame.button.extract_wizard")) {{
                    setActionCommand(ACTION_EXTRACT_WIZARD);
                    addActionListener(dispatcher);
                }});
            }}, PAGE_START);

            table = new JTable();
            {
                table.setModel(new RecordsTableModel(records));
                table.setDefaultRenderer(Records.Item.class, new ItemProgressRenderer());
                table.setGridColor(Color.lightGray);
                table.setIntercellSpacing(new Dimension(7, 3));
                table.setAutoCreateRowSorter(true);
                table.getColumnModel().getColumn(0).setMaxWidth(40);
                ((DefaultRowSorter<?, ?>) table.getRowSorter()).setComparator(4, Records.BY_STATUS_COMPARATOR);
                table.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == VK_DELETE && !e.isConsumed()) {
                            if (table.isEditing()) {
                                table.getCellEditor().cancelCellEditing();
                            }
                            removeSelected();
                            e.consume();
                        }
                    }
                });

                add(new JScrollPane(table));
            }

            add(new JPanel(new BorderLayout()) {{
                add(progress = new JProgressBar());
                add(new JPanel() {{
                    add(btnStart = new JButton(l.l("frame.button.download")) {{
                        setActionCommand(ACTION_START_DOWNLOAD);
                        addActionListener(dispatcher);
                    }});
                    add(btnStop = new JButton(l.l("frame.button.stop")) {{
                        setActionCommand(ACTION_STOP_DOWNLOAD);
                        addActionListener(dispatcher);
                    }});
                }}, LINE_END);
            }}, PAGE_END);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    dispatcher.actionPerformed(new ActionEvent(e, 0, ACTION_WINDOW_CLOSING));
                }
            });
            setSize(1000, 800);
        }};
        setInProgress(false);
    }

    private void removeSelected() {
        int[] selectedRows = table.getSelectedRows();
        int[] inds = new int[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            inds[i] = table.convertRowIndexToModel(selectedRows[i]);
        }
        ((RecordsTableModel) table.getModel()).remove(inds);
    }

    private void setInProgress(boolean inProgress) {
        btnExtract.setEnabled(! inProgress);
        btnSelectFolder.setEnabled(! inProgress);
        btnStart.setEnabled(!inProgress);
        btnExtractWzrd.setEnabled(! inProgress);
        btnStop.setEnabled(inProgress);
        ((RecordsTableModel) table.getModel()).setEditable(!inProgress);
    }

    public void startProgress(int total) {
        if (total > 0) {
            setInProgress(true);
            progress.setMaximum(total);
        }
    }

    public void setProgress(int progress) {
        if (this.progress.getMaximum() <= progress) {
            setInProgress(false);
            this.progress.setValue(0);
        }
        else {
            this.progress.setValue(progress);
        }
    }

    public void show() {
        root.setVisible(true);
    }

    public Window getRootFrame() {
        return root.getOwner();
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

    public void stopProgress() {
        progress.setValue(0);
        progress.setMaximum(0);
        setInProgress(false);
    }
}
