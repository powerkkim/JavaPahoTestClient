package com.appspace.pushclienttest;

public class Constant {
	public static final int MQTT_KEEP_ALIVE = 2 * 60; 
	
	public static final String ARG_BROKER_IP = "192.168.25.7";
	public static final int ARG_BROKER_PORT = 1883;
	public static final String ARG_ACTION_DEFALT = "subscribe";
	public static final int ARG_QOS_DEFALT = 0;
	public static final String ARG_TOPIC_DEFALT = "FB_TOPIC/#";
	public static final String ARG_PUB_TOPIC_DEFALT = "FB_TOPIC/TEST/T";
	
	public static final int ARG_CLIENT_MAX_COUNT = 10;
	
	public static final String ARG_MESSAGE_DEFALT = "Message from async callback Paho MQTTv3 Java client sample";
	public static final boolean MQTT_CLEAN_SESSION = true;
	public static final String WILL_MESSAGE = "check/down";
	
	public static final String MQTT_DEVICEID_PREFIX = "A_";
	public static final String MQTT_DATA_STORE_FOLDER = "pushTestStore";
	public static final String MQTT_DATA_CLIENT_COUNT = "./"+Constant.MQTT_DATA_STORE_FOLDER+"/clientCount.txt";
	 
	
}
