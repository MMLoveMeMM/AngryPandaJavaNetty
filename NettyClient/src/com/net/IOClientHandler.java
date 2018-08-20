package com.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class IOClientHandler extends SimpleChannelInboundHandler<String>{

	private INetListener<String> mListener;
	private Channel mChannel;
	
	public IOClientHandler(INetListener<String> listener) {
		mListener = listener;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String data) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("data : "+data);
		mListener.onReceiver(data);
	}

	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
		// TODO Auto-generated method stub
		return super.acceptInboundMessage(msg);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object arg1) throws Exception {
		// TODO Auto-generated method stub
		super.channelRead(ctx, arg1);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		mChannel = ctx.channel();
		sendMessage("client connection");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		mListener.onStatusChange(MachineState.NET_STATUS_INVALID);
	}
	
	public void sendMessage(String data) {
		System.out.println("client send : "+data);
		if(mChannel!=null && mChannel.isActive()) {
			mChannel.writeAndFlush(data+"\n");
		}
		
	}
	
}
