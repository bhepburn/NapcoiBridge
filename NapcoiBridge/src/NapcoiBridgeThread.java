import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class NapcoiBridgeThread extends Thread {
	
	private Queue<String> messageQueue = new LinkedBlockingDeque<String>();

	public void run() {
		while (true) {
			Socket socket = null;
			try {
				socket = new java.net.Socket("192.168.1.201", 8003);
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				int theByte;

				int i = 0;
				int msgLength = 42;
				StringBuffer message = new StringBuffer();
				while ((theByte = bis.read()) != -1) {
					i++;
					if (i == 1) {
						assert (theByte == 187);
					} else if (i == 3) {
						msgLength = theByte;
						assert (msgLength == 32 || msgLength == 42);
					} else if (i > 7 && i < msgLength - 1) {
						message.append((char) theByte);
					} else if (i == msgLength) {
						if (msgLength == 42) {
							messageQueue.add(message.toString().trim());
						}
						message = new StringBuffer();
						i = 0;
						msgLength = 42;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
