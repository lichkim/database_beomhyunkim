package cse3010.lab.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cse3010.lab.common.Message;
import cse3010.lab.common.MessageType;
import cse3010.lab.utils.DebugLog;

public class CSE3010Client {

	private static String port = "16679";
	private String srvip = "http://127.0.0.1:" + port + "/xmlrpc";
	private String srvName = "CSE3010 Edge DB Server";
	private String reqHandler = "CSE3010EdgeDBServer.handleClientRequest";
	
	
	private CSE3010SenderXMLRPC sender;
	
	public String loggerID = "CSE3010Client";
	public String clientID;
	private volatile boolean finishFlag;

	public CSE3010Client() throws Exception {
		DebugLog.log("srvip=" + srvip);
		sender = new CSE3010SenderXMLRPC(srvip, srvName, reqHandler);
		sender.loggerID = this.loggerID;
		clientID = null;
	}
	
	private void doRegister() throws Exception {
		if (clientID != null) {
			System.out.println("Skipping the regster command.. "
					+ "This client is already registered with the server: clientID=" 
					+ clientID);
		} else {
			Message regMsg = new Message();
			regMsg.msgType = MessageType.MSG_T_REGISTER_CLIENT;
			Message srvmsg = sender.send(regMsg);
			if (srvmsg.msgType == MessageType.MSG_T_ACK) {
				DebugLog.log("server responds with ACK.", this.loggerID);
				Message ackmsg = (Message) srvmsg;
				clientID = (String) ackmsg.msgContent;
				DebugLog.log("Registered with Server: my clientID=" + clientID);
			} else {
				// Server NACK...
				// For now, we just exit.
				DebugLog.elog("SEVERE: Server NACK. For now, we just exit.", this.loggerID);
				System.exit(1);
			}
		}
	}
	
	public static void printMainMenu() {
		DebugLog.log("<main> What do you want to do? "
				+ "[1] Register Client"
				+ "[2] Read"
				+ "[3] Write"
				+ "[100] Quit\n");
	}

	private void doRead(String key) throws Exception {
		Message readMsg = new Message();
		readMsg.msgType = MessageType.MSG_T_READ_CMD;
		readMsg.msgContent = (Object) key;
		readMsg.senderID = clientID;
		Message svrmsg = sender.send(readMsg);
		String recievedMsg = (String) svrmsg.msgContent;

		if (svrmsg.msgType == MessageType.MSG_T_ACK) {
			DebugLog.log("server responds with ACK.", this.loggerID);
			Message ackmsg = (Message) svrmsg;
			String value = (String) ackmsg.msgContent;
			DebugLog.log("Read value from Server: key = " + key + " value = " + value);
		} else {
			if (recievedMsg != null && recievedMsg.equalsIgnoreCase("REGISTER FIRST")) {
				DebugLog.log("Register Fisrt!");
			} else {
				//Server NACK...
				DebugLog.elog("Server NACK: Key may not exist yet... Create it first.", this.loggerID);
			}
			
		}
	}

	private void doWrite(String key, String value) throws Exception {
		Message writeMsg = new Message();
		writeMsg.msgType = MessageType.MSG_T_WRITE_CMD;
		String[] writeMsgContent = new String[2];
		writeMsgContent[0] = key;
		writeMsgContent[1] = value;
		writeMsg.msgContent = (Object) writeMsgContent;
		writeMsg.senderID = clientID;
		Message svrmsg = sender.send(writeMsg);
		String recievedMsgContent = (String) svrmsg.msgContent;
		if (svrmsg.msgType == MessageType.MSG_T_ACK) {
			DebugLog.log("server responds with ACK.", this.loggerID);
		} else if (svrmsg.msgType == MessageType.MSG_T_NACK && recievedMsgContent.equalsIgnoreCase("REGISTER FIRST")) {
			DebugLog.log("Register First!");
		} else {
			//Server NACK... Unlikely
			DebugLog.elog("SERVER E: Server NACK for some reason... Exit.", this.loggerID);
			System.exit(1);
		}
	}
	
	/**
	 * 
	 * main function for the testing and debugging purpose.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("enter the port number for this client's Edge DB Server:");
		BufferedReader inputBr = new BufferedReader(new InputStreamReader(System.in));
		try {
			String input = inputBr.readLine();
			port = input;
			System.out.println("port number for this client's Edge DB server= " + port);
		} catch (IOException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean quitFlag = false;

		String loggerID = "standalone-CSE3010Client-main";

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input;
			printMainMenu();
			CSE3010Client cl = new CSE3010Client();
			cl.loggerID = loggerID;
			while (!quitFlag && ((input = br.readLine()) != null)) {
				try {
					int cmd = Integer.valueOf(input);
					switch (cmd) {
					case 1: // register the client
						cl.doRegister();
						DebugLog.log("Register Client.. Done");
						break;
					case 2:	//read
						DebugLog.log("Enter the key you want to read from:");
						input = br.readLine();
						if (input == null) {
							DebugLog.elog("Failed to read, because the input is null");
						} else {
							cl.doRead(input);
							DebugLog.log("Read...Done");
						}
						break;
					case 3:	//write
						DebugLog.log("Enter the key you want to write to:");
						String keyStr = br.readLine();
						DebugLog.log("Enter the value you want to write to the key:");
						String valStr = br.readLine();
						if (keyStr == null || valStr == null) {
							DebugLog.elog("Failed to write, because either key or value is null");
						} else {
							cl.doWrite(keyStr, valStr);
							DebugLog.log("Write...Done");
						}
						break;
					case 100:
						quitFlag = true;
						break;
					default:
						break;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (!quitFlag) {
					printMainMenu();
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			DebugLog.elog("JavaClient: " + exception, loggerID);
		}

		DebugLog.log("The main function is Done now...", loggerID);
		DebugLog.log("Goodbye!!!", loggerID);

	}
	
}
