package cse3010.lab.cloudDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cse3010.lab.common.Message;
import cse3010.lab.common.MessageType;
import cse3010.lab.common.CSE3010ReceiverXMLRPC;
import cse3010.lab.utils.DebugLog;
import cse3010.lab.utils.ObjectSerializer;

public class CSE3010CloudDBServer {

	private static CSE3010ReceiverXMLRPC receiver;
	private String port = "16689";		//접속을 허가할 포트 번호
	
	public String loggerID = "CSE3010EdgeDBServer";
	public String serverID;
	public int serverNo;
	
	private static int clientCnt;
	public static ArrayList<String> clientDevices;
	
	private static HashMap<String, String> kvStore;
	
	
	/**
	 * This constructor is called multiple times. On client's request, WebServer
	 * creates a CSE3010EdgeDBServer instance on the process of creating a
	 * separate worker to handle the request. (out of our control)
	 * 
	 * Shared variable instantiation should be done in another constructor that
	 * is used for starting up the WebServer
	 * 
	 * NOTE: This should not be called by code that is not WebServer
	 * 
	 * @throws Exception
	 */
	public CSE3010CloudDBServer() throws Exception {
		if (loggerID == null) {
			this.loggerID = serverID + (serverNo++);
		}
		DebugLog.log("CSE3010EdgeDBServer constructor for each worker thread");
	}
	
	/**
	 * This constructor is to be used to start up the WebServer. This is meant
	 * to be called only once. 
	 * 
	 * @param srvName
	 * @throws Exception
	 */	
	public CSE3010CloudDBServer(String srvName) {
		serverID = srvName;
		serverNo = 0;
		clientCnt = 0;
		clientDevices = new ArrayList<String>();
		kvStore = new HashMap<String, String>();
	}
	
	public void runServer() {
		receiver = new CSE3010ReceiverXMLRPC(Integer.valueOf(port), "CSE3010CloudServer", this.getClass());	//접속을 허가할 포트 번호
	}

	public void stopServer() {
		receiver.webServer.shutdown();
	}
	/*
	 * This function handles Client's request accordingly
	 */
	public Object handleClientRequest(byte[] clReq) throws Exception {

		Message srvmsg = null;
		Object returnObj = null;
		Message cmsg = (Message) ObjectSerializer.deserialize(clReq);
		DebugLog.log("Server has received request for handleClientRequest", this.loggerID);
		boolean isRegistered = clientDevices.contains(cmsg.senderID);
		Message ack;
		switch (cmsg.msgType) {
		case MessageType.MSG_T_REGISTER_CLIENT:
			DebugLog.log("Register Client Request", this.loggerID);
			returnObj = handleRegisterClient(cmsg);
			ack = new Message();
			ack.msgType = MessageType.MSG_T_ACK;
			ack.msgContent = returnObj;
			srvmsg = (Message) ack;
			break;
		case MessageType.MSG_T_READ_CMD:
			if (isRegistered) {
				DebugLog.log("Read Request", this.loggerID);
				returnObj = handleReadRequest(cmsg);
				ack = new Message();
				if (returnObj != null) {
					ack.msgType = MessageType.MSG_T_ACK;
				} else {
					ack.msgType = MessageType.MSG_T_NACK;
				}
				ack.msgContent = returnObj;
				srvmsg = (Message) ack;
			} else {
				DebugLog.log("Request is from an unregistered client. Reject.");
				ack = new Message();
				ack.msgType = MessageType.MSG_T_NACK;
				ack.msgContent = (Object) "REGISTER FIRST";
				srvmsg = (Message) ack;
			}
			break;
		case MessageType.MSG_T_WRITE_CMD:
			if (isRegistered) {
				DebugLog.log("Write Request", this.loggerID);
				returnObj = handleWriteRequest(cmsg);
				ack = new Message();
				ack.msgType = MessageType.MSG_T_ACK;
				ack.msgContent = returnObj;
				srvmsg = (Message) ack;
			} else {
				DebugLog.log("Request is from an unregistered client. Reject.");
				ack = new Message();
				ack.msgType = MessageType.MSG_T_NACK;
				ack.msgContent = (Object) "REGISTER FIRST";
				srvmsg = (Message) ack;
			}
			break;
		case MessageType.MSG_T_READ_KEY_LIST:
			DebugLog.log("Read Key List Request", this.loggerID);
			returnObj = handleReadKeyListRequest(cmsg);
			ack = new Message();
			ack.msgType = MessageType.MSG_T_ACK;
			ack.msgContent = returnObj;
			srvmsg = (Message) ack;
			break;
		default:
			break;
		}

		Object retObj = ObjectSerializer.serialize(srvmsg);
		DebugLog.log("Server is responding to a client request", this.loggerID);
		return retObj;
	}
	
	private Object handleReadRequest(Message cmsg) {
		String keyStr = (String) cmsg.msgContent;
		String valStr = kvStore.get(keyStr);
		return valStr;
	}

	private Object handleWriteRequest(Message cmsg) {
		String keyStr = ((String[]) cmsg.msgContent)[0];
		String valStr = ((String[]) cmsg.msgContent)[1];
		String oldVal = kvStore.put(keyStr, valStr);
		return oldVal;
	}
	private static String getUniqueCID() {
		return "" + clientCnt++;
	}
	
	private Object handleRegisterClient(Message cmsg) {
		String cid = getUniqueCID();
		synchronized(clientDevices) {
			clientDevices.add(cid);
			DebugLog.log("handleRegisterClient returns ID=" + cid);
		}
		return (Object) cid;
	}

	private Object handleReadKeyListRequest(Message cmsg) {
		List<String> keyList = new ArrayList<String>(kvStore.keySet());
		return (Object) keyList;
	}
	
	public static void main(String[] argv) {
		CSE3010CloudDBServer server = new CSE3010CloudDBServer("standalone-CSE3010EdgeDBServerEngine-main");
		server.runServer();
	}
}
