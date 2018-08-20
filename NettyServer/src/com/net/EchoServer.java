package com.net;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class EchoServer {

	private final static int PORT=8009;

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private IOServerHandler mIOServerHandler;
    
    public void startServer(){

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        mIOServerHandler=new IOServerHandler();
        
        System.out.println("start server ...");
        
        ServerBootstrap boot = new ServerBootstrap();
        try {
            boot.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 5
                    .localAddress(new InetSocketAddress(PORT)) // 6   区别再 监听端口 形式；
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 7

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("initChannel ch:" + ch);
                            ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast(mIOServerHandler);
                        }
                    });
            // Bind and start to accept incoming connections.
            ChannelFuture f = boot.bind().sync();
            
            f.channel().closeFuture().sync(); // 9
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("start server ...");

    }
    
    
    public void sendMessage(String data, ChannelFutureListener callback) {
    	
    	mIOServerHandler.sendMessage(data, callback);
    	
    }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new EchoServer().startServer();
		
	}

}
