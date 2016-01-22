package freescale.ks.remoteeye.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import freescale.ks.remoteeye.application.RemoteEyeApplication;
import freescale.ks.remoteeye.streaming.mp4info.MP4Config;

public class CommendServerThread extends Thread {
	
	private String serverIP;
	private int serverPort;
	
	
	
	private SocketChannel socketChannel=null;
	 private Selector selector=null;
	 
	 private Socket socketClient;
	 private Thread keepliveThread;
	 private OutputStream ops;
	 private RemoteEyeApplication mApplication=RemoteEyeApplication.getInstance();
	 private String deviceID;
	 private String TAG="CommendServerThread";
	 private boolean isRunning=true;
	 private ArrayBlockingQueue<String> mRequestQueue;
	 private String keepLiveString;
	 private String mUri;
	 private ReceiveStringParseUtil receiveUtil;
	
	 
	 
	 
	 @Override
	public void run() {
		// TODO Auto-generated method stub
		 super.run();
		Log.e(TAG, ".........CommendServerThread.run().........");
		serverIP=mApplication.serverIP;
		serverPort=Integer.parseInt(mApplication.serverPort);
		deviceID=mApplication.deviceID;
		mRequestQueue=new ArrayBlockingQueue<String>(20);
		
		 InetSocketAddress inetsocketaddress = new InetSocketAddress(serverIP,serverPort);
		
		//正在连接
		sendBroadcastToMainActivity(mApplication.intentFilterStateChange,
                mApplication.intentFilterStateChangeKey,
                             mApplication.connectting
                   );
		
        
		
		
		try
		{
			// socketChannel=SocketChannel.open(new InetSocketAddress(serverIP,serverPort));
			   
			    selector = Selector.open();
	            socketChannel = SocketChannel.open();  
	            socketChannel.socket().setReuseAddress(true); 
	            socketChannel.configureBlocking(false);
	            socketChannel.connect(inetsocketaddress);
	            socketChannel.register(selector,SelectionKey.OP_CONNECT);
	            //socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	           
		}
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			cleanConnection();
			return;
		
		} 
		
		
		
	
		 
	
		
		 
		 
		 
		    mUri = (new StringBuilder("rtsp://")).append(serverIP).append(":").append(serverPort).append("/").append(deviceID).append(".sdp").toString();
		    keepLiveString = (new StringBuilder("OPTIONS ")).append(mUri).append(" RTSP/1.0\r\n").append("CSeq: ").append(2).append("\r\n\r\n").toString();
		    mRequestQueue.offer(String.format("OPTIONS %s RTSP/1.0\r\nCSeq:1\r\nx-msg-type: DEV_MSG\r\nx-dev-id: %s\r\n\r\n", mUri,deviceID));
			
	
		    
		 
	
		   try {
			      while (!Thread.interrupted() && isRunning ) {
			    	  //Log.e(TAG, "......while (!Thread.interrupted() && isRunning && selector.select() > 0)......");
			        // 遍历每个有可用IO操作Channel对应的SelectionKey
			    	  if(selector.select() > 0)
			    	  {
			          for (SelectionKey sk : selector.selectedKeys()) {
			        	 selector.selectedKeys().remove(sk);
			        	 if(!sk.isValid()){  
			                 continue;  
			             }  
			        
			        	 
			        	 
			        	  if (sk.isConnectable()) {
		                        SocketChannel client = (SocketChannel)sk.channel();

					        	 if (client.isConnectionPending()) {  
				                        client.finishConnect();   
				                    }  
					        	 //连接成功
			            			sendBroadcastToMainActivity(mApplication.intentFilterStateChange,
			            	                mApplication.intentFilterStateChangeKey,
			            	                             mApplication.connectionOK
			            	                   );
				                    client.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE); 
				                   // client.configureBlocking(false);
				                    
				                    
		                     
		                        
			        	  }
			        	  else
			          // 如果该SelectionKey对应的Channel中有可读的数据
			        	
			          if (sk.isReadable()) {
			        	 
			            // 使用NIO读取Channel中的数据  
			        	 
			           receiveUtil=ReceiveStringParseUtil.parseReader((SocketChannel)sk.channel());
			           int returnState= receiveUtil.status;
			           Log.e(TAG,"...."+returnState);
			           if(returnState==200)
			            {
			            	if(!TextUtils.isEmpty(receiveUtil.method))
			            	{
			            		if(receiveUtil.method.equalsIgnoreCase(mApplication.intentFilterPlay))
			            		{
			            			StringBuilder stringbuilder = new StringBuilder("RTSP/1.0 200 OK\r\nCseq: ");
			            			mRequestQueue.offer(stringbuilder.append((String)receiveUtil.headers.get("cseq")).append("\r\n\r\n").toString());
			            			
			            			
			            			Intent intent=new Intent(mApplication.intentFilterPlay);
			            			intent.putExtra("path", String.format("/%s.sdp", deviceID));
			            				
			            			Iterator<String> iterator = receiveUtil.headers.keySet().iterator();
			            			while(iterator.hasNext())
			            			{
			            				String s3 = (String)iterator.next();
			            				intent.putExtra(s3, (String)receiveUtil.headers.get(s3));
			            				iterator.remove();
			            			}
			            			LocalBroadcastManager.getInstance(mApplication).sendBroadcast(intent);
			            		}
			            		else
			            			if(receiveUtil.method.equalsIgnoreCase(mApplication.intentFilterStop))
			            			{
			            				StringBuilder stringbuilder = new StringBuilder("RTSP/1.0 200 OK\r\nCseq: ");
				            			mRequestQueue.offer(stringbuilder.append((String)receiveUtil.headers.get("cseq")).append("\r\n\r\n").toString());
				            			
			            				sendBroadcastToMainActivity(mApplication.intentFilterStop,mApplication.intentFilterStopKey,null);
			            				
			            				
			            			}
			            			else
			            			{
			            				StringBuilder stringbuilder = new StringBuilder("RTSP/1.0 400 STATUS_BAD_REQUEST\r\nCseq: ");
			            				mRequestQueue.offer(stringbuilder.append((String)receiveUtil.headers.get("cseq")).append("\r\n\r\n").toString());
			            				
			            			}
			            	}
			            	
			            }
			            else
			            {
			            	cleanConnection();
							isRunning=false;
							return;
			            	//this.interrupt();
			            	/*try {
								this.join();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
			            	
			            }
			            
			          
			          
			            // 为下一次读取作准备
			          
			           //sk.interestOps(SelectionKey.OP_READ |SelectionKey.OP_WRITE);
			            
				         
			           
			          }
			          else
			          if(sk.isWritable())
			          {
			        	  tryWriteOut((SocketChannel)sk.channel());
			        	  
			          }
			          // 删除正在处理的SelectionKey
			        // sk.interestOps(SelectionKey.OP_READ |SelectionKey.OP_WRITE);
			        
			   
			      }  
			       // selector.selectedKeys().clear();
			      }
			    }
			      //线程结束时，要给主界面发送结束命令
			        //sendBroadcastToMainActivity(mApplication.intentFilterStop,mApplication.intentFilterStopKey,null);
			       cleanConnection();
		   }catch (IOException ex) {
			      ex.printStackTrace();
			      Log.e(TAG, ".........ERRRRRRRRROOOORRRR...........");
			      cleanConnection();
			      return;
			    } 
		  
	}
	
	 private void cleanConnection()
	 {
	 	//Log.e(TAG, "Connect error!!!");
		 isRunning=false;
	 	sendBroadcastToMainActivity(mApplication.intentFilterStateChange,
	 			                  mApplication.intentFilterStateChangeKey,
	 			                               mApplication.noConnection
	                                  );
	 	sendBroadcastToMainActivity(mApplication.intentFilterStop,
	             mApplication.intentFilterStopKey,
	                                      null
	             );
	 	if(socketChannel!=null)
	 	{
	 		try {
	 			socketChannel.close();
	 			socketChannel=null;
	 		} catch (IOException e1) {
	 			// TODO Auto-generated catch block
	 			e1.printStackTrace();
	 			socketChannel=null;
	 		}
	 	}
	 	if (selector != null)
	 		try
	 		{
	 			selector.close();
	 			selector=null;
	 		}
	 		// Misplaced declaration of an exception variable
	 		catch (IOException ioexception9)
	 		{
	 			ioexception9.printStackTrace();
	 			selector=null;
	 		}
	 }

	 private long mLastWriteTime;
	 public void tryWriteOut(SocketChannel socketchannel)
	 		throws IOException
	 	{
	 		String s = (String)mRequestQueue.poll();
	 		if (s == null)
	 		{
	 			if (SystemClock.uptimeMillis() - mLastWriteTime < 15000L)
	 				return;
	 			s = keepLiveString;
	 		}
	 		ByteBuffer bytebuffer = ByteBuffer.wrap(s.getBytes());
	 		 while(true)
	 		{
	 			if (!isRunning || !bytebuffer.hasRemaining())
	 			{
	 				mLastWriteTime = SystemClock.uptimeMillis();
	 				return;
	 			}
	 			socketchannel.write(bytebuffer);
	 		}
	 	}
	 	


/*	 
 @Override
	public void run() {
		// TODO Auto-generated method stub
	 
	    serverIP=mApplication.serverIP;
		serverPort=Integer.parseInt(mApplication.serverPort);
		deviceID=mApplication.deviceID;
		mRequestQueue=new ArrayBlockingQueue<String>(20);
		
		//正在连接
		sendBroadcastToMainActivity(mApplication.intentFilterStateChange,
             mApplication.intentFilterStateChangeKey,
                          mApplication.connectting
                );
	 
		socketClient=new Socket();
		SocketAddress remoteAddress=new InetSocketAddress(serverIP, serverPort);
		try {
			socketClient.connect(remoteAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			cleanSocket();
			
		}
		//连接成功
			sendBroadcastToMainActivity(mApplication.intentFilterStateChange,
	                mApplication.intentFilterStateChangeKey,
	                             mApplication.connectionOK
	                   );
 
		 mUri = (new StringBuilder("rtsp://")).append(serverIP).append(":").append(serverPort).append("/").append(deviceID).append(".sdp").toString();
		 keepLiveString = (new StringBuilder("OPTIONS ")).append(mUri).append(" RTSP/1.0\r\n").append("CSeq: ").append(2).append("\r\n\r\n").toString();
		 mRequestQueue.offer(String.format("OPTIONS %s RTSP/1.0\r\nCSeq:1\r\nx-msg-type: DEV_MSG\r\nx-dev-id: %s\r\n\r\n", mUri,deviceID));
		 
		 try {
			ops = socketClient.getOutputStream();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	
		 keepliveThread=new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!Thread.interrupted()&& isRunning)
				{
					
					try {
						tryWriteOut(ops);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
					}
				}
				
			}
		});
		 keepliveThread.start();
		BufferedReader bufferedreader = null;
		try {
			bufferedreader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			cleanSocket();
		}	
		 while (!Thread.interrupted() && isRunning)
		 {
			 
			 
			 
			  try {
				receiveUtil=ReceiveStringParseUtil.parseReader(bufferedreader);
				int returnState= receiveUtil.status;
		           Log.e(TAG,"...."+returnState);
		           if(returnState==200)
		            {
		            	if(!TextUtils.isEmpty(receiveUtil.method))
		            	{
		            		if(receiveUtil.method.equalsIgnoreCase(mApplication.intentFilterPlay))
		            		{
		            			StringBuilder stringbuilder = new StringBuilder("RTSP/1.0 200 OK\r\nCseq: ");
		            			mRequestQueue.offer(stringbuilder.append((String)receiveUtil.headers.get("cseq")).append("\r\n\r\n").toString());
		            			
		            			
		            			Intent intent=new Intent(mApplication.intentFilterPlay);
		            			intent.putExtra("path", String.format("/%s.sdp", deviceID));
		            				
		            			Iterator<String> iterator = receiveUtil.headers.keySet().iterator();
		            			while(iterator.hasNext())
		            			{
		            				String s3 = (String)iterator.next();
		            				intent.putExtra(s3, (String)receiveUtil.headers.get(s3));
		            				iterator.remove();
		            			}
		            			LocalBroadcastManager.getInstance(mApplication).sendBroadcast(intent);
		            		}
		            		else
		            			if(receiveUtil.method.equalsIgnoreCase(mApplication.intentFilterStop))
		            			{
		            				StringBuilder stringbuilder = new StringBuilder("RTSP/1.0 200 OK\r\nCseq: ");
			            			mRequestQueue.offer(stringbuilder.append((String)receiveUtil.headers.get("cseq")).append("\r\n\r\n").toString());
			            			
		            				sendBroadcastToMainActivity(mApplication.intentFilterStop,mApplication.intentFilterStopKey,null);
		            				
		            				
		            			}
		            			else
		            			{
		            				StringBuilder stringbuilder = new StringBuilder("RTSP/1.0 400 STATUS_BAD_REQUEST\r\nCseq: ");
		            				mRequestQueue.offer(stringbuilder.append((String)receiveUtil.headers.get("cseq")).append("\r\n\r\n").toString());
		            				
		            			}
		            	}
		            	
		            }
		            else
		            {
		            	cleanSocket();
						isRunning=false;
		            	//this.interrupt();
		            
		            	
		            }
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				cleanSocket();
				e.printStackTrace();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				cleanSocket();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				cleanSocket();
				e.printStackTrace();
			}
	           
	            
		 }
		 
		 
			
	 
		super.run();
	}
 
 
 
 private void cleanSocket()
 {
	 sendBroadcastToMainActivity(mApplication.intentFilterStateChange,
              mApplication.intentFilterStateChangeKey,
                           mApplication.noConnection
                 );
	sendBroadcastToMainActivity(mApplication.intentFilterStop,
	mApplication.intentFilterStopKey,
	                     null
	);
	  isRunning=false;
	 if(socketClient!=null)
	 {
		 try {
			socketClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 socketClient=null;
	 }
 }
 
 
 
 
 public synchronized void  tryWriteOut(OutputStream outPutStream)
 		throws IOException
 	{
 		String s = (String)mRequestQueue.poll();
 		if (s == null)
 		{
 			s = keepLiveString;
 		}
 		
 			outPutStream.write(s.getBytes());
 		
 	}
 
 
 */
 
 
 
 
 
 
 
 
 
public void stopThread()
 {
	 isRunning=false;
	 cleanConnection();
	// cleanSocket();
	/* keepliveThread.interrupt();
	 try {
		keepliveThread.join();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
 }
 public void startThread()
 {
	 isRunning=true;
 }
 private void sendBroadcastToMainActivity(String actionCommend,String intentKey,String state)
 {
	     Intent intent = new Intent(actionCommend);
	     if(intentKey!=null && state!=null)
	     {
		 intent.putExtra(intentKey, state);
	     }
		 LocalBroadcastManager.getInstance(mApplication).sendBroadcast(intent);
	 
 }





	
}
