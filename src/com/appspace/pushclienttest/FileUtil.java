package com.appspace.pushclienttest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

	FileUtil(){
		
	}

	public void makeStorageFolder() {
		File file = new File(".", Constant.MQTT_DATA_STORE_FOLDER);
		if(!file.exists()){
			file.mkdir();
		}
	}
	
	public void setCountFile(int nCount){
		String szTestFile = Constant.MQTT_DATA_CLIENT_COUNT;
		BufferedWriter writer;
		try {
			 writer = new BufferedWriter(new FileWriter(szTestFile));
			 writer.write(String.valueOf(nCount));
			 writer.close();
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getCount(){
		String szTestFile = Constant.MQTT_DATA_CLIENT_COUNT;
		int nCount = 0;
		File file = new File(szTestFile);
		BufferedReader reader;
		try {
			FileReader fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);
			
			String line = null;
			while((line = reader.readLine() )  != null ){
				nCount = Integer.parseInt(line);
			}
			
			reader.close();
		} catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return nCount;
		
	}
}
