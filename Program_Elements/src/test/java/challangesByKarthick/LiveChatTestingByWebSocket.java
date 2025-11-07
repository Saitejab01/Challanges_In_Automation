package challangesByKarthick;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class LiveChatTestingByWebSocket {
    private static final String SERVER_URL = "https://simplewebchat-st0x.onrender.com/";
    private static final String[] BOT_NAMES = {"Alex", "Riya", "John", "Meera", "Sam"};
    private static final String[] MESSAGES = {
            "Hey everyone 👋",
            "How’s it going?",
            "Nice chat app!",
            "Testing from Java bot 🤖",
            "This looks awesome 😎",
            "I love WebSocket chats!",
            "Hello from bot!"
    };

    private static final String REAL_USER_NAME = "SAITEJA BANDI";

    private static final List<Socket> bots = new ArrayList<>();
    private static ScheduledExecutorService scheduler;
    private static boolean running = false; // will set to true when scheduler starts
    private static Random random = new Random();

    public static void main(String[] args) throws Exception {
        System.out.println("Connecting bots...");

        // Connect bots
        for (String name : BOT_NAMES) {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            Socket socket = IO.socket(new URI(SERVER_URL), options);

            socket.on(Socket.EVENT_CONNECT, args1 -> System.out.println(name + " connected"));

            // Listen to all chat messages
            socket.on("chatMessage", args1 -> {
                JSONObject msg = (JSONObject) args1[0];
                String messageText = msg.getString("message").trim();
                String user = msg.getString("user").trim();

                if (REAL_USER_NAME.equals(user)) {
                    if ("stop".equalsIgnoreCase(messageText)) {
                        stopBots();
                    } else if ("run".equalsIgnoreCase(messageText)) {
                        startBots();
                    } else if ("exit".equalsIgnoreCase(messageText)) {
                        terminateBots();
                    }
                }
            });

            socket.connect();
            bots.add(socket);
            Thread.sleep(500);
        }

        // Start chatting by default
        startBots();

        System.out.println("Bots are running. Type 'stop', 'run', or 'exit' from '" + REAL_USER_NAME + "' in chat.");
    }

    private static synchronized void startBots() {
        if (running) return; // already running
        System.out.println("Starting bots...");
        running = true;

        // Create a new scheduler each time we start
        scheduler = Executors.newScheduledThreadPool(BOT_NAMES.length);

        for (int i = 0; i < BOT_NAMES.length; i++) {
            final Socket botSocket = bots.get(i);
            final String botName = BOT_NAMES[i];

            scheduler.scheduleAtFixedRate(() -> {
                if (!running) return; // extra safety
                JSONObject data = new JSONObject();
                data.put("user", botName);
                data.put("message", MESSAGES[random.nextInt(MESSAGES.length)]);
                botSocket.emit("chatMessage", data);
                System.out.println(botName + " sent: " + data);
            }, 1, 5 + random.nextInt(5), TimeUnit.SECONDS);
        }
    }

    private static synchronized void stopBots() {
        if (!running) return; // already stopped
        System.out.println("Stopping bots...");
        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private static synchronized void terminateBots() {
        System.out.println("Exiting program... disconnecting bots.");
        stopBots();
        bots.forEach(Socket::disconnect);
        System.exit(0);
    }
}
