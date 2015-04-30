package ua.atamurius.vk.music.ui;

import ua.atamurius.vk.music.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.FlowLayout.TRAILING;
import static java.awt.Font.PLAIN;
import static java.util.Objects.requireNonNull;

public class FetchListWizard {

    public static final String ACTION_JSON_COPIED = "json_copied";

    private final I18n l = new I18n(FetchListWizard.class);
    private final JDialog root;
    private final ActionListener listener;

    private boolean showingJS = true;
    private JLabel message;
    private JTextArea text;

    public FetchListWizard(final Window owner, ActionListener listener) {
        this.listener = requireNonNull(listener);
        root = new JDialog(owner, l.l("wizard.title"), APPLICATION_MODAL) {{
            setLayout(new GridBagLayout());
            final JRootPane rootPane = getRootPane();
            add(message = new JLabel(), constraints(0, 0, 5));
            add(new JScrollPane(text = new JTextArea() {{
                setFont(new Font("monospace", PLAIN, getFont().getSize()));
                setLineWrap(true);
            }}), constraints(0, 1, 15));
            add(new JPanel(new FlowLayout(TRAILING)) {{
                add(new JButton(l.l("wizard.cancel")) {{
                    addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            root.setVisible(false);
                        }
                    });
                }});
                add(new JButton(l.l("wizard.proceed")) {{
                    rootPane.setDefaultButton(this);
                    addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            nextStep();
                        }
                    });
                }});
            }}, constraints(0, 2, 0.1));
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            setSize(640, 480);
            setLocationRelativeTo(owner);
        }};
    }

    private GridBagConstraints constraints(int x, int y, double weight) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = weight;
        c.weightx = 1;
        c.insets = new Insets(3,5,3,5);
        return c;
    }

    private void nextStep() {
        if (showingJS) {
            message.setText(l.l("wizard.copy_json_message"));
            text.setEditable(true);
            text.setText("");
            showingJS = false;
        }
        else {
            listener.actionPerformed(new ActionEvent(this, 0, ACTION_JSON_COPIED));
        }
    }

    public void show() {
        showingJS = true;
        message.setText(l.l("wizard.execute_js_message"));
        text.setEditable(false);
        text.setText(getResource("/extractor.js"));
        root.setVisible(true);
    }

    private String getResource(String name) {
        try {
            try (InputStream in = getClass().getResourceAsStream(name)) {
                StringBuilder sb = new StringBuilder();
                byte[] buff = new byte[1024];
                int l;
                while ((l = in.read(buff)) != -1) {
                    sb.append(new String(buff, 0, l));
                }
                return sb.toString();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Cannot read "+ name, e);
        }
    }

    public String getInputData() {
        return text.getText();
    }

    public void hide() {
        root.setVisible(false);
    }
}
