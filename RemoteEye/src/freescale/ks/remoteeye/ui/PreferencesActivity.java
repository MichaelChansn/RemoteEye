package freescale.ks.remoteeye.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.MenuItem;
import freescale.ks.remoteeye.application.RemoteEyeApplication;
import freescale.remoteeye.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class PreferencesActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	private final String SERVER_ADDRESS="42.96.205.175";//默认服务器IP
	private final String SERVER_PORT="554";//默认端口号
	private final String DEVICE_ID=android.os.Build.MODEL.replace(" ", "_");//得到手机型号默认的ID
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
	
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		addPreferencesFromResource(R.xml.globalpreference);
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		
	
		
		//设置服务器相关的IP和端口，传送方式
		final EditTextPreference serveraddress=(EditTextPreference) findPreference("key_server_address");
		final EditTextPreference serverport=(EditTextPreference) findPreference("key_server_port");
		final EditTextPreference machineID=(EditTextPreference) findPreference("key_device_id");
		final ListPreference     transmittalmode = (ListPreference) findPreference("key_transport_mode");
		
		//设置audio参数
		final CheckBoxPreference audioEnabled = (CheckBoxPreference)findPreference("key_stream_audio");
		final ListPreference     audioEncoder = (ListPreference) findPreference("key_audio_encoder");
		
		//设置vedio参数
		final CheckBoxPreference videoEnabled = (CheckBoxPreference)findPreference("key_stream_video");
		final ListPreference     videoEncoder = (ListPreference) findPreference("key_video_encoder");
		final ListPreference     videoResolution = (ListPreference) findPreference("key_video_resolution");
		final ListPreference     videoBitrate = (ListPreference) findPreference("key_video_bitrate");
		final ListPreference     videoFramerate = (ListPreference) findPreference("key_video_framerate");
		//设置通知使能
		final CheckBoxPreference notificationEnable=(CheckBoxPreference)findPreference("key_notification_enabled");
		
		
	
		
		//显示动态获取手机的分辨率
		//videoResolution.setEntries(RemoteEyeApplication.getInstance().strDeviceResolutions);
		//videoResolution.setEntryValues(RemoteEyeApplication.getInstance().strDeviceResolutionsValues);
		
		//设置各个prefernce的summarry
		serveraddress.setSummary(settings.getString("key_server_address",SERVER_ADDRESS));
		serverport.setSummary(settings.getString("key_server_port", SERVER_PORT));
		machineID.setSummary(settings.getString("key_device_id", DEVICE_ID));
		transmittalmode.setSummary(transmittalmode.getEntries()[transmittalmode.findIndexOfValue(settings.getString("key_transport_mode", "0"))]);
		audioEnabled.setSummaryOn("Enable the audio streaming!");
		audioEnabled.setSummaryOff("Disable the audio streaming!");
		audioEncoder.setSummary(audioEncoder.getEntries()[audioEncoder.findIndexOfValue(settings.getString("key_audio_encoder", "3"))]);
		videoEnabled.setSummaryOn("Enable the video streaming!");
		videoEnabled.setSummaryOff("Disable the video streaming!");
		videoEncoder.setSummary(videoEncoder.getEntries()[videoEncoder.findIndexOfValue(settings.getString("key_video_encoder", "1"))]);
		videoResolution.setSummary(videoResolution.getEntries()[videoResolution.findIndexOfValue(settings.getString("key_video_resolution", "3"))]);
		videoBitrate.setSummary(videoBitrate.getEntries()[videoBitrate.findIndexOfValue(settings.getString("key_video_bitrate", "500"))]);
		videoFramerate.setSummary(videoFramerate.getEntries()[videoFramerate.findIndexOfValue(settings.getString("key_video_framerate", "20"))]);
		notificationEnable.setSummaryOn("The notification is Enabled!");
		notificationEnable.setSummaryOff("The notification is Disabled!");
		
		
		
		/**
		 * 判断初始状态，使能各个功能
		 */
		
		if(settings.getBoolean(audioEnabled.getKey(),true))
		{
			audioEncoder.setEnabled(true);
		}
		else
		{
			audioEncoder.setEnabled(false);
		}
		if(settings.getBoolean(videoEnabled.getKey(), true))
		{
			boolean state=true;
			videoEncoder.setEnabled(state);
			videoResolution.setEnabled(state);
			videoBitrate.setEnabled(state);
			videoFramerate.setEnabled(state);
		}
		else
		{
			boolean state=false;
			videoEncoder.setEnabled(state);
			videoResolution.setEnabled(state);
			videoBitrate.setEnabled(state);
			videoFramerate.setEnabled(state);
			
		}
		
		
		/**
		 * 设置监听器，用来在设置后刷新summary，设置之后android会自动保存数据到preference但是不会刷新summary，所以要手动刷新
		 */
		serveraddress.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				String str=newValue.toString();
				str=str.replace(" ", "");//不能包含空格
				
				Editor editor = settings.edit();
				editor.putString(preference.getKey(), str);
				editor.commit();
				serveraddress.setSummary(str);
				return false;
			}
		});
		
		serverport.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				String str=newValue.toString();
				str=str.replace(" ", "");//不能包含空格
				
				Editor editor = settings.edit();
				editor.putString(preference.getKey(), str);
				editor.commit();
				serverport.setSummary(str);
				
				return false;
			}
		});
		
		machineID.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				String str=newValue.toString();
				str=str.replace(" ", "");//不能包含空格
				
				Editor editor = settings.edit();
				editor.putString(preference.getKey(), str);
				editor.commit();
				machineID.setSummary(str);
				return false;//返回true会自动更改到preference，此时sditor无效，使用editor时要设置为false
			}
		});
		
		transmittalmode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				//transmittalmode.setSummary(newValue.toString().equalsIgnoreCase("0")? "TCP":"UDP");
				transmittalmode.setSummary(transmittalmode.getEntries()[transmittalmode.findIndexOfValue(newValue.toString())]);
				return true;
			}
		});
		
		
		audioEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				boolean state = (Boolean)newValue;
				audioEncoder.setEnabled(state);
				return true;
			}
		});
		
		audioEncoder.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub

				//audioEncoder.setSummary(newValue.toString().equalsIgnoreCase("3")? audioEncoder.getEntries()[0]:audioEncoder.getEntries()[1]);
				audioEncoder.setSummary(audioEncoder.getEntries()[audioEncoder.findIndexOfValue(newValue.toString())]);
				
				return true;
			}
		});
		
		videoEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub

				boolean state = (Boolean)newValue;
				videoEncoder.setEnabled(state);
				videoResolution.setEnabled(state);
				videoBitrate.setEnabled(state);
				videoFramerate.setEnabled(state);
				return true;
			}
		});
		
		videoEncoder.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				videoEncoder.setSummary(videoEncoder.getEntries()[videoEncoder.findIndexOfValue(newValue.toString())]);
				
				return true;
			}
		});
		
		videoResolution.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				//videoEncoder.setSummary(videoEncoder.getEntries()[videoEncoder.findIndexOfValue(newValue.toString())]);
				videoResolution.setSummary(videoResolution.getEntries()[videoResolution.findIndexOfValue(newValue.toString())]);
				
				return true;
			}
		});
		
		videoBitrate.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				//videoEncoder.setSummary(videoEncoder.getEntries()[videoEncoder.findIndexOfValue(newValue.toString())]);
				videoBitrate.setSummary(videoBitrate.getEntries()[videoBitrate.findIndexOfValue(newValue.toString())]);
				
				return true;
			}
		});
		videoFramerate.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				//videoEncoder.setSummary(videoEncoder.getEntries()[videoEncoder.findIndexOfValue(newValue.toString())]);
				videoFramerate.setSummary(videoFramerate.getEntries()[videoFramerate.findIndexOfValue(newValue.toString())]);
				
				return true;
			}
		});
		
		
		
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//setResult(resultCode, data)
	}
	
	
	
	
	
	

}
