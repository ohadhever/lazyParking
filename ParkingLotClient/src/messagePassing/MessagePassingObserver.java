package messagePassing;

import common.ReplyMessage;

public interface MessagePassingObserver {
	public void receivedMessageEvent(ReplyMessage reply);
	
	public void exceptionEvent(String message);
}
