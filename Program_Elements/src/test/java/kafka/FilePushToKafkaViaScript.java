package kafka;
import com.jcraft.jsch.*;
import java.io.*;
import java.util.Properties;

public class FilePushToKafkaViaScript {

    public static void main(String[] args) throws Exception {

        // === CONFIGURATION ===        // Linux server IP
        int port = 22;
        String host = "49.249.29.5";      
        String username = "chidori";         
        String password = "@coe$rv!@#"; 

        String localFilePath = "C:\\Users\\User\\Desktop\\ATM_TRXN_997.txt";
        String remoteUploadDir = "/home/chidori/Flink_Pay/swift_files/";
        String remoteScriptPath = "/home/chidori/Flink_Pay/kafka/bin/pushTxn_swift.sh";

        // === STEP 1: Upload file via SFTP ===
        String remoteFileName = uploadFileToLinux(localFilePath, host, port, username, password, remoteUploadDir);

        // === STEP 2: Run push_txn.sh with uploaded file ===
        String remoteFilePath = remoteUploadDir + "/" + remoteFileName;
        runRemoteScript(host, port, username, password, remoteScriptPath, remoteFilePath);
    }

    private static String uploadFileToLinux(String localFilePath, String host, int port,
                                            String username, String password, String remoteDir) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect(10000);
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        File localFile = new File(localFilePath);
        String fileName = localFile.getName();

        try (InputStream input = new FileInputStream(localFile)) {
            try {
                sftp.cd(remoteDir);
            } catch (SftpException e) {
                sftp.mkdir(remoteDir);
                sftp.cd(remoteDir);
            }

            sftp.put(input, fileName);
            System.out.println("✅ Uploaded file to: " + remoteDir + "/" + fileName);
        } finally {
            sftp.exit();
            session.disconnect();
        }

        return fileName;
    }

    private static void runRemoteScript(String host, int port, String username, String password,
                                        String scriptPath, String filePathArg) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect(10000);

        String command = scriptPath + " " + filePathArg;

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setErrStream(System.err);
        InputStream in = channel.getInputStream();

        channel.connect();

        System.out.println("🚀 Executing: " + command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[remote] " + line);
        }

        int exitStatus = channel.getExitStatus();
        while (channel.isConnected()) {
            Thread.sleep(100);
        }

        channel.disconnect();
        session.disconnect();

        if (exitStatus == 0) {
            System.out.println("✅ Script executed successfully.");
        } else {
            System.err.println("❌ Script failed with exit code: " + exitStatus);
        }
    }
}
