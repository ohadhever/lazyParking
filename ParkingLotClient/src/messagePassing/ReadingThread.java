package messagePassing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import common.ReplyMessage;
import common.RequestType;



public class ReadingThread extends Thread {
	private MessagePassing model;
	private ObjectInputStream iStream;
	private boolean stopRunning;
	
	ReadingThread(MessagePassing model, Socket socket) throws IOException {
		this.model = model;
		iStream = new ObjectInputStream(socket.getInputStream());
		stopRunning = false;
	}
	
	public void run() {
		ReplyMessage msg = null;
		while (!stopRunning) {
			try {
				msg = (ReplyMessage) iStream.readObject();
				model.fireMessageReceived(msg);
				if (msg.type == RequestType.LOGOUT)
					disconnect();
			} catch (Exception e) {
				model.fireErrorOccured(e.getMessage());
				stopRunning = true;
			}
		}
	}
	
	
	private void disconnect() {
		synchronized(model) {
			model.notify();
		}
		stopRunning = true;
	}
	
	public boolean isRunning() {
		return (!stopRunning);
	}

}
