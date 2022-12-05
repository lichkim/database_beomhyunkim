package cse3010.lab.cloudDB;

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import cse3010.lab.utils.DebugLog;

public class CSE3010ReceiverXMLRPC {

	public WebServer webServer;
	
	public String loggerID = "CSE3010ReceiverXMLRPC";
	
	public CSE3010ReceiverXMLRPC(int port) {
		
		try {	
			
			webServer = new WebServer(port);

			XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

			PropertyHandlerMapping phm = new PropertyHandlerMapping();
		
			phm.addHandler("CSE3010Server", cse3010.lab.cloudDB.CSE3010CloudDBServer.class);
			
			xmlRpcServer.setHandlerMapping(phm);

			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer
					.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);

			webServer.start();
			
			DebugLog.log("Started successfully.", loggerID);
			DebugLog.log("Accepting requests. (Halt program to stop.)", loggerID);
			
		} catch (XmlRpcException e) {
			DebugLog.log("JavaServer: " + e + " port: " + port, loggerID);
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			DebugLog.log("JavaServer: " + e + " port: " + port, loggerID);
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
}
