package freescale.ks.remoteeye.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CommendServer extends Service {
	
	private CommendServerThread serverRunningThread=null;
    private Thread temp=null;
	private String TAG="CommendServer";
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e(TAG, "......onCreate().........");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "....onDestroy()......");
		temp=serverRunningThread;
		serverRunningThread=null;
		if(temp!=null)
		{
			((CommendServerThread) temp).stopThread();
			temp.interrupt();
		/*	try
			{
				temp.join();
			}
			catch (InterruptedException interruptedexception)
			{
				interruptedexception.printStackTrace();
			}*/
			temp=null;
			
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e(TAG, ".........onStartCommand..........");
		if(serverRunningThread==null)
		{
			
		 serverRunningThread=new CommendServerThread();
		 serverRunningThread.startThread();
		 serverRunningThread.start();
		 Log.e(TAG, ".........serverRunningThread.start()..........");
		}
		
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	
	
	
	
	

}
