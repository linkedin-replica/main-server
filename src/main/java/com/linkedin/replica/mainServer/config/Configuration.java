package com.linkedin.replica.mainServer.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	private static final Properties webServicesConfigProps = new Properties();
	
	private static Configuration instance;
	
	private Configuration (String webServiceConfigFilePath) throws IOException{
		populateWithConfig(webServiceConfigFilePath, webServicesConfigProps);
	}
	
	public static Configuration getInstance() {
		return instance;
	}

    public static void init(String webServiceConfigFilePath) throws IOException {
        instance = new Configuration(webServiceConfigFilePath);
    }
	
	private static void populateWithConfig(String configFilePath,Properties properties) throws IOException {
		FileInputStream inputStream = new FileInputStream(configFilePath);
		properties.load(inputStream);
		inputStream.close();
	}

    public String getWebServiceConfigProp(String key) {
        return webServicesConfigProps.getProperty(key);
    }
    
}
