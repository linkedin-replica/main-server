package com.linkedin.replica.mainServer.messaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import com.linkedin.replica.mainServer.config.Configuration;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MessageQueueConnection {
	private final HashMap<String, Connection> queuesConnections = new HashMap<String, Connection>();  
	
	private static MessageQueueConnection instance;
	
	private MessageQueueConnection() throws IOException, TimeoutException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(Configuration.getInstance().getAppConfigProp("rabbitmq.username"));
		factory.setPassword(Configuration.getInstance().getAppConfigProp("rabbitmq.password"));
		factory.setHost(Configuration.getInstance().getAppConfigProp("rabbitmq.ip"));

		String[] services =  Configuration.getInstance().getAppConfigProp("services.names").split(",");
		
		for(String service : services)
			queuesConnections.put(service, factory.newConnection());
		
	}
	
	public static MessageQueueConnection getInstance() throws IOException, TimeoutException{
		if(instance == null){
			synchronized (MessageQueueConnection.class) {
				if(instance == null)
					instance = new MessageQueueConnection();
			}
		}
		return instance;
	}
	
	public Channel newChannel(String service) throws IOException{
		// TODO check that connection is thread safe
		return queuesConnections.get(service).createChannel();
	}
	
	public void closeConnections() throws IOException {
		for(Connection c: queuesConnections.values())
			c.close();
	}
	
}
