package messagePassing;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import common.ReplyMessage;
import common.RequestMessage;


public class MessagePassing {
	/*
	 * The classes that are common to both the server and the client:
	 * RequestMessage, ReplyMessage, RequestType (Enum), Reply (Enum)
	 * are found in the common package
	 */
	public static final int SERVER_PORT = 7000;
	private LinkedList<MessagePassingObserver> observers;
	private WritingThread wThread;
	private ReadingThread rThread;
	private Socket socket;
	
	public MessagePassing(String serverIp) throws IOException {
		observers = new LinkedList<MessagePassingObserver>();
		
		try {
			socket = new Socket(serverIp, SERVER_PORT);
			wThread = new WritingThread(this, socket);
			rThread = new ReadingThread(this, socket);
			
			rThread.start();
			wThread.start();
		} catch (IOException e) {
			disconnect();
			throw e;
		}
	}
	
	public void disconnect() {
		if (socket != null && !socket.isClosed()) {
			try {
					if (wThread != null && wThread.isRunning()) {
						wThread.disconnect();
					}
				
					/* Waiting for the reading thread to disconnect */
					synchronized(this) {
						if (rThread != null && rThread.isRunning()) {
							wait();
						}
					}
				
					socket.close();
			} catch (Exception e) {
				fireErrorOccured(e.getMessage());
			}
		}
	}
	
	boolean isAllRunning() {
		return wThread.isRunning() && rThread.isRunning();
	}
	
	void fireErrorOccured(String message) {
		for (MessagePassingObserver observer : observers)
			observer.exceptionEvent(message);
	}
	
	void fireMessageReceived(ReplyMessage reply) {
		for (MessagePassingObserver observer : observers)
			observer.receivedMessageEvent(reply);
	}
	
	public void addObserver(MessagePassingObserver observer) {
		synchronized(observers) {
			observers.add(observer);
		}
	}
	
	public void removeObserver(MessagePassingObserver observer) {
		synchronized(observers) {
			observers.remove(observer);
		}
	}
	
	public void addRequest(RequestMessage request) {
		wThread.addRequest(request);
	}

}
