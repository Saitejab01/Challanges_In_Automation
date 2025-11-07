package kafka.jsonFiles;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class KafkaJson {
	static Random r = new Random();
	static int randNum = r.nextInt(900, 1000);
    public static void main(String[] args) throws Exception {
    	int randNum2 = r.nextInt(900, 10000);
        // === PLACEHOLDER REPLACEMENTS ===
        String transactionId = "TR20250926051906"+randNum;
        System.out.println("Transaction ID : "+transactionId);
        String amount = ""+randNum2;

        // === CONNECTION CONFIGURATION ===
        int port = 22;
        String host = "49.249.29.5";
        String username = "chidori";
        String password = "@coe$rv!@#";

        // === PATHS ===
        String templateFilePath = "C:\\Users\\User\\Downloads\\JsonFile_FlinkPay.txt";  // file with placeholders
        String remoteUploadDir = "/home/chidori/Flink_Pay/swift_files/";
        String remoteScriptPath = "/home/chidori/Flink_Pay/kafka/bin/pushTxn.sh";

        // === STEP 1: Replace placeholders variables and create processed file ===
        System.out.println("*************Replace placeholders variables and create processed file*************");
        String processedFilePath = createFileWithReplacements(templateFilePath, transactionId, amount);
        System.out.println();

        // === STEP 2: Upload processed file with a unique name ===
        System.out.println("*************Upload processed file with a unique name*************");
        String remoteFileName = uploadFileToLinux(processedFilePath, host, port, username, password, remoteUploadDir);
        System.out.println();

        // === STEP 3: Run the shell script with the remote file ===
        System.out.println("*************Run the shell script with the remote file*************");
        String remoteFilePath = remoteUploadDir + "/" + remoteFileName;
        runRemoteScript(host, port, username, password, remoteScriptPath, remoteFilePath);
         new File(processedFilePath).delete();
         //=== STEP 4: UI validation
         System.out.println();
         System.out.println("*******************UI validation*******************");
         validation(transactionId);
         System.out.println();
          
    }

    // STEP 1: Replace placeholders in the template and save a new file
    private static String createFileWithReplacements(String templatePath, String transactionId, String amount) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(templatePath)));

        // Replace placeholders
        content = content.replaceAll("__TRANSACTION_ID__", transactionId);
        content = content.replaceAll("__AMOUNT__", amount);

        // Create a new local file with a unique name
        String fileName = "ATM_TRXN_" + transactionId + ".txt";
        String outputPath = "C:\\Users\\User\\Desktop\\paymentTrxn\\" + fileName;

        // Write the processed content to new file
        Files.write(Paths.get(outputPath), content.getBytes());

        System.out.println(outputPath);
        return outputPath;
    }

    // STEP 2: Upload file via SFTP and rename it on the remote server to include a timestamp
    private static String uploadFileToLinux(String localFilePath, String host, int port,
                                            String username, String password, String remoteDir) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect(10000); // 10 seconds timeout

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        File localFile = new File(localFilePath);
        // === filename for remote file ===
        String remoteFileName = localFile.getName();

        try (InputStream input = new FileInputStream(localFile)) {
        	sftp.cd(remoteDir);
            // Upload the file with the new remote name
            sftp.put(input, remoteFileName);
            System.out.println("Uploaded file to: " + remoteDir + "/" + remoteFileName);
        } finally {
            sftp.exit();
            session.disconnect();
        }

        return remoteFileName;
    }

    // STEP 3: SSH into server and run the Kafka push script
    private static void runRemoteScript(String host, int port, String username, String password,
                                        String scriptPath, String filePathArg) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect(10000);
        //command creation---
        String command = scriptPath + " " + filePathArg;

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        //command execution---
        channel.setCommand(command);
        InputStream in = channel.getInputStream();

        channel.connect();

        System.out.println("Executing this command: " + command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        // reading output of cmd in linux and printing it in the console
        while ((line = reader.readLine()) != null) {
            System.out.println("[remote] " + line);
        }

        // Wait until the channel is done
        int exitStatus = channel.getExitStatus();
        //checking weather the cmd is active or not
        while (channel.isConnected()) {
            Thread.sleep(100);
        }

        channel.disconnect();
        session.disconnect();

        if (exitStatus == 0) {
            System.out.println("Script executed successfully.");
        } else {
            System.err.println("Script failed and status code : " + exitStatus);
        }
    }
    public static void validation(String transactionId) {
		WebDriver driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.manage().window().maximize();
		driver.get("http://49.249.29.5:8091/");
		driver.findElement(By.id("username")).sendKeys("rmgyantra");
		driver.findElement(By.id("inputPassword")).sendKeys("rmgy@9999");
		driver.findElement(By.xpath("//button[.=' Login']")).click();
		driver.findElement(By.xpath("//a[.=' All Transactions']")).click();
		driver.findElement(By.xpath("//input[@placeholder=\"Search by Transaction Id\"]")).sendKeys(transactionId);
		String TrxnID = driver.findElement(By.xpath("//td[contains(.,'TR')]")).getText();
		if (transactionId.equals(TrxnID)) {
			System.out.println("Same Trxn ID is present on UI");
		}else {
			System.out.println("The "+transactionId+" is not same in Flink_Pay");
		}
		driver.quit();
	}
}
