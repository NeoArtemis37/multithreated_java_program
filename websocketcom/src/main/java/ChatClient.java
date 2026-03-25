import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame {
    private JTextField usernameField, serverField, portField, messageField;
    private JTextArea chatArea;
    private JButton connectButton, sendButton, groupButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ChatClient() {
        setTitle("Neon Chat Client");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0x0D0D0D));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createChatPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        add(createSidebarPanel(), BorderLayout.EAST);

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 10));
        panel.setBackground(new Color(0x0D0D0D));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameField = createNeonTextField("Username");
        serverField = createNeonTextField("localhost");
        portField = createNeonTextField("1500");
        connectButton = createNeonButton("Connect");

        connectButton.addActionListener(e -> connectToServer());

        panel.add(usernameField);
        panel.add(serverField);
        panel.add(portField);
        panel.add(connectButton);
        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x0D0D0D));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(0x1A1A1A));
        chatArea.setForeground(new Color(0xE0E0E0));
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        chatArea.setBorder(new RoundedBorder(10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x0D0D0D));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messageField = createNeonTextField("");
        sendButton = createNeonButton("Send");

        sendButton.addActionListener(e -> {
            String msg = messageField.getText().trim();
            if (!msg.isEmpty() && out != null) {
                out.println(msg);
                messageField.setText("");
            }
        });

        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(0x0D0D0D));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        sidebar.add(createHelpPanel(), BorderLayout.NORTH);
        sidebar.add(createUserListPanel(), BorderLayout.CENTER);

        return sidebar;
    }

    private JPanel createHelpPanel() {
        JTextArea helpArea = new JTextArea();
        helpArea.setEditable(false);
        helpArea.setBackground(new Color(0x1A1A1A));
        helpArea.setForeground(new Color(0x00FFFF));
        helpArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        helpArea.setBorder(new RoundedBorder(10));
        helpArea.setText(
                "📘 Commandes disponibles:\n" +
                        "• /online → Voir les utilisateurs\n" +
                        "• /logout → Se déconnecter\n" +
                        "• @nom message → Message privé\n" +
                        "• /group create NomDuGroupe → Créer un groupe\n" +
                        "• /group join NomDuGroupe → Rejoindre un groupe\n" +
                        "• /group leave → Quitter le groupe actuel\n" +
                        "• /group list → Voir les groupes disponibles\n" +
                        ". /group msg message  → Envoyer des messages dans le groupe\n"
        );

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x0D0D0D));
        panel.add(helpArea, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUserListPanel() {
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(new Color(0x1A1A1A));
        userList.setForeground(new Color(0x00FFFF));
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setBorder(new RoundedBorder(10));

        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null) {
                        String msg = JOptionPane.showInputDialog("Message privé à " + selectedUser + ":");
                        if (msg != null && !msg.isEmpty() && out != null) {
                            out.println("@" + selectedUser + " " + msg);
                        }
                    }
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x0D0D0D));
        JLabel label = new JLabel("👥 En ligne", JLabel.CENTER);
        label.setForeground(new Color(0x00FFFF));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(userList), BorderLayout.CENTER);
        return panel;
    }

    private void connectToServer() {
        username = usernameField.getText().trim();
        String server = serverField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());

        try {
            socket = new Socket(server, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(username); // send username to server

            new Thread(() -> listenToServer()).start();
            startUserListRefresh();
        } catch (IOException e) {
            handleIncomingMessage("❌ Connection failed: " + e.getMessage());
        }
    }

    private void listenToServer() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                handleIncomingMessage(msg);
                if (msg.startsWith("👥 Online users:")) {
                    updateUserList(msg);
                } else if (msg.contains("has joined the chat")) {
                    String name = extractUsername(msg, "has joined the chat");
                    if (!userListModel.contains(name)) userListModel.addElement(name);
                } else if (msg.contains("has left the chat")) {
                    String name = extractUsername(msg, "has left the chat");
                    userListModel.removeElement(name);
                }
            }
        } catch (IOException e) {
            handleIncomingMessage("⚠️ Disconnected from server.");
        }
    }

   private  void startUserListRefresh() {
        Timer timer = new Timer(60000, e -> {
            if (out != null) out.println("/online");
        });
        timer.setRepeats(true);
        timer.start();
    }

    private void updateUserList(String msg) {
        userListModel.clear();
        String[] parts = msg.replace("👥 Online users:", "").trim().split(" ");
        for (String name : parts) {
            if (!name.isEmpty()) userListModel.addElement(name);
        }
    }

    private String extractUsername(String msg, String marker) {
        int idx = msg.indexOf(marker);
        if (idx > 0) {
            return msg.substring(2, idx).trim(); // skip emoji
        }
        return "";
    }

    private void handleIncomingMessage(String msg) {
        chatArea.append(msg + "\n");
    }

    private JTextField createNeonTextField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setBackground(new Color(0x1A1A1A));
        field.setForeground(new Color(0xE0E0E0));
        field.setCaretColor(Color.CYAN);
        field.setFont(new Font("Consolas", Font.PLAIN, 13));
        field.setBorder(new RoundedBorder(10));
        return field;
    }

    private JButton createNeonButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0x00FFFF));
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0x00FFAA));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0x00FFFF));
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }

    // --- RoundedBorder class ---
    static class RoundedBorder implements Border {
        private int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius + 1, radius + 1, radius + 2, radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x00FFFF));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
