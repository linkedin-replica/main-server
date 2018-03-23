package com.linkedin.replica.mainServer.server.handlers;


import java.nio.file.InvalidPathException;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.linkedin.replica.mainServer.config.Configuration;
import com.linkedin.replica.mainServer.exceptions.MainServerException;
import com.linkedin.replica.mainServer.model.Request;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;


public class RequestFilterationHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			Request request = (Request) msg;
			// check if request URI is not a valid web service call
			if(Configuration.getInstance().getWebServConfigProp(request.getWebServName()) == null 
					&& Configuration.getInstance().getCommandConfigProp(request.getWebServName()+"."+request.getFuncName()) == null)
				throw new InvalidPathException(request.getRequestURI(), "Access Denied, forbidden request");

			// check that POST, PUT and DELETE requests has a valid body
			HttpMethod method = request.getMethod();
			if((method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.DELETE)) 
					&& (request.getBody().isEmpty() || request.getBody().replaceAll(" ", "").isEmpty()))
				throw new MainServerException("Request Body must not be empty.");

			String token = request.getHeaders().get("access-token");
			// TODO validate token with secret key
			// extract user Id
			// add userId to request if needed
			
			ctx.fireChannelRead(request);
	}

	/**
	 * Overriding exceptionCaught()  to react to any Throwable.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// construct Error Response
		LinkedHashMap<String, Object> responseBody = new LinkedHashMap<String, Object>();
		
		// set Http status code
		if(cause instanceof InvalidPathException){
			responseBody.put("statusCode", HttpResponseStatus.NOT_FOUND.code());
		}else{ 
			if (cause instanceof MainServerException){
				responseBody.put("statusCode", HttpResponseStatus.BAD_REQUEST.code());
			}else{
				responseBody.put("statusCode", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
			}
		}
		responseBody.put("errMessage", cause.getMessage());
		String json = new Gson().toJson(responseBody);
//		cause.printStackTrace();
		// send response to ResponseEncoderHandler
		ctx.writeAndFlush(json);
	}
}
