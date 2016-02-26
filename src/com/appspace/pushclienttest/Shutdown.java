package com.appspace.pushclienttest;

import java.io.File;

class Shutdown implements Runnable{
	FileUtil fileutil = new FileUtil();
	int nClientCount = 0;
	int nCurrentMaxClient = 0;
	Shutdown(int nMaxClient){
		nCurrentMaxClient = nMaxClient;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("system down");
		File file = new File(".", Constant.MQTT_DATA_STORE_FOLDER);
		
		try {
			nClientCount = fileutil.getCount();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			System.out.println( "Count 파일 없음."); 	
		}
		
		if( nClientCount - nCurrentMaxClient <= 0 ){
			deleteDirectory(file);
		}
		else{
			fileutil.setCountFile(nClientCount - nCurrentMaxClient);
		}
	}
	
	public boolean deleteDirectory(File path) {
        if(!path.exists()) {
            return false;
        }
         
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
         
        return path.delete();
    }
}
