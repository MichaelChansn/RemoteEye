/*
 * Copyright (C) 2011-2013 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package freescale.ks.remoteeye.application;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;
import freescale.ks.remoteeye.streaming.SessionBuilder;
import freescale.ks.remoteeye.streaming.audio.AudioQuality;
import freescale.ks.remoteeye.streaming.video.VideoQuality;
import freescale.remoteeye.R;

/**
 * 
 * This class will be instantiated when the App is running,
 * And this is the first class this app instantiate.
 * 
 * 
 * @author ks
 *
 */
public class RemoteEyeApplication extends android.app.Application {

	public final static String TAG = "RemoteEyeApplication";
	
	/** Default quality of video streams. */
	public static VideoQuality videoQuality = new VideoQuality(320,240,20,500000);

	/** Default quality of audio streams. */
	public AudioQuality audioQuality=new AudioQuality(8000, 16000);
	/** By default AMR is the audio encoder. */
	public int audioEncoder = SessionBuilder.AUDIO_AAC;
	public boolean isAudioEncoder=false;

	/** By default H.263 is the video encoder. */
	public int videoEncoder = SessionBuilder.VIDEO_H264;

	public boolean isVideoEncoder=false;
	

	/** If the notification is enabled in the status bar of the phone. */
	public boolean notificationEnabled = true;


	/** Contains an approximation of the battery level. */
	public int batteryLevel = 0;
	
	private static RemoteEyeApplication sApplication;
	//public String[] strDeviceResolutions;
	//public String[] strDeviceResolutionsValues;
    private final String sharePreID="RemoteEye";
    private final String DeviceResolutionsNum="DeviceResolutionsNum";
    private final String DeviceResolutions="DeviceResolutions";
    private final String DeviceResolutionsValues="DeviceResolutionsValues";
    private final String DEVICE_ID=android.os.Build.MODEL.replace(" ", "_");
    private final int Defaultvideo_resX=320;
    private final int Defaultvideo_resY=240;
    
    public final String UDP="0";
    public final String TCP="1";
    public String serverIP="42.96.205.175";
    public String serverPort="554";
    public String deviceID=android.os.Build.MODEL.replace(" ", "_");
    public String transmittalMode=UDP;
    
    
    //设置程序使用的广播信息
    public final String intentFilterBatteryChange=Intent.ACTION_BATTERY_CHANGED;
    public final String intentFilterStateChange="action_state_change";
    public final String intentFilterInterentChange=ConnectivityManager.CONNECTIVITY_ACTION;
    public final String intentFilterTimeChange="android.intent.action.TIME_TICK";
    public final String intentFilterPlay="REDIRECT";
    public final String intentFilterStop="PAUSE";
    public final String intentFilterStateChangeKey="KEY_STATE_CHANGE";
    public final String noConnection="0";
    public final String connectting="1";
    public final String connectionOK="2";
    public final String intentFilterPlayKey="KEY_PLAY";
    public final String intentFilterStopKey="KEY_STOP";
	@Override
	public void onCreate() {

		// The following line triggers the initialization of ACRA
		// Please do not uncomment this line unless you change the form id or I will receive your crash reports !
		//ACRA.init(this);

		sApplication = this;
		Log.e(TAG, "create application");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	//	initDeviceResolution(settings);
		setTheInfoFromPerfence(settings);
		//registerBatteryInfoReceiver();
		super.onCreate();

	}

	public static RemoteEyeApplication getInstance() {
		if(sApplication==null)
			sApplication=new RemoteEyeApplication();
		
		return sApplication;
	}

	

	/*public void unregisterBatteryInfoReceiver()
	{
		unregisterReceiver(mBatteryInfoReceiver);
	}
	public void registerBatteryInfoReceiver()
	{
		registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			batteryLevel = intent.getIntExtra("level", 0);//电量百分比level就是百分数%之前的数字
		}
	};*/
	
	/*
	private void initDeviceResolution(SharedPreferences settings )
	{
		getDeviceResolution(settings);
		loadDeviceResolutionsFromPreference(settings);
	}
	*/
	public  void setTheInfoFromPerfence(SharedPreferences settings )
	{
		//PreferenceManager.setDefaultValues(this,R.xml.globalpreference, false);
		
		
		notificationEnabled = settings.getBoolean("key_notification_enabled", true);
		serverIP=settings.getString("key_server_address", serverIP);
		serverPort=settings.getString("key_server_port", serverPort);
		deviceID=settings.getString("key_device_id", deviceID);
		transmittalMode=settings.getString("key_transport_mode", transmittalMode);
		
		
		
		
		// On android 3.* AAC ADTS is not supported so we set the default encoder to AMR-NB, on android 4.* AAC is the default encoder
		audioEncoder = (android.os.Build.VERSION.SDK_INT<14) ? SessionBuilder.AUDIO_AMRNB : SessionBuilder.AUDIO_AAC;
		audioEncoder = Integer.parseInt(settings.getString("key_audio_encoder", String.valueOf(audioEncoder)));
		videoEncoder= SessionBuilder.VIDEO_H264;
		videoEncoder = Integer.parseInt(settings.getString("key_video_encoder", String.valueOf(videoEncoder)));

		//设置视频分辨率
		//序号从1开始
		int video_resX;
		int video_resY;
		int videoResolution=Integer.parseInt(settings.getString("key_video_resolution", "3"));
	
		switch (videoResolution) {
		case 1://144x176
			
			video_resX=176;
			video_resY=144;
			break;
		case 2:
			video_resX=240;
			video_resY=160;	
			break;
		case 3:
			video_resX=320;
			video_resY=240;
			break;
		case 4:
			video_resX=352;
			video_resY=288;
			break;
		case 5:
			video_resX=480;
			video_resY=320;
			break;
		case 6:
			video_resX=640;
			video_resY=480;
			break;
		case 7:
			video_resX=720;
			video_resY=480;
			break;
		case 8:
			
			video_resX=800;
			video_resY=480;
			break;
	

		default:
			video_resX=Defaultvideo_resX;
			video_resY=Defaultvideo_resY;
			break;
		}
		
		/*if(videoResolution==0)
		{
			video_resX=Defaultvideo_resX;
			video_resY=Defaultvideo_resY;
		}
		else
		{
		Pattern pattern = Pattern.compile("([0-9]+)x([0-9]+)");
		Matcher matcher = pattern.matcher(strDeviceResolutions[videoResolution-1]);
		matcher.find();
		video_resX=Integer.parseInt(matcher.group(2));
		video_resY= Integer.parseInt(matcher.group(1));
		}
		*/
		// Read video quality settings from the preferences 
		videoQuality.resX=video_resX;
		videoQuality.resY=video_resY;
		videoQuality.framerate=Integer.parseInt(settings.getString("key_video_framerate", String.valueOf(videoQuality.framerate)));
		videoQuality.bitrate=Integer.parseInt(settings.getString("key_video_bitrate", String.valueOf(videoQuality.bitrate/1000)))*1000;
		
		
		/*
		videoQuality= new VideoQuality(
				         video_resX,
				         video_resY, 
						Integer.parseInt(settings.getString("key_video_framerate", String.valueOf(videoQuality.framerate))), 
						Integer.parseInt(settings.getString("key_video_bitrate", String.valueOf(videoQuality.bitrate/1000)))*1000);

		*/
		
		isAudioEncoder=settings.getBoolean("key_stream_audio", true);
		isVideoEncoder=settings.getBoolean("key_stream_video", false);
		SessionBuilder.getInstance() 
		.setContext(getApplicationContext())
		.setAudioEncoder(!isAudioEncoder?0:audioEncoder)
		.setVideoEncoder(!isVideoEncoder?0:videoEncoder)
		.setVideoQuality(videoQuality);

	
		// Listens to changes of preferences
/*		settings.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
*/
		
		
	}
	/**
	 * 动态得到手机支持的分辨率
	 * 只运行一次
	 */
	
	/*
	public void getDeviceResolution(SharedPreferences settings)
	{
	
	Log.e(TAG, "run in getDeviceResolution()");
		boolean isfirstrun=true;
		isfirstrun=settings.getBoolean("key_isfirstrun", true);
		
		if(isfirstrun)
		{
			isfirstrun=false;
			int j=0;
			Camera camera = Camera.open(0); 
			Parameters parameters = camera.getParameters(); 
			List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
			camera.release();
			
			
			int sizeNum=supportedPreviewSizes.size();
			strDeviceResolutions=new String[sizeNum];
			strDeviceResolutionsValues=new String[sizeNum];
			Editor editor = settings.edit(); //编辑文件 
			editor.putBoolean("key_isfirstrun", false);
			editor.putInt(DeviceResolutionsNum, sizeNum);
			editor.putString("key_device_id", DEVICE_ID);
			for(int i=0;i<sizeNum;i++)
			{
				strDeviceResolutions[i]=supportedPreviewSizes.get(i).height+"x"+supportedPreviewSizes.get(i).width;
				strDeviceResolutionsValues[i]=String.valueOf(i+1);
				editor.putString(DeviceResolutions+String.valueOf(i+1), strDeviceResolutions[i]);
				editor.putString(DeviceResolutionsValues+String.valueOf(i+1), strDeviceResolutionsValues[i]);
			}
			 
			Log.e(TAG,strDeviceResolutions.toString());
			Log.e(TAG,"this funtion only run for once");
			
			
			editor.commit();
		}
		
		
	}
	
	private void loadDeviceResolutionsFromPreference(SharedPreferences settings)
	{
		int deviceResolutionsNum=settings.getInt(DeviceResolutionsNum, 0);
		if(deviceResolutionsNum==0)
		{
			  System.exit(0);
			  return;
		}
		strDeviceResolutions=new String[deviceResolutionsNum];
		strDeviceResolutionsValues=new String[deviceResolutionsNum];
		for(int i=0;i<deviceResolutionsNum;i++)
		{
			strDeviceResolutions[i]=settings.getString(DeviceResolutions+String.valueOf(i+1), "320x240");
			strDeviceResolutionsValues[i]=settings.getString(DeviceResolutionsValues+String.valueOf(i+1),String.valueOf(i+1));
		}
	}
	*/
	
	

}
