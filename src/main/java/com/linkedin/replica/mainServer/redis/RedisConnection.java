package com.linkedin.replica.mainServer.redis;

import com.linkedin.replica.mainServer.config.Configuration;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisConnection {
	private JedisPool cachePool;
	private static RedisConnection instance;
	
	private RedisConnection(){
		String ip = Configuration.getInstance().getAppConfigProp("redis.ip");
		int port = Integer.parseInt(Configuration.getInstance().getAppConfigProp("redis.port"));
		cachePool = new JedisPool(ip,port);
	}
		
	public static RedisConnection init(){
		if(instance == null)
			instance = new RedisConnection();
		
		return instance;
	}

	public static RedisConnection getInstance(){
		return instance;
	}
	
	public Jedis getResource(){
		return cachePool.getResource();
	}
	
}
