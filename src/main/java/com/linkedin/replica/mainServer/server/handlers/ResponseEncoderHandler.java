package com.linkedin.replica.mainServer.server.handlers;

import java.util.LinkedHashMap;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class ResponseEncoderHandler extends ChannelOutboundHandlerAdapter{

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		LinkedHashMap<String, Object> responseBody = (LinkedHashMap<String, Object>) msg;
		HttpResponseStatus status = (HttpResponseStatus) responseBody.remove("type");
		
		// convert to JSON string
		Gson gson = new Gson();
		String body =  gson.toJson(responseBody);
		// wrap msg in ByteBuf
		ByteBuf out = Unpooled.wrappedBuffer(body.getBytes());

		// construct FullHttpResponse
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status,
					Unpooled.copiedBuffer(out.toString(CharsetUtil.UTF_8), CharsetUtil.UTF_8));		 
		// set headers
	    response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");

	    // write response to HttpResponseEncoder 
		ChannelFuture future = ctx.writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}
	
}
