package com.shenxinduo.chatgpt.web.server.dto;

import lombok.Data;

@Data
public class ModelConfig {

	private String reverseProxy;
	private String httpsProxy;
	private String socksProxy;
	private String apiModel;
	private String usage;
	private int timeoutMs;

}
