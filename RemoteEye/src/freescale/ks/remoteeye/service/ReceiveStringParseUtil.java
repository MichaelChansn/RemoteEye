package freescale.ks.remoteeye.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * 
 * @author ks
 *
 *
 *����ע��
 *������REDIRECT ��location����ʽ��Location: rtsp://192.168.2.33:554/f1.sdp������ʽ
 *����DSS���õ���Location: 192.168.2.33:554/f1.sdp����û��rtspͷ����ʽ 
 */
public class ReceiveStringParseUtil {


	public static final String STATUS_BAD_REQUEST = "400 Bad Request";
	public static final String STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";
	public static final String STATUS_NOT_FOUND = "404 Not Found";
	public static final String STATUS_OK = "200 OK";
	public static ByteBuffer buffer = ByteBuffer.allocate(1024);
	public static final Pattern regexMethod = Pattern.compile("(\\w+) (\\S+) RTSP", 2);
	public static final Pattern regexStatus = Pattern.compile("RTSP/\\d.\\d (\\d+) (\\w+)", 2);
	public static final Pattern rexegAuthenticate = Pattern.compile("realm=\"(.+)\",\\s+nonce=\"(\\w+)\"", 2);
	public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)", 2);
	public static final Pattern rexegSession = Pattern.compile("(\\d+)", 2);
	public static final Pattern rexegTransport = Pattern.compile("client_port=(\\d+)-(\\d+).+server_port=(\\d+)-(\\d+)", 2);
	public HashMap<String,String> headers;
	public String method=null;
	public int status;
	public String uri;

	private static final String TAG="ReceiveStringParseUtil";
	/**
	 * �����յ�������
	 * rtspЭ���Э��ͷ���һ���Ǹ����У���һ��ʹ��\r\n\r\n����
	 * �������жϽ�����ʱ�����readline()�õ����ַ�������һ��С��3����Ϊ�ǿյ�
	 */
	ReceiveStringParseUtil()
	{
		status = 200;
		headers = new HashMap<String,String>();
	}

	

	public static ReceiveStringParseUtil parseReader(SocketChannel socketchannel)
		throws IOException, IllegalStateException, SocketException
	{
		ReceiveStringParseUtil receiveunit;
		BufferedReader bufferedreader;
		String str;
		Matcher matcher;
		receiveunit = new ReceiveStringParseUtil();
		buffer.clear();
		socketchannel.read(buffer);
		buffer.flip();
		bufferedreader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit())));
		str = bufferedreader.readLine();
		if (str == null)
			throw new SocketException("Client disconnected");
		Log.e(TAG, str);
		matcher = regexMethod.matcher(str);
		
		if(!matcher.find())
		{
			Matcher matcher1 = regexStatus.matcher(str);
			if (!matcher1.find())
			{
				return receiveunit;
			}
			else
			{
				receiveunit.status = Integer.parseInt(matcher1.group(1));
				while(true)
				{
					String s1 = bufferedreader.readLine();
					if (s1 == null || s1.length() <= 3)
					{
						if (s1 == null)
							throw new SocketException("Client disconnected");
						break;
					}
					Log.d(TAG, s1);
					Matcher matcher2 = rexegHeader.matcher(s1);
					matcher2.find();
					receiveunit.headers.put(matcher2.group(1).toLowerCase(Locale.US), matcher2.group(2));
					
				}
				
			 return receiveunit;
			}
		}
		else
		{
			receiveunit.method = matcher.group(1);
			receiveunit.uri = matcher.group(2);
			while(true)
			{
				String s2 = bufferedreader.readLine();
				if (s2 == null || s2.length() <= 3)
				{
					if (s2 == null)
						throw new SocketException("Client disconnected");
					break;
				}
				Log.e("ReceiveUnit", s2);
				Matcher matcher3 = rexegHeader.matcher(s2);
				matcher3.find();
				
				receiveunit.headers.put(matcher3.group(1).toLowerCase(Locale.US), matcher3.group(2));
			} 
			return receiveunit;
			
		}
		
		
	}
	
	
	
	public static ReceiveStringParseUtil parseReader(BufferedReader bufferedreader)
			throws IOException, IllegalStateException, SocketException
		{
		
			ReceiveStringParseUtil receiveunit;
			String str;
			Matcher matcher;
			receiveunit = new ReceiveStringParseUtil();
			str = bufferedreader.readLine();
			if (str == null)
				throw new SocketException("Client disconnected");
			Log.e(TAG, str);
			matcher = regexMethod.matcher(str);
			
			if(!matcher.find())
			{
				Matcher matcher1 = regexStatus.matcher(str);
				if (!matcher1.find())
				{
					return receiveunit;
				}
				else
				{
					receiveunit.status = Integer.parseInt(matcher1.group(1));
					while(true)
					{
						String s1 = bufferedreader.readLine();
						if (s1 == null || s1.length() <= 3)
						{
							if (s1 == null)
								throw new SocketException("Client disconnected");
							break;
						}
						Log.d(TAG, s1);
						Matcher matcher2 = rexegHeader.matcher(s1);
						matcher2.find();
						receiveunit.headers.put(matcher2.group(1).toLowerCase(Locale.US), matcher2.group(2));
						
					}
					
				 return receiveunit;
				}
			}
			else
			{
				receiveunit.method = matcher.group(1);
				receiveunit.uri = matcher.group(2);
				while(true)
				{
					String s2 = bufferedreader.readLine();
					if (s2 == null || s2.length() <= 3)
					{
						if (s2 == null)
							throw new SocketException("Client disconnected");
						break;
					}
					Log.e("ReceiveUnit", s2);
					Matcher matcher3 = rexegHeader.matcher(s2);
					matcher3.find();
					
					receiveunit.headers.put(matcher3.group(1).toLowerCase(Locale.US), matcher3.group(2));
				} 
				return receiveunit;
			}
				
			
			
			
		}
	
}

