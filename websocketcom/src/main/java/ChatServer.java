import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class ChatServer {
    private static final int PORT = 1500;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> usernames = Collections.synchronizedSet(new HashSet<>());

    // 🔐 Groupes
    private static final Map<String, Set<ClientHandler>> groups = new HashMap<>();
    private static final Map<ClientHandler, String> clientGroupMap = new HashMap<>();

    public static void main(String[] args) {
        log("🟢 Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            log("🔴 Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                out.println("❌ Invalid username.");
                socket.close();
                return;
            }

            synchronized (usernames) {
                if (usernames.contains(username)) {
                    out.println("❌ Username not available.");
                    socket.close();
                    return;
                } else {
                    usernames.add(username);
                }
            }

            ClientHandler handler = new ClientHandler(socket, username, in, out);
            clients.add(handler);
            log("✅ " + username + " connected.");
            broadcast("🔵 " + username + " has joined the chat.");
            handler.run();

        } catch (IOException e) {
            log("❌ Error handling client: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }

    private static void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] " + message);
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private final String username;

        public ClientHandler(Socket socket, String username, BufferedReader in, PrintWriter out) {
            this.socket = socket;
            this.username = username;
            this.in = in;
            this.out = out;
        }

        public void run() {
            try {
                out.println("✅ Connected as " + username);
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equalsIgnoreCase("/logout")) {
                        break;
                    } else if (msg.equalsIgnoreCase("/online")) {
                        sendOnlineUsers();
                    } else if (msg.startsWith("@")) {
                        sendPrivateMessage(msg);
                    } else if (msg.startsWith("/group create ")) {
                        String group = msg.substring(14).trim();
                        if (group.isEmpty()) {
                            out.println("❌ Nom de groupe invalide.");
                        } else if (groups.containsKey(group)) {
                            out.println("⚠️ Le groupe '" + group + "' existe déjà.");
                        } else {
                            groups.put(group, new HashSet<>());
                            out.println("✅ Groupe '" + group + "' créé.");
                        }
                    } else if (msg.startsWith("/group join ")) {
                        String group = msg.substring(12).trim();
                        if (!groups.containsKey(group)) {
                            out.println("❌ Groupe '" + group + "' introuvable.");
                        } else {
                            groups.get(group).add(this);
                            clientGroupMap.put(this, group);
                            out.println("✅ Rejoint le groupe '" + group + "'.");
                        }
                    } else if (msg.equals("/group leave")) {
                        String group = clientGroupMap.remove(this);
                        if (group != null && groups.containsKey(group)) {
                            groups.get(group).remove(this);
                            out.println("🚪 Quitte le groupe '" + group + "'.");
                        } else {
                            out.println("❌ Vous n'êtes dans aucun groupe.");
                        }
                    } else if (msg.equals("/group list")) {
                        if (groups.isEmpty()) {
                            out.println("📂 Aucun groupe disponible.");
                        } else {
                            out.println("📂 Groupes disponibles: " + String.join(", ", groups.keySet()));
                        }
                    } else if (msg.startsWith("/group msg ")) {
                        String content = msg.substring(11).trim();
                        String group = clientGroupMap.get(this);
                        if (group != null && groups.containsKey(group)) {
                            for (ClientHandler member : groups.get(group)) {
                                member.out.println("[Groupe " + group + "] " + username + ": " + content);
                            }
                        } else {
                            out.println("❌ Vous n'êtes dans aucun groupe.");
                        }
                    } else {
                        ChatServer.broadcast("[" + username + "]: " + msg);
                    }
                }
            } catch (IOException e) {
                log("⚠️ Connection lost with " + username);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}

                clients.remove(this);
                usernames.remove(username);

                String group = clientGroupMap.remove(this);
                if (group != null && groups.containsKey(group)) {
                    groups.get(group).remove(this);
                }

                log("🔴 " + username + " disconnected.");
                ChatServer.broadcast("🔻 " + username + " has left the chat.");
            }
        }

        private void sendOnlineUsers() {
            StringBuilder sb = new StringBuilder("👥 Online users: ");
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    sb.append(client.username).append(" ");
                }
            }
            out.println(sb.toString().trim());
        }

        private void sendPrivateMessage(String msg) {
            int spaceIdx = msg.indexOf(' ');
            if (spaceIdx == -1) {
                out.println("❗ Invalid private message format.");
                return;
            }

            String targetUser = msg.substring(1, spaceIdx);
            String privateMsg = msg.substring(spaceIdx + 1);
            boolean found = false;

            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client.username.equalsIgnoreCase(targetUser)) {
                        client.out.println("📩 [Private from " + username + "]> " + privateMsg);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                out.println("❌ User '" + targetUser + "' not found.");
            }
        }
    }
}
