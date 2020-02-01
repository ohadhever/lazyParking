package messagePassing;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import common.RequestMessage;


public class WritingThread extends Thread {
	private MessagePassing model;
	private ObjectOutputStream oStream;
	private boolean stopRunning;
	private Queue<RequestMessage> requestsQueue;
	
	WritingThread(MessagePassing model, Socket socket) throws IOException {
		this.model = model;
		oStream = new ObjectOutputStream(socket.getOutputStream());
		stopRunning = false;
		requestsQueue = new LinkedList<RequestMessage>();
	}
	
	@Override
	public void run() {
		RequestMessage msg;
		
		while (!stopRunning) {
			if (!requestsQueue.isEmpty()) {
				synchronized(this) {
					msg = requestsQueue.poll();
				}
				try {
					oStream.writeObject(msg);
				} catch (IOException e) {
					model.fireErrorOccured(e.getMessage());
					stopRunning = true;
				}
			} else {
				synchronized(this) {
					/*
					 * This looks redundant, but there is a tiny chance 
					 * that the thread will wait forever without it.
					 */
					if (!stopRunning && requestsQueue.isEmpty()) { 
						try {
							wait();
						} catch (InterruptedException e) {
							model.fireErrorOccured(e.getMessage());
						}
					}
				}
			}
		}
	}
	
	
	synchronized void addRequest(RequestMessage request) {
		requestsQueue.add(request);
		notify();
	}
	
	void disconnect() throws IOException, InterruptedException {
		synchronized(this) {
			stopRunning = true;
			notify();
		}
	}
	
	public boolean isRunning() {
		return (!stopRunning);
	}

}
