package kafka;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class SftpUploader {

    public static void upload(String username, String password, String host, int port,
                              String localFilePath, String remoteDir) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);

        session.setPassword(password);

        // Avoid "UnknownHostKey" issues in quick examples — in production, manage host keys properly
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect(10000); // 10s timeout
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        try (InputStream input = new FileInputStream(localFilePath)) {
            // Ensure remoteDir exists on server, or create (simple attempt)
            try {
                sftp.cd(remoteDir);
            } catch (SftpException e) {
                // try to create the directory (single-level). For nested paths, you'd iterate.
                sftp.mkdir(remoteDir);
                sftp.cd(remoteDir);
            }

            String fileName = localFilePath.substring(localFilePath.lastIndexOf(System.getProperty("file.separator")) + 1);
            sftp.put(input, fileName);
            System.out.println("Uploaded " + localFilePath + " to " + host + ":" + remoteDir + "/" + fileName);
        } finally {
            sftp.exit();
            session.disconnect();
        }
    }
    public static void main(String[] args) throws Exception {
        String username = "chidori";
        String password = "@coe$rv!@#";
        String host = "49.249.29.5"; // your Linux box IP
        int port = 22;
        String localFilePath = "C:\\Users\\User\\Desktop\\ATM_TRXN_998.txt";
        String remoteDir = "/home/chidori/Flink_Pay/swift_files/";

        upload(username, password, host, port, localFilePath, remoteDir);
    }
}
