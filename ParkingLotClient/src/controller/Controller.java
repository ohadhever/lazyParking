package controller;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import common.Reply;
import common.ReplyMessage;
import common.RequestMessage;
import common.RequestType;
import messagePassing.MessagePassing;
import messagePassing.MessagePassingObserver;
import view.AllowedActions;
import view.GUI;
import view.GUIObserver;


public class Controller extends Thread implements GUIObserver, MessagePassingObserver {
	public final int OPERATOR_PERMISSION = 2;
	private GUI view;
	private MessagePassing model;
	private String serverIP;
	private Queue<ReplyMessage> replies;
	private boolean moreExpiredUsers;
	private boolean stopRunning;
	
	public Controller(String serverIP) {
		view = new GUI();
		view.addObserver(this);
		model = null;
		moreExpiredUsers = false;
		stopRunning = false;
		this.serverIP = serverIP;
		replies = new LinkedList<ReplyMessage>();
		start();
	}	

	@Override
	public synchronized void receivedMessageEvent(ReplyMessage reply) {
		replies.add(reply);
		notify();
	}

	@Override
	public void exceptionEvent(String message) {
		view.displayError(message, "MESSAGE PASSING EXCEPTION");
	}

	@Override
	public void loginEvent(String username, String password) {
		if (username.equals("") || password.equals("")) {
			view.displayError("the username and password must not be empty", "INVALID USERNAME/PASSWORD");
			return;
		}
		try {
			model = new MessagePassing(serverIP);
			model.addObserver(this);
			RequestMessage request = new RequestMessage();
			request.type = RequestType.LOGIN;
			request.stringField1 = username;
			request.stringField2 = password;
			model.addRequest(request);
		} catch (IOException e) {
			view.displayError(e.getMessage(), "COMMUNICATION EXCEPTION");
		}
	}

	@Override
	public void floorChangedEvent(LinkedList<Integer> unknownParkingSpots) {
		while (!unknownParkingSpots.isEmpty()) {
			RequestMessage request = new RequestMessage();
			request.type = RequestType.GET_PARKING_STATUS;
			request.intField = unknownParkingSpots.removeFirst();
			model.addRequest(request);
		}
	}

	@Override
	public void logoutEvent() {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.LOGOUT;
		model.addRequest(request);
	}

	@Override
	public void openGateEvent() {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.OPEN_GATE;
		model.addRequest(request);
	}

	@Override
	public void cancelReservationEvent(int parkingSpotId) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.CANCEL_RESERVATION;
		request.intField = parkingSpotId;
		model.addRequest(request);
	}

	@Override
	public void changePasswordEvent(String username, String password) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.CHANGE_PW;
		request.stringField1 = username;
		request.stringField2 = password;
		model.addRequest(request);
	}

	@Override
	public void deleteDriverEvent(String username) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.REMOVE_DRIVER;
		request.stringField1 = username;
		model.addRequest(request);
	}

	@Override
	public void addDriverEvent(String name, String passwordStr, Date expirationDate) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.ADD_DRIVER;
		request.stringField1 = name;
		request.stringField2 = passwordStr;
		request.dateField = expirationDate;
		model.addRequest(request);
	}

	@Override
	public void updateDriverExpirationEvent(String username, Date date) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.CHANGE_EXPIRATION;
		request.stringField1 = username;
		request.dateField = date;
		model.addRequest(request);
	}

	@Override
	public void reserveParkingSpotEvent(int parkingSpotID, String reservedFor, Date expirationDate) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.RESERVE_PARKING_SPOT;
		request.intField = parkingSpotID;
		request.stringField1 = reservedFor;
		request.dateField = expirationDate;
		model.addRequest(request);
	}

	@Override
	public void requestForExpiredUsersEvent() {
		do {
			RequestMessage request = new RequestMessage();
			request.type = RequestType.GET_NEXT_EXPIRED;
			model.addRequest(request);
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException e) {
					view.displayError(e.getMessage(), "EXCEPTION WHILE WAITING FOR NEXT EXPIRED USER");
				}
			}
		} while(moreExpiredUsers);
	}


	@Override
	public void run() {
		ReplyMessage reply;
		while (!stopRunning) {
			if (!replies.isEmpty()) {
				synchronized(this) {
					reply = replies.poll();
				}
				handleReply(reply);
			} else {
				synchronized(this) {
					/*
					 * This looks redundant, but there is a tiny chance 
					 * that the thread will wait forever without it.
					 */
					if (!stopRunning && replies.isEmpty()) { 
						try {
							wait();
						} catch (InterruptedException e) {
							view.displayError(e.getMessage(), "EXCEPTION WHILE WAITING IN RUN LOOP");
						}
					}
				}
			}
		}
	}
	
	
	private void handleReply(ReplyMessage reply) {
		switch(reply.type) {
		case LOGIN:
			handleLoginReply(reply);
			break;
		case LOGOUT:
			handleLogoutReply(reply);
			break;
		case OPEN_GATE:
			handleOpenGateReply(reply);
			break;
		case GET_PARKING_STATUS:
			handleGetParkingStatusReply(reply);
			break;
		case CHANGE_PW:
			handleChangePasswordReply(reply);
			break;
		case RESERVE_PARKING_SPOT:
			handleReserveParkingSpotReply(reply);
			break;
		case ADD_DRIVER:
			handleAddDriverReply(reply);
			break;
		case REMOVE_DRIVER:
			handleDeleteDriverReply(reply);
			break;
		case CANCEL_RESERVATION:
			handleCancelReservationReply(reply);
			break;
		case CHANGE_EXPIRATION:
			handleUpdateDriverExpirationReply(reply);
			break;
		case GET_NEXT_EXPIRED:
			handleRequestForExpiredUsersReply(reply);
			break;
		}
	}
	

	private void handleLoginReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			if (reply.intField >= OPERATOR_PERMISSION)
				view.launchMainFrame(new AllowedActions(true, true, true, true, true, true, true, true, true), reply.stringField); // OPERATOR
			else
				view.launchMainFrame(new AllowedActions(true, false, false, false, false, false, true, false, false), reply.stringField); // DRIVER
		else if (reply.reply == Reply.USERNAME_NOT_FOUND || reply.reply ==Reply.WRONG_PW)
			view.displayError("WRONG USERNAME OR PASSWORD", "LOGIN ERROR");
	}

	private void handleGetParkingStatusReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			view.setParkingSpot(reply.intField, reply.boolField1, reply.boolField2, reply.stringField);
	}

	private void handleLogoutReply(ReplyMessage reply) {
		model.disconnect();
	}

	private void handleOpenGateReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			view.displayNotice("GATE OPENED", "SUCCESS");
	}

	private void handleCancelReservationReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			view.displayNotice("RESERVATION CANCELLED", "SUCCESS");
	}

	private void handleChangePasswordReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS) {
			view.displayNotice("password changed successfully", "SUCCESS");
			view.revertToPreviousWindow();
		}
		else
			view.displayError("username not found or you do not have high enough permission to change this user's password", "INVALID USERNAME");
	}

	private void handleDeleteDriverReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			view.deleteDriver("driver removed successfully", "SUCCESS");
		else
			view.displayError("username not found or you do not have high enough permission to remove this user", "INVALID USERNAME");
	}

	private void handleAddDriverReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			view.displayNotice("username: " + reply.stringField + "\ncard key: " + reply.intField, "Driver Credentials");
		else
			view.displayError("date expired", "INVALID DATE");
	}

	private void handleUpdateDriverExpirationReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			view.updateDriverExpiration("expiration date changed successfully", "SUCCESS");
		else if (reply.reply == Reply.USERNAME_NOT_FOUND)
			view.displayError("username not found", "INVALID USERNAME");
		else
			view.displayError("date expired", "INVALID DATE");
	}

	private void handleReserveParkingSpotReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS)
			view.displayNotice("parking spot reserved successfully", "SUCCESS");
		else
			view.displayError("date expired", "INVALID DATE");
	}
	
	private void handleRequestForExpiredUsersReply(ReplyMessage reply) {
		if (reply.reply == Reply.SUCCESS) {
			view.addExpiredUser(reply.stringField, reply.dateField);
			moreExpiredUsers = true;
		}
		else
			moreExpiredUsers = false;
		
		synchronized(this) {
			notify();
		}
	}
	
}
