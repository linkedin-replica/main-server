package com.linkedin.replica.mainServer.messaging;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.linkedin.replica.mainServer.config.Configuration;
import com.rabbitmq.client.*;


public class ResponseMessageReceiver {
	private static final ConcurrentHashMap<String, OnResponseListener> responseListeners = new ConcurrentHashMap<String, OnResponseListener>();
	private static Channel channel;
	
	private static  ResponseMessageReceiver instance;

	private ResponseMessageReceiver () throws IOException, TimeoutException{
		String queueName = Configuration.getInstance().getAppConfigProp("rabbitmq.queue.name");
		String ip = Configuration.getInstance().getAppConfigProp("rabbitmq.ip");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(ip);
		Connection con = factory.newConnection();
		channel = con.createChannel();
        Consumer consumer = initConsumer();
        
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicConsume(queueName, true, consumer);
	}
	
	public static ResponseMessageReceiver getInstance() throws IOException, TimeoutException{
		if(instance == null){
			synchronized (ResponseMessageReceiver.class) {
				if(instance == null){
					instance = new ResponseMessageReceiver();
				}
			}
		}		
		return instance;
	}
	
	private static Consumer initConsumer(){
		 // Create the messages consumer
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
            	// get correlationID
            	String correlationID = properties.getCorrelationId();

                // Extract the request arguments
                String responseBody = new String(body, CharsetUtil.UTF_8);

                // call callback method in the channel waiting for response
                responseListeners.get(correlationID).onResponse(responseBody);
                // remove listener from map after calling it
                responseListeners.remove(correlationID);
            }
        };
        
        return consumer;
	}
	
	public void addListener(String id, OnResponseListener listener){
		responseListeners.put(id, listener);
	}
}	
