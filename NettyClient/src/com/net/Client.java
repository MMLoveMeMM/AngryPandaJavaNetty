package com.net;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class Client extends Thread implements INetListener<String>{

	public static final int READ_TIMEOUT_SECONDS = 60;
    public static final int WRITE_TIMEOUT_SECONDS = 60;
    public static final int BOTH_TIMEOUT_SECONDS = 60;
    
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8009;
    
	private NioEventLoopGroup workGroup;
	private Bootstrap bootstrap;
	public Channel channel;
	private IOClientHandler mIOClientHandler;
	
	private int mStatus = MachineState.NET_STATUS_IDLE;
	
	private int mCount=0;
	
	public Client(String host,int port) {
		
	}
	
	private boolean init() {
		
		workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);// 双核
		bootstrap = new Bootstrap();
		mIOClientHandler = new IOClientHandler(this);
		
		try {
			bootstrap
	        .group(workGroup)   //这种属于 单线程Reactor模式；boss与 work 共用一个线程池；
	        .channel(NioSocketChannel.class)
	        .handler(new ChannelInitializer<SocketChannel>(){

	    		@Override
	    		protected void initChannel(SocketChannel socketChannel) throws Exception {
	    			// TODO Auto-generated method stub
	    			ChannelPipeline p = socketChannel.pipeline();
	                //配置超时时间
	                p.addLast(
	                        //增加连接超时时间定义
	                        new IdleStateHandler(
	                        READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS,
	                        BOTH_TIMEOUT_SECONDS));
	                p.addLast("ping", new IdleStateHandler(20, 50, 0, TimeUnit.SECONDS));//5s未发送数据，回调userEventTriggered
	                p.addLast(new StringEncoder(CharsetUtil.UTF_8));
	                /*
	                 * 依次编译bytebuf中的可读字符，判断看是否有"\n"或者"\r\n"
	                 * 如果有，就以此位置为结束位置，从可读索引到结束位置区间的字节就组成了一行
	                 * 处理粘包
	                 * */
	                p.addLast(new LineBasedFrameDecoder(1024));
	                /*
	                 * 将接收到的对象转成字符串，然后继续调用后面的handler
	                 * */
	                p.addLast(new StringDecoder(CharsetUtil.UTF_8));
	                p.addLast(mIOClientHandler);
	    		}
	    		
	    	});
			bootstrap.option(ChannelOption.SO_RCVBUF, 1024);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
		}catch(Exception e) {
			e.printStackTrace();
			mStatus = MachineState.NET_STATUS_INVALID;
			return false;
		}finally {
			// workGroup.shutdownGracefully();
		}
		
		return true;

	}
	
	private class ChannelFilter extends ChannelInitializer<SocketChannel>{

		@Override
		protected void initChannel(SocketChannel socketChannel) throws Exception {
			// TODO Auto-generated method stub
			ChannelPipeline p = socketChannel.pipeline();
            //配置超时时间
            p.addLast(
                    //增加连接超时时间定义
                    new IdleStateHandler(
                    READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS,
                    BOTH_TIMEOUT_SECONDS));
            p.addLast("ping", new IdleStateHandler(20, 50, 0, TimeUnit.SECONDS));//5s未发送数据，回调userEventTriggered
            p.addLast(new StringEncoder(CharsetUtil.UTF_8));
            /*
             * 依次编译bytebuf中的可读字符，判断看是否有"\n"或者"\r\n"
             * 如果有，就以此位置为结束位置，从可读索引到结束位置区间的字节就组成了一行
             * 处理粘包
             * */
            p.addLast(new LineBasedFrameDecoder(1024));
            /*
             * 将接收到的对象转成字符串，然后继续调用后面的handler
             * */
            p.addLast(new StringDecoder(CharsetUtil.UTF_8));
            p.addLast(mIOClientHandler);
		}
		
	}
	
	private boolean onConnect() {

		System.out.println("onConnect");
        try {
            // 连接服务端
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(HOST, PORT)).sync();
            channel = future.sync().channel(); // 这个地方进入工作状态,会阻塞
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            mStatus = MachineState.NET_STATUS_INVALID;
        } finally {

        }
        return false;
    }
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		/*
		 * 这个地方做个状态机,在这个线程中做状态机,这个状态机在通讯正常的情况下时会处于block状态.
		 * */
		while(true) {
			switch(mStatus) {
			case MachineState.NET_STATUS_INVALID:{
				/*
				 * 如果出现异常,就暂时
				 * */
				try {
					// 5s 后继续重连
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mStatus = MachineState.NET_STATUS_IDLE;
			}
				break;
			case MachineState.NET_STATUS_IDLE:{
				/*
				 * 启动连接
				 * */
				if(init()) {
					mStatus = MachineState.NET_STATUS_CONNECTING;
				}else {
					mStatus = MachineState.NET_STATUS_INVALID;
				}
				
			}
				break;
			case MachineState.NET_STATUS_CONNECTING:{
				mStatus = MachineState.NET_STATUS_CONNECTED;
				if(!onConnect()) {
					// 连接异常
					mStatus = MachineState.NET_STATUS_INVALID;
				}
				// 连接OK,这个状态机会处于阻塞状态[工作状态处于阻塞状态]
			}
				break;
			case MachineState.NET_STATUS_CONNECTED:{
				mStatus = MachineState.NET_STATUS_CONNECTED;
			}
				break;
			case MachineState.NET_STATUS_DISCONNECTION:
				break;
			case MachineState.NET_STATUS_IDENTITY:{
				/*
				 * 客户在服务器的身份识别
				 * */
			}
				break;
			}
		}
	}

	@Override
	public void onReceiver(String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChange(int code) {
		// TODO Auto-generated method stub
		System.out.println("onStatusChange code : "+code);
		mStatus = MachineState.NET_STATUS_INVALID;
	}
	
	public void sendMessage(String data) {
		if(mIOClientHandler!=null) {
			mIOClientHandler.sendMessage(data);
		}
	}

}
