package programElements;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSH_Connection {
	public static void main(String[] args) {
		  String user = "penugonda sai durga";
	        String host = "192.168.0.203";
	        String password = "1213";
	        int port = 7756;

	        try {
	            JSch jsch = new JSch();
	            Session session = jsch.getSession(user, host, port);
	            session.setPassword(password);
	            session.setConfig("StrictHostKeyChecking", "no");
	            session.connect();
	            ChannelExec channel = (ChannelExec) session.openChannel("exec");
	            channel.setCommand("ls -l /home/" + user);
	            channel.setErrStream(System.err);
	            channel.setInputStream(null);
	            java.io.InputStream in = channel.getInputStream();
	            channel.connect();
	            byte[] tmp = new byte[1024];
	            while (true) {
	                while (in.available() > 0) {
	                    int i = in.read(tmp, 0, 1024);
	                    if (i < 0) break;
	                    System.out.print(new String(tmp, 0, i));
	                }

	                if (channel.isClosed()) {
	                    System.out.println("Exit status: " + channel.getExitStatus());
	                    break;
	                }
	                Thread.sleep(1000);
	            }

	            channel.disconnect();
	            session.disconnect();

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	}
