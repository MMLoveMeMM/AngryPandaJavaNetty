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
		
		workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);// ˫��
		bootstrap = new Bootstrap();
		mIOClientHandler = new IOClientHandler(this);
		
		try {
			bootstrap
	        .group(workGroup)   //�������� ���߳�Reactorģʽ��boss�� work ����һ���̳߳أ�
	        .channel(NioSocketChannel.class)
	        .handler(new ChannelInitializer<SocketChannel>(){

	    		@Override
	    		protected void initChannel(SocketChannel socketChannel) throws Exception {
	    			// TODO Auto-generated method stub
	    			ChannelPipeline p = socketChannel.pipeline();
	                //���ó�ʱʱ��
	                p.addLast(
	                        //�������ӳ�ʱʱ�䶨��
	                        new IdleStateHandler(
	                        READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS,
	                        BOTH_TIMEOUT_SECONDS));
	                p.addLast("ping", new IdleStateHandler(20, 50, 0, TimeUnit.SECONDS));//5sδ�������ݣ��ص�userEventTriggered
	                p.addLast(new StringEncoder(CharsetUtil.UTF_8));
	                /*
	                 * ���α���bytebuf�еĿɶ��ַ����жϿ��Ƿ���"\n"����"\r\n"
	                 * ����У����Դ�λ��Ϊ����λ�ã��ӿɶ�����������λ��������ֽھ������һ��
	                 * ����ճ��
	                 * */
	                p.addLast(new LineBasedFrameDecoder(1024));
	                /*
	                 * �����յ��Ķ���ת���ַ�����Ȼ��������ú����handler
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
            //���ó�ʱʱ��
            p.addLast(
                    //�������ӳ�ʱʱ�䶨��
                    new IdleStateHandler(
                    READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS,
                    BOTH_TIMEOUT_SECONDS));
            p.addLast("ping", new IdleStateHandler(20, 50, 0, TimeUnit.SECONDS));//5sδ�������ݣ��ص�userEventTriggered
            p.addLast(new StringEncoder(CharsetUtil.UTF_8));
            /*
             * ���α���bytebuf�еĿɶ��ַ����жϿ��Ƿ���"\n"����"\r\n"
             * ����У����Դ�λ��Ϊ����λ�ã��ӿɶ�����������λ��������ֽھ������һ��
             * ����ճ��
             * */
            p.addLast(new LineBasedFrameDecoder(1024));
            /*
             * �����յ��Ķ���ת���ַ�����Ȼ��������ú����handler
             * */
            p.addLast(new StringDecoder(CharsetUtil.UTF_8));
            p.addLast(mIOClientHandler);
		}
		
	}
	
	private boolean onConnect() {

		System.out.println("onConnect");
        try {
            // ���ӷ����
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(HOST, PORT)).sync();
            channel = future.sync().channel(); // ����ط����빤��״̬,������
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
		 * ����ط�����״̬��,������߳�����״̬��,���״̬����ͨѶ�����������ʱ�ᴦ��block״̬.
		 * */
		while(true) {
			switch(mStatus) {
			case MachineState.NET_STATUS_INVALID:{
				/*
				 * ��������쳣,����ʱ
				 * */
				try {
					// 5s ���������
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
				 * ��������
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
					// �����쳣
					mStatus = MachineState.NET_STATUS_INVALID;
				}
				// ����OK,���״̬���ᴦ������״̬[����״̬��������״̬]
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
				 * �ͻ��ڷ����������ʶ��
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
