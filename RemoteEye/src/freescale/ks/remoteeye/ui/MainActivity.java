package freescale.ks.remoteeye.ui;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import freescale.ks.remoteeye.application.RemoteEyeApplication;
import freescale.ks.remoteeye.service.CommendServer;
import freescale.ks.remoteeye.streaming.Session;
import freescale.ks.remoteeye.streaming.SessionBuilder;
import freescale.ks.remoteeye.streaming.audio.AudioQuality;
import freescale.ks.remoteeye.streaming.rtsp.RtspClient;
import freescale.ks.remoteeye.streaming.surfaceview.SurfaceView;
import freescale.remoteeye.R;

public class MainActivity extends Activity {
	
	private TextView textViewTime;
	private TextView textViewNetState;
	private TextView textViewBattery;
	private TextView textViewBitrate;
	private TextView textViewVLCAddress;
	private SurfaceView surfaceViewVideoShow;
	private BroadcastReceiver mReceiver;
	private IntentFilter intentfilter;
	private IntentFilter intentfilter_global;
	private BroadcastReceiver mReceiver_global;

	private PowerManager.WakeLock mWakeLock;
	private final String TAG="MainActivity";
	private final int notificationID=0x007;
	private RemoteEyeApplication myApplicatioon;
	private final int ActivityForResultID=0x008;
	
	protected RtspClient mClient;
	protected Session mSession;
	
	private Thread timeThread=null;
	private boolean isTimeThreadRuning=false;
	 private Handler mHandler =null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getWindow().setFlags(BIND_ADJUST_WITH_ACTIVITY, BIND_ADJUST_WITH_ACTIVITY);
		setContentView(R.layout.remoteeye_mainlayout);
		findViewsAndInitOthers();
		registCommendBroadcastReceiver();
		registGlobalBroadcastReceiver();
	}

	
	
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		
	}




	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "...........onDestroy()............");
		unregistCommendBroadcastReceiver();
		 unregistGlobalBroadcastReceiver();
		super.onDestroy();
	
	}








	private void removeNotification(int IntNotificationID) {
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(IntNotificationID);
	}
	
	private void setUpNotification(int IntNotificationID)
	{
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		Notification notification = builder.setContentIntent(pendingIntent)
				.setWhen(System.currentTimeMillis())
				.setTicker(getText(R.string.notification_title))
				.setSmallIcon(R.drawable.remoteeye2)
				.setContentTitle(getText(R.string.notification_title))
				.setContentText(getText(R.string.notification_content)).build();
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(IntNotificationID,notification);
		
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		upDateRemoteEyeApplicationInfo();
		// Lock screen
				mWakeLock.acquire();

				// Did the user disabled the notification ?
				if (myApplicatioon.notificationEnabled) {
					setUpNotification(notificationID);
					
				} else {
					removeNotification(notificationID);
				}
				
				Log.e(TAG, ".....onStart().....");
				
				startTimeThread();
				
				startService(new Intent(this,CommendServer.class ));
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mWakeLock.isHeld())
			mWakeLock.release();
		
		Log.e(TAG, ".....onStop().....");
		stopTimeThread();
		/*try {
			timeThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		closeClientAndSession();
		stopService(new Intent(this,CommendServer.class ));
		 
	}




	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * 这个方法会在onstart（）之前调用
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		/*if(requestCode==ActivityForResultID)
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			myApplicatioon.setTheInfoFromPerfence(settings);
			
			
		   stopService(new Intent(this,CommendServer.class));
		 
			startService(new Intent(this,CommendServer.class));
			
		}*/
			
			super.onActivityResult(requestCode, resultCode, data);
	}

private void upDateRemoteEyeApplicationInfo()
{
	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	myApplicatioon.setTheInfoFromPerfence(settings);
}

	private void setTimeStateDescription(String time)
	{
		textViewTime.setText(time);
		
	}
	private void setBitrateDescription(String bitrate)
	{
		textViewBitrate.setText("Speed:"+((Integer.parseInt(bitrate)/8)/1024)+"K/s");
		
	}
	private void setBatteryStateDescription(int batteryStat)
	{
		textViewBattery.setText("Battery:"+batteryStat+"%");
		
	}
	private void setNetStateDescription(String netstat)
	{
		textViewNetState.setText("NET:"+netstat);
		
	}
	private void setStateDescription(String states)
	{
		int state=Integer.parseInt(states);
		switch (state)
		{
		default:
			return;

		case 0 : // '\0'
			textViewVLCAddress.setText("Disconnect From Server!");
			return;

		case 2: // '\002'
			Object aobj[] = new Object[2];
			aobj[0] = myApplicatioon.serverIP+":"+myApplicatioon.serverPort;
			aobj[1] = myApplicatioon.deviceID;
			textViewVLCAddress.setText(String.format("Open VLC and key in the address:\nrtsp://%s/%s.sdp", aobj));
			return;

		case 1: // '\001'
			textViewVLCAddress.setText("Connecting server...");
			return;
		}
	}

	private void findViewsAndInitOthers()
	{
		myApplicatioon=RemoteEyeApplication.getInstance();
		textViewTime=(TextView) findViewById(R.id.textView_Time);
		textViewNetState=(TextView) findViewById(R.id.textView_NetState);
		textViewBattery=(TextView) findViewById(R.id.textView_Battery);
		textViewBitrate=(TextView) findViewById(R.id.textView_BitRate);
		textViewVLCAddress=(TextView) findViewById(R.id.textView_PlayAddress);
		surfaceViewVideoShow=(SurfaceView) findViewById(R.id.surfaceView_VideoShow);
		
		// Prevents the phone from going to sleep mode
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
		
		intentfilter= new IntentFilter();
		//intentfilter.addAction(myApplicatioon.intentFilterInterentChange);
		//intentfilter.addAction(myApplicatioon.intentFilterBatteryChange);
		intentfilter.addAction(myApplicatioon.intentFilterStateChange);
		intentfilter.addAction(myApplicatioon.intentFilterPlay);
		intentfilter.addAction(myApplicatioon.intentFilterStop);
		
		
		intentfilter_global=new IntentFilter();
		intentfilter_global.addAction(myApplicatioon.intentFilterInterentChange);
		intentfilter_global.addAction(myApplicatioon.intentFilterBatteryChange);
		//intentfilter_global.addAction(myApplicatioon.intentFilterTimeChange);
		
		mReceiver=new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String actionString=intent.getAction();
				if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterStateChange))
				{
					Log.e(TAG, "...........intentFilterStateChange............");
					setStateDescription(intent.getStringExtra(myApplicatioon.intentFilterStateChangeKey));
					
				}
			/*	if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterInterentChange))
				{
					Log.e(TAG, "...........intentFilterInterentChange............");
					phoneNetworkConnections();
				}
				*/
				if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterPlay))
				{
					Log.e(TAG, "...........intentFilterPlay............");
					String s1 = intent.getStringExtra("location");
					if (!TextUtils.isEmpty(s1))
							{
							String as[] = s1.split(":");
							if (as != null && as.length == 2)
							{
								String s2 = as[0].trim();
								int i = Integer.parseInt(as[1]);
								
									if (mSession == null)
									{
										
										mSession = SessionBuilder.getInstance()
												.setContext(getApplicationContext())
												.setAudioEncoder(!myApplicatioon.isAudioEncoder?0:myApplicatioon.audioEncoder)
												.setVideoEncoder(!myApplicatioon.isVideoEncoder?0:myApplicatioon.videoEncoder)   
												.setVideoQuality(RemoteEyeApplication.videoQuality)
												.setAudioQuality(myApplicatioon.audioQuality)  
												.setOrigin("127.0.0.0")
												.setDestination(myApplicatioon.serverIP)
												.setSurfaceView(surfaceViewVideoShow)
												.setPreviewOrientation(90)
												.build();
									}
									if (mClient == null)
									{
										mClient = new RtspClient();
										if (myApplicatioon.transmittalMode.equalsIgnoreCase(myApplicatioon.UDP))
											mClient.setTransportMode(0);
										else
											mClient.setTransportMode(1);
										mClient.setSession(mSession);
										
									}
									mClient.setCredentials("", "");
									mClient.setServerAddress(s2, i);
									mClient.setStreamPath(intent.getStringExtra("path"));
									mClient.startStream();
							}
						}
				}
				if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterStop))
				{
					Log.e(TAG, "...........intentFilterStop............");
					if (mClient != null)
					{
						mClient.release();
						mClient = null;
					}
					if (mSession != null)
					{
						mSession.release();
						mSession = null;
					}
				}
				/*
				if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterBatteryChange))
				{
					Log.e(TAG, "...........intentFilterBatteryChange............");
					setBatteryStateDescription(intent.getIntExtra("level", 0));
				}
				*/
			}
		};
		
		mReceiver_global=new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String actionString=intent.getAction();
				if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterInterentChange))
				{
					Log.e(TAG, "...........intentFilterInterentChange............");
					phoneNetworkConnections();
				}
				if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterBatteryChange))
				{
					Log.e(TAG, "...........intentFilterBatteryChange............");
					setBatteryStateDescription(intent.getIntExtra("level", 0));
				}
				/*if(actionString.equalsIgnoreCase(myApplicatioon.intentFilterTimeChange))
				{
					Log.e(TAG, "........intentFilterTimeChange.........");
					SimpleDateFormat    sDateFormat =  new  SimpleDateFormat("yyyy/MM/dd hh:mm:ss");       
					String    date    =    sDateFormat.format(new    java.util.Date()); 
					setTimeStateDescription(date);
				}
				*/
			}
		};
		
		
		mHandler= new Handler() {
			          @Override
			          public void handleMessage (Message msg) {
			             super.handleMessage(msg);
			              switch (msg.what) {
			                  case 1:
			                	  setTimeStateDescription((String)msg.obj);
			                     break;
			                  case 2:
			                	  setBitrateDescription((String)msg.obj);
			                	  break;
			                 default:
			                     break;
			             }
			         }
			     };

		
		
		
	}
	private void closeClientAndSession()
	{
		if (mClient != null)
		{
			mClient.release();
			mClient = null;
		}
		if (mSession != null)
		{
			mSession.release();
			mSession = null;
		}
	}
	
	private void startTimeThread()
	{
		isTimeThreadRuning=true;
		timeThread=new Thread()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(!Thread.interrupted()&&isTimeThreadRuning)
				{
					SimpleDateFormat    sDateFormat =  new  SimpleDateFormat("yyyy/MM/dd hh:mm:ss");       
					String    date    =    sDateFormat.format(new    java.util.Date()); 
					Message msg=new Message();
					msg.what=1;
					msg.obj=date;
					mHandler.sendMessage(msg);
					if(mSession!=null)
					{
						Message msg2=new Message();
				    long bitrate=mSession.getBitrate();
				    msg2.what=2;
					msg2.obj=bitrate+"";
					mHandler.sendMessage(msg2);
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		};
		timeThread.start();
	}
	private void stopTimeThread()
	{
		isTimeThreadRuning=false;
		Thread tempthread=timeThread;
		timeThread=null;
		if(tempthread!=null)
		{
			tempthread.interrupt();
			tempthread=null;
		/*try {
			timeThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		}
	}
	
	
	private void phoneNetworkConnections()
	{
		ConnectivityManager connectMgr = (ConnectivityManager)myApplicatioon.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
		NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected())
		{ 
		
		  // no net
			Log.e(TAG, "there is no net connection");
			
			setNetStateDescription(" NO!");
		}
		else 
		   if(mobNetInfo.isConnected() && !wifiNetInfo.isConnected())
			{ 
			   // 2G or 3G
			   Log.e(TAG, "2G or 3G is connecting");
			   setNetStateDescription(" 2G/3G");
			    
			} 
			else
				if(!mobNetInfo.isConnected() && wifiNetInfo.isConnected())
				{
					//wifi 
					 Log.e(TAG, "wifi is connecting");
					   setNetStateDescription(" WIFI");
					   
					   
						
				}
				else
					if(mobNetInfo.isConnected() && wifiNetInfo.isConnected())
				{
				   //2g/3g/wifi 
					Log.e(TAG, "wifi is connecting");
					 setNetStateDescription(" WIFI&2G/3G");
						
			    }
	}
	
	/**
	 * 注意LocalBroadcastManager只能发送和接收自己的广播，系统级别的是不能接收和发送的
	 * 系统级别的使用registerReceiver
	 */
	private void registGlobalBroadcastReceiver()
	{
		if(mReceiver_global!=null)
		{
			registerReceiver(mReceiver_global, intentfilter_global);
		}
		
	}
	private void unregistGlobalBroadcastReceiver()
	{
		if (mReceiver_global != null)
		{
			unregisterReceiver(mReceiver_global);
			mReceiver_global = null;
		}
	}
	
	private void registCommendBroadcastReceiver()
	{
		if(mReceiver!=null)
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentfilter);
	}
	private void unregistCommendBroadcastReceiver()
	{
		if (mReceiver != null)
		{
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}
	
	@Override    
	public void onBackPressed() {
		 qiutRemoteEye();
		/*Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);*/
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.options), MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.quit), MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.options:
			
			openSharePreferenceActivity();
			return true;
		case R.id.quit:
			
			qiutRemoteEye();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		//return super.onMenuItemSelected(featureId, item);
	}

	private void qiutRemoteEye()
	{
		removeNotification(notificationID);
		//myApplicatioon.unregisterBatteryInfoReceiver();
		//this.onStop();
		this.onStop();
		this.onDestroy();
		this.finish();
		 System.exit(0);
		 
	}
	
	private void openSharePreferenceActivity()
	{
		
		Intent sharepreferenceIntent=new Intent();
		sharepreferenceIntent.setClass(MainActivity.this, PreferencesActivity.class);
		startActivityForResult(sharepreferenceIntent, ActivityForResultID);
		
	}




	



	
}
