package cse3010.lab.common;

import java.net.URL;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import cse3010.lab.common.Message;
import cse3010.lab.utils.DebugLog;
import cse3010.lab.utils.ObjectSerializer;

public class CSE3010SenderXMLRPC {
	// RPC related
	private static final String SVR_URL = "http://127.0.0.1:8090/xmlrpc"; //Hard coded
	private XmlRpcClientConfigImpl config;
	public XmlRpcClient client;
	public String reqHandlerName;
	public String serverName;
	
	public String loggerID = null;
	
	public CSE3010SenderXMLRPC() {
		try {
			
			//RPC related
			config = new XmlRpcClientConfigImpl();
			config.setEnabledForExtensions(true);
			config.setServerURL(new URL(SVR_URL));
			client = new XmlRpcClient();
			client.setConfig(config);
			
			reqHandlerName = "CSE3010Server.handleClientRequest";
			
		} catch(Exception exception) {
			DebugLog.elog("JavaClient.init: " + exception, loggerID);
		}
		
	}
	
	public CSE3010SenderXMLRPC(String srvURL, String srvName, String reqHN) {
		try {
			
			//RPC related
			config = new XmlRpcClientConfigImpl();
			config.setEnabledForExtensions(true);
			config.setServerURL(new URL(srvURL));
			client = new XmlRpcClient();
			client.setConfig(config);
	
			serverName = srvName;
			
			reqHandlerName = reqHN;
			
		} catch(Exception exception) {
			DebugLog.log("JavaClient.init: " + exception, loggerID);
			System.exit(1);
		}
		
	}
	
	public static String getSrvURL(String ip, String port){
		return "http://" + ip + ":" + port + "/xmlrpc";
	}
	
	//send the given msg and return what it gets from the receiver
	public Message send(Message msg) throws Exception {
		Vector<Object> requestParams;
		requestParams = new Vector<Object>();
		
		byte[] bytesToSend = ObjectSerializer.serialize(msg);
		
		requestParams.addElement(bytesToSend);
		
		Object result = client.execute(reqHandlerName, requestParams);
		
		Message srvResp = (Message) ObjectSerializer.deserialize((byte[]) result);
		
		return srvResp;		
	}
	
}
