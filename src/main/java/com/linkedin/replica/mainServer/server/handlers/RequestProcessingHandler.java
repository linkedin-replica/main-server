package com.linkedin.replica.mainServer.server.handlers;

import java.nio.file.InvalidPathException;
import java.util.LinkedHashMap;

import com.google.gson.JsonObject;
import com.linkedin.replica.serachEngine.Exceptions.SearchException;
import com.linkedin.replica.serachEngine.services.ControllerService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponseStatus;


public class RequestProcessingHandler extends ChannelInboundHandlerAdapter{
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// JSONObject body that was decoded by RequestDecoderHandler
		JsonObject body = (JsonObject) msg;

		// pass body to ControllerService to serve the request
		ControllerService.serve(body);

		// create successful response
		LinkedHashMap<String, Object> responseBody = new LinkedHashMap<String, Object>();
		responseBody.put("type", HttpResponseStatus.ACCEPTED);
		responseBody.put("code", HttpResponseStatus.ACCEPTED.code());
		responseBody.put("message", "Changes are applied successfully and configuration files are updated");
		
		// send response to ResponseEncoderHandler
		ctx.writeAndFlush(responseBody);
	}

	
	/**
	 * Overriding exceptionCaught()  to react to any Throwable.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// construct Error Response
		LinkedHashMap<String, Object> responseBody = new LinkedHashMap<String, Object>();
		
		// set Http status code
		if(cause instanceof InvalidPathException){
			responseBody.put("code", HttpResponseStatus.NOT_FOUND.code());
			responseBody.put("type", HttpResponseStatus.NOT_FOUND);
		}else{ 
			if (cause instanceof SearchException){
				responseBody.put("code", HttpResponseStatus.BAD_REQUEST.code());
				responseBody.put("type", HttpResponseStatus.BAD_REQUEST);
			}else{
				responseBody.put("code", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
				responseBody.put("type", HttpResponseStatus.INTERNAL_SERVER_ERROR);
			}
		}
		responseBody.put("errMessage", cause.getMessage());
		
//		cause.printStackTrace();
		// send response to ResponseEncoderHandler
		ctx.writeAndFlush(responseBody);
	}	
}
