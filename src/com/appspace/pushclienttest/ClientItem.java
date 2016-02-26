package com.appspace.pushclienttest;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Locale;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class ClientItem  implements MqttCallback {
	// Private instance variables
	private MqttClient 			client;
	private String 				brokerUrl;
	private boolean 			quietMode;
	private MqttConnectOptions 	conOpt;
	private boolean 			clean;
	private String password;  // mStbMac.getByte();
	private String userName;  // STBID
	private String clientID;
		
	public ClientItem(String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password){
		// driving the client API can begin
		try {
			// Create an instance of this class
			getClientItem(brokerUrl, clientId, cleanSession, quietMode,userName,password);
		} catch(MqttException me) {
			log("Mqtt Exception 1"+brokerUrl+" with client ID "+client.getClientId()+"/user:"+userName+"/pass:"+password);
	    	
			// Display full details of any exception that occurs
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
	}
	
	public void setData(String action, String topic, int qos, String message){
		try {
				// Create an instance of this class
			if (action.equals("publish")) {
				publish(topic,qos, message.getBytes() );
			} else if (action.equals("subscribe")) {
				subscribe(topic,qos);
			}
		} catch(MqttException me) {
			log("Mqtt Exception 2"+brokerUrl+" with client ID "+client.getClientId()+"/user:"+userName+"/pass:"+password);

			// Display full details of any exception that occurs
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
	}
	
	public void getClientItem(String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password) throws MqttException {
    	this.brokerUrl = brokerUrl;
    	this.quietMode = quietMode;
    	this.clean 	   = cleanSession;
    	this.password = password;
    	this.userName = userName;
    	this.clientID = clientId;
    	//This sample stores in a temporary directory... where messages temporarily
    	// stored until the message has been delivered to the server.
    	//..a real application ought to store them somewhere
    	// where they are not likely to get deleted or tampered with
    	//String tmpDir = System.getProperty("java.io.tmpdir");
        String tmpDir = System.getProperty("user.dir")+"/"+Constant.MQTT_DATA_STORE_FOLDER;
        //log("timed out:"+tmpDir);
    	//MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence();
        
    	try {
    		// Construct the connection options object that contains connection parameters
    		// such as cleanSession and LWT
	    	conOpt = new MqttConnectOptions();
	    	conOpt.setCleanSession(clean);
	    	if(password != null ) {
	    	  conOpt.setPassword(this.password.toCharArray());
	    	}
	    	if(userName != null) {
	    	  conOpt.setUserName(this.userName);
	    	}

	        conOpt.setKeepAliveInterval(Constant.MQTT_KEEP_ALIVE);
	        
	        String topic = Constant.WILL_MESSAGE + "/" + this.userName;
		    conOpt.setWill(topic, this.userName.getBytes(), 2, true);
	        
    		// Construct an MQTT blocking mode client
			client = new MqttClient(this.brokerUrl,clientId, null);

			// Set this wrapper as the callback handler
	    	client.setCallback(this);

		} catch (MqttException e) {
			e.printStackTrace();
			//log("Unable to set up client: "+e.toString());
			System.exit(1);
		}
    	
    }


    /**
     * Publish / send a message to an MQTT server
     * @param topicName the name of the topic to publish to
     * @param qos the quality of service to delivery the message at (0,1,2)
     * @param payload the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload) throws MqttException {

    	// Connect to the MQTT server
    	log("Connecting to "+brokerUrl + " with client ID "+client.getClientId());
    	client.connect(conOpt);
    	log("Connected");

    	String time = new Timestamp(System.currentTimeMillis()).toString();
    	log("Publishing at: "+time+ " to topic \""+topicName+"\" qos "+qos);

    	// Create and configure a message
   		MqttMessage message = new MqttMessage(payload);
    	message.setQos(qos);

    	// Send the message to the server, control is not returned until
    	// it has been delivered to the server meeting the specified
    	// quality of service.
    	client.publish(topicName, message);

    	// Disconnect the client
    	client.disconnect();
    	log("Disconnected");
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {

    	// Connect to the MQTT server
    	client.connect(conOpt);
    	log("Connected to "+brokerUrl+" with client ID "+client.getClientId()+"/user:"+userName+"/pass:"+password);
    	// Subscribe to the requested topic
    	// The QoS specified is the maximum level that messages will be sent to the client at.
    	// For instance if QoS 1 is specified, any messages originally published at QoS 2 will
    	// be downgraded to 1 when delivering to the client but messages published at 1 and 0
    	// will be received at the same level they were published at.
    	log("Subscribing to topic \""+topicName+"\" qos "+qos);
    	client.subscribe(topicName, qos);

    	// SKB mqtt 요청 필요 사황.
    	// broker 서버 선택과 관련됨.
    	String topic =  "check/up/" + userName;
    	MqttTopic mKeepAliveTopic = client.getTopic(String.format(Locale.US, topic, this.clientID));
		MqttMessage message = new MqttMessage(this.userName.getBytes());
		message.setQos(0);
		mKeepAliveTopic.publish(message);
	
	// Continue waiting for messages until the Enter is pressed
//    	log("Press <Enter> to exit");
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			//If we can't read we'll just exit
//		}

		// Disconnect the client from the server
		//client.disconnect();
		//log("Disconnected");
	
	

	 
    }

    public MqttClient getClient(){
    	return client;
    }
    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does nothing
     * @param message the message to log
     */
    private void log(String message) {
    	if (!quietMode) {
    		System.out.println(message);
    	}
    }

    /****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
	public void connectionLost(Throwable cause) {
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
		// logic at this point. This sample simply exits.
		log("Connection to " + brokerUrl + " lost!" + cause);
		System.exit(1);
	}

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Called when a message has been delivered to the
		// server. The token passed in here is the same one
		// that was passed to or returned from the original call to publish.
		// This allows applications to perform asynchronous
		// delivery without blocking until delivery completes.
		//
		// This sample demonstrates asynchronous deliver and
		// uses the token.waitForCompletion() call in the main thread which
		// blocks until the delivery has completed.
		// Additionally the deliveryComplete method will be called if
		// the callback is set on the client
		//
		// If the connection to the server breaks before delivery has completed
		// delivery of a message will complete after the client has re-connected.
		// The getPendingTokens method will provide tokens for any messages
		// that are still to be delivered.
	}

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server that matches any
		// subscription made by the client
		String time = new Timestamp(System.currentTimeMillis()).toString();
		System.out.println("UserID:"+userName+"\tTime:\t" +time +
                           "  Topic:\t" + topic +
                           "  Message:\t" + new String(message.getPayload()) +
                           "  QoS:\t" + message.getQos());
	}
}
