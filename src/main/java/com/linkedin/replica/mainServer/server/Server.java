package com.linkedin.replica.mainServer.server;

import java.net.InetSocketAddress;



import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class Server {
	private final String IP;
	private final int PORT;
	private EventLoopGroup bossGroup;
	public Server(String IP, int PORT){
		this.IP = IP;
		this.PORT = PORT;
	}
	
	public void start() throws InterruptedException{
		// Producer which is responsible for accepting connections
        bossGroup = new NioEventLoopGroup();
		/*
		 *  consumer which handles the traffic of the accepted connection once the boss accepts the connection.
		 *  Boss (producer) registers the accepted connection to the worker (consumer).
		 */
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try{
        	/*
        	 *  Bootstrap a server channel.
        	 *   ServerChannel is a Channel that accepts an incoming connection attempt and creates a child Channel after accepting request.
        	 */
        	ServerBootstrap server = new ServerBootstrap();
        	server.group(bossGroup, workerGroup) // setting EventLoopGroups
        			.channel(NioServerSocketChannel.class) // set channel to NIO(non-blocking IO) transport channel
        			.childHandler(new ChannelInitializer<SocketChannel>() { // initialize channel

						@Override
						protected void initChannel(SocketChannel channel) throws Exception {
//							channel.pipeline().addLast(new HttpRequestDecoder()); // decode request bytes to FullHttpRequest (HttpRequest, HttpRequestContent, LastHttpRequestContent).
//							channel.pipeline().addLast(new HttpResponseEncoder());	 // encode FullHttpResponse to bytes.
//							channel.pipeline().addLast(new ResponseEncoderHandler()); // encode response object model into FullHttpResponse.
//							channel.pipeline().addLast(new RequestDecoderHandler()); // decode FullHttpRequest to request model.
//							channel.pipeline().addLast(new RequestProcessingHandler()); // process request object model and create response object model from results.
						}	
					})
					.option(ChannelOption.SO_BACKLOG, 128) // maximum queue length for incoming connection (a request to connect)
					.childOption(ChannelOption.SO_KEEPALIVE, true); // keep tcp connection alive.
        			
			// Bind and start to accept incoming connections.
        	InetSocketAddress socketAddress = new InetSocketAddress(IP, PORT);
			final ChannelFuture future = server.bind(socketAddress);
			future.addListener(new ChannelFutureListener() {
				
				public void operationComplete(ChannelFuture ch) throws Exception {
						if(ch.isSuccess()){
							System.out.println("Server started at Host = "+IP +" and Port = "+PORT);
						}else{
							System.err.println("Failed to start server at Host = "+IP +" and Port = "+PORT);
							future.cause().printStackTrace();
						}
				}
			});
			
			future.channel().closeFuture().sync();	
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
    
	}
	
	public void shutdown() throws InterruptedException{
		bossGroup.shutdownGracefully().sync();
	}
}
