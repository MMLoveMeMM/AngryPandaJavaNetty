package com.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class IOServerHandler extends SimpleChannelInboundHandler<String>{

	private Channel mChannel;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String arg1) throws Exception {
		// TODO Auto-generated method stub
		/*
		 * ��ȡ����,�������������Ҫ����һ��
		 * */
		System.out.println("channelRead0 data : "+arg1);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object arg1) throws Exception {
		// TODO Auto-generated method stub
		super.channelRead(ctx, arg1);
		/*
		 * ��ȡ����
		 */ 
		System.out.println("channelRead ..."+arg1);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		/*
		 * ���ӳɹ�
		 * */
		System.out.println("channelActive ...");
		mChannel = ctx.channel();
		mChannel.writeAndFlush("connect server successfully !" + System.getProperty("line.separator")).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				// TODO Auto-generated method stub
				if(arg0.isSuccess()) {
					System.out.println("operation complete ...");
				}
			}
			
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		/*
		 * ���ӶϿ�
		 * */
		System.out.println("channelInactive ...");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
		/*
		 * ����ط���ȡ���,�����������������
		 * */
		System.out.println("channelReadComplete ...");
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
		System.out.println("channelRegistered ...");
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelUnregistered(ctx);
		System.out.println("channelUnregistered ...");
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelWritabilityChanged(ctx);
		System.out.println("channelWritabilityChanged ...");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		System.out.println("exceptionCaught ...");
	}
	
	public void sendMessage(String data,ChannelFutureListener callback) {
		if(mChannel!=null && mChannel.isActive() && mChannel.isRegistered()) {
			mChannel.writeAndFlush("connect server successfully !" + System.getProperty("line.separator")).addListener(callback);
		}
	}
	

}
