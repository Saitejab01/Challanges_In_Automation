package kafka;

import com.jcraft.jsch.*;
import java.io.*;

public class JSchConnect {
    public static void main(String[] args) {
        String host = "49.249.29.5";      
        String user = "chidori";         
        String password = "@coe$rv!@#"; 

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            // WARNING: disables host key checking
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            System.out.println("Connecting to " + host + "...");
            session.connect(10000); // 10 seconds timeout

            if (session.isConnected()) {
                System.out.println("✅ Successfully connected to " + host + " as " + user);
            }

            // Optionally run a command
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("whoami");
            channel.setInputStream(null);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            channel.setOutputStream(output);
            channel.connect();

            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            System.out.println("Remote output: " + output.toString());

            channel.disconnect();
            session.disconnect();
            System.out.println("🔒 Disconnected from server.");
        } catch (JSchException | InterruptedException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
    }
}

