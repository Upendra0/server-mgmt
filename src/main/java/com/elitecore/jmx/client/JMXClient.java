/**
 * 
 */
package com.elitecore.jmx.client;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * @author Sunil Gulabani
 * Sep 1, 2015
 */
public class JMXClient {
	
	protected MBeanServerConnection mBeanServerConnection ;
	protected JMXConnector jmxConnector;
	
	protected String ipAddress;
	protected String port;
	
	public JMXClient(String ipAddress, String port) {
		this.ipAddress = ipAddress ; 
		this.port = port;
	}
	
	protected boolean isConnected(){
		try{
			System.out.println("IPAddress: " + ipAddress);
			System.out.println("port: " + port);
			jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + ipAddress + ":" + port + "/jmxrmi"), null);
			mBeanServerConnection = jmxConnector.getMBeanServerConnection();
			if(mBeanServerConnection!=null)
				return true;
		}catch(IOException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public void read(){
		
		Integer serverInstancePort = 9696;
		String objectNameStr = "ServerMgmt:name=IServerManagementMBean" ;
		try {
        	if(isConnected()){
        		String serverHome = (String) mBeanServerConnection.invoke(
														new ObjectName(objectNameStr), 
														"crestelPEngineHome", 
														new Object[]{}, 
														new String[]{});
        		
        		System.out.println("serverHome: " + serverHome);
        		
        		String javaHome = (String) mBeanServerConnection.invoke(
						new ObjectName(objectNameStr), 
						"javaHome", 
						new Object[]{}, 
						new String[]{});

        		System.out.println("javaHome: " + javaHome);
        		//System.exit(0);
        		boolean portAvailable = (Boolean) mBeanServerConnection.invoke(
        															new ObjectName(objectNameStr), 
        															"portAvailable", 
        															new Object[]{serverInstancePort}, 
        															new String[]{Integer.class.getName()});
        		System.out.println("portAvailable: " + portAvailable);
        		System.out.println("___________________________________________");
        		if(portAvailable){
        			boolean fileCreated = (Boolean) mBeanServerConnection.invoke(
															new ObjectName(objectNameStr), 
															"createServerInstanceScript", 
															new Object[]{"",serverInstancePort, 256, 512}, 
															new String[]{String.class.getName(),Integer.class.getName(),Integer.class.getName(),Integer.class.getName()});
        			System.out.println("fileCreated: " + fileCreated);
        			System.out.println("___________________________________________");
        			if(fileCreated){
        				String response = (String) mBeanServerConnection.invoke(
															new ObjectName(objectNameStr), 
															"runScript", 
															new Object[]{serverInstancePort}, 
															new String[]{Integer.class.getName()});
        				System.out.println("runScript Response: " + response);
        				System.out.println("___________________________________________");
        				portAvailable = (Boolean) mBeanServerConnection.invoke(
        													new ObjectName(objectNameStr), 
        													"portAvailable", 
        													new Object[]{serverInstancePort}, 
        													new String[]{Integer.class.getName()});
                		System.out.println("portAvailable: " + portAvailable);
                		System.out.println("___________________________________________");
        				if(portAvailable){
        					System.out.println("Server Started...");
        					System.out.println("___________________________________________");
        				}
        			}
        		}
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		//JMXClient client = new JMXClient("localhost","1617");
		//JMXClient client = new JMXClient("127.0.0.1","1617");
		JMXClient client = new JMXClient("127.0.0.1","1617");
		client.read();
	}
}
