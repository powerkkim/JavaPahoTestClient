package com.appspace.pushclienttest;
/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */



import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 *
 * It can be run from the command line in one of two modes:
 *  - as a publisher, sending a single message to a topic on the server
 *  - as a subscriber, listening for messages from the server
 *
 *  There are three versions of the sample that implement the same features
 *  but do so using using different programming styles:
 *  <ol>
 *  <li>Sample (this one) which uses the API which blocks until the operation completes</li>
 *  <li>SampleAsyncWait shows how to use the asynchronous API with waiters that block until
 *  an action completes</li>
 *  <li>SampleAsyncCallBack shows how to use the asynchronous API where events are
 *  used to notify the application when an action completes<li>
 *  </ol>
 *
 *  If the application is run with the -h parameter then info is displayed that
 *  describes all of the options / parameters.
 */
public class Sample { 
	
	public static void main(String[] args) {

		
        
        
		// Default settings:
		boolean quietMode 	= false;
		String action 		= Constant.ARG_ACTION_DEFALT;
		String topic 		= "";
		String message 		= Constant.ARG_MESSAGE_DEFALT;
		int qos 			= Constant.ARG_QOS_DEFALT;
		String broker 		= Constant.ARG_BROKER_IP;
		int port 			= Constant.ARG_BROKER_PORT;
		String clientId 	= null;
		String subTopic		= Constant.ARG_TOPIC_DEFALT;
		String pubTopic 	= Constant.ARG_PUB_TOPIC_DEFALT;
		boolean cleanSession = Constant.MQTT_CLEAN_SESSION;			// Non durable subscriptions
		boolean ssl = false;
		String password = "";  // mStbMac.getByte();
		String userName = "";  // STBID 
		int clientMaxCount = Constant.ARG_CLIENT_MAX_COUNT;
		
		// FileCount 
		FileUtil fileutil = new FileUtil();
		int nClinetCount = 0;
		
		
		// Parse the arguments -
		for (int i=0; i<args.length; i++) {
			// Check this is a valid argument
			if (args[i].length() == 2 && args[i].startsWith("-")) {
				char arg = args[i].charAt(1);
				// Handle arguments that take no-value
				switch(arg) {
					case 'h': case '?':	printHelp(); return;
					case 'q': quietMode = true;	continue;
				}

				// Now handle the arguments that take a value and
				// ensure one is specified
				if (i == args.length -1 || args[i+1].charAt(0) == '-') {
					System.out.println("Missing value for argument: "+args[i]);
					printHelp();
					return;
				}
				switch(arg) {
					case 'a': action = args[++i];                 break;
					case 't': topic = args[++i];                  break;
					case 'm': message = args[++i];                break;
					case 's': qos = Integer.parseInt(args[++i]);  break;
					case 'b': broker = args[++i];                 break;
					case 'p': port = Integer.parseInt(args[++i]); break;
					case 'i': clientId = args[++i];				  break;
					case 'c': cleanSession = Boolean.valueOf(args[++i]).booleanValue();  break;
					case 'k': System.getProperties().put("javax.net.ssl.keyStore", args[++i]); break;
					case 'w': System.getProperties().put("javax.net.ssl.keyStorePassword", args[++i]); break;
					case 'r': System.getProperties().put("javax.net.ssl.trustStore", args[++i]); break;
					case 'v': ssl = Boolean.valueOf(args[++i]).booleanValue(); break;
					case 'u': userName = args[++i];               break;
					case 'z': password = args[++i];               break;
					case 'l': clientMaxCount = Integer.parseInt(args[++i]); break;
					
					default:
						System.out.println("Unrecognised argument: "+args[i]);
						printHelp();
						return;
				}
			} else {
				System.out.println("Unrecognised argument: "+args[i]);
				printHelp();
				return;
			}
		}
		
		Runtime r = Runtime.getRuntime();
        //프로그램 종료시(CTRL+C를 누르는 경우도 해당)실행 할 쓰레드를 JVM에 알려 줍니다.
        r.addShutdownHook(new Thread(new Shutdown(clientMaxCount)));
        
		// 기존 connection 에 대한 처리 유지 하며 Client 증가 할 수 있도록 처리. [[
		fileutil.makeStorageFolder(); 
		try {
			nClinetCount = fileutil.getCount();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			System.out.println( "Count 파일 없음."); 	
		}
		int nStartIndex = nClinetCount;
		fileutil.setCountFile(nClinetCount+clientMaxCount);
		// 기존 connection 에 대한 처리 유지 하며 Client 증가 할 수 있도록 처리. ]]
		
		// Validate the provided arguments
		if (!action.equals("publish") && !action.equals("subscribe")) {
			System.out.println("Invalid action: "+action);
			printHelp();
			return;
		}
		if (qos < 0 || qos > 2) {
			System.out.println("Invalid QoS: "+qos);
			printHelp();
			return;
		}
		if (topic.equals("")) {
			// Set the default topic according to the specified action
			if (action.equals("publish")) {
				topic = pubTopic;
			} else {
				if(userName == null || userName.equals("")){
					topic = subTopic;
				}
				else{
					topic = userName + "/#";
				}
			}
		}

		String protocol = "tcp://";

	    if (ssl) {
	      protocol = "ssl://";
	    }
	
	    final String url = protocol + broker + ":" + port;
	
		if (clientId == null || clientId.equals("")) {
			clientId = Constant.MQTT_DEVICEID_PREFIX+action;
		}
	
		final String fClientID = clientId;
		final boolean fcleanSession = cleanSession;	
		final boolean fquietMode = quietMode;	
		final String fuserName = userName;
		final String fpassword = password;
		final String faction = action;
		final String ftopic = topic;
		final int fqos = qos; 
		final String fmessage = message;
		final int fclientMaxCount = clientMaxCount; 
		final ClientItem[] arrClient = new ClientItem[clientMaxCount];
		final int fstartIndex = nStartIndex;
		System.out.println("Client Start");
		
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				 
				// Create an instance of this class
				for( int nLoop=0; nLoop < fclientMaxCount; nLoop++){
					int nIndex = nLoop + fstartIndex;
					String nTempClientId = fClientID + nIndex;
					String TempUserName = "USER"+nIndex;
					String TempPassword = "PASS"+nIndex;
					if( fuserName == null || fuserName.equals("")){
						TempUserName = "USER"+nIndex;
					}
					else{
						TempUserName = fuserName;
					}
					if( fpassword == null || fpassword.equals("") ){
						TempPassword = "PASS"+nIndex;
					}
					else{
						TempPassword = fpassword;
					}
					
					ClientItem sampleClient = new ClientItem(url, nTempClientId, fcleanSession, fquietMode, TempUserName, TempPassword);
					String TempTopic = ftopic;
					
					sampleClient.setData(faction, TempTopic, fqos, fmessage);
					arrClient[nLoop] = sampleClient;
					System.out.println("Client Count :"+nIndex);
				}
				
				try {
					System.in.read();
				} catch (IOException e) {
					//If we can't read we'll just exit
				}
			
				// Disconnect the client from the server
				for(int nLoop=0; nLoop < fclientMaxCount; nLoop++){
					try {
						arrClient[nLoop].getClient().disconnect();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println( "Syntax:\n\n" +"Disconnected"); 	
				
			}
		});
		th.start(); 
	}
	


	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

	   static void printHelp() {
	      System.out.println(
	          "Syntax:\n\n" +
	              "    Sample [-h] [-a publish|subscribe] [-t <topic>] [-m <message text>]\n" +
	              "            [-s 0|1|2] -b <hostname|IP address>] [-p <brokerport>] [-i <clientID>]\n\n" +
	              "    -h  Print this help text and quit\n" +
	              "    -q  Quiet mode (default is false)\n" +
	              "    -a  Perform the relevant action (default is publish)\n" +
	              "    -t  Publish/subscribe to <topic> instead of the default\n" +
	              "            (publish: \"Sample/Java/v3\", subscribe: \"Sample/#\")\n" +
	              "    -m  Use <message text> instead of the default\n" +
	              "            (\"Message from MQTTv3 Java client\")\n" +
	              "    -s  Use this QoS instead of the default (2)\n" +
	              "    -b  Use this name/IP address instead of the default (m2m.eclipse.org)\n" +
	              "    -p  Use this port instead of the default (1883)\n\n" +
	              "    -i  Use this client ID instead of SampleJavaV3_<action>\n" +
	              "    -c  Connect to the server with a clean session (default is false)\n" +
	              "     \n\n Security Options \n" +
	              "     -u Username \n" +
	              "     -z Password \n" +
	              "     \n\n SSL Options \n" +
	              "    -v  SSL enabled; true - (default is false) " +
	              "    -k  Use this JKS format key store to verify the client\n" +
	              "    -w  Passpharse to verify certificates in the keys store\n" +
	              "    -r  Use this JKS format keystore to verify the server\n" +
	              "    -l  Client MAX Count Setting\n" +
	              " If javax.net.ssl properties have been set only the -v flag needs to be set\n" +
	              "Delimit strings containing spaces with \"\"\n\n" +
	              "Publishers transmit a single message then disconnect from the server.\n" +
	              "Subscribers remain connected to the server and receive appropriate\n" +
	              "messages until <enter> is pressed.\n\n"
	          );
    }

}