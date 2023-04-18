package com.shenxinduo.chatgpt.web.server.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.shenxinduo.chatgpt.web.server.common.ApiModel;
import com.shenxinduo.chatgpt.web.server.dto.ModelConfig;

import lombok.Data;

@ConfigurationProperties(prefix = "env-config")
@Configuration
@Data
public class EnvConfig {

	private String authSecretKey;

	private String openaiApiKey;

	private String openaiApiBaseUrl;

	private String openaiApiModel;

	private String openaiApiDisableDebug;

	private int timeoutMs;

	private String maxRequestPerHour;

	private String socksProxyHost;

	private String socksProxyPort;

	private String socksProxyUsername;

	private String socksProxyPassword;

	private String httpProxyHost;

	private String httpProxyPort;

	public int getRateLimitCount() {

		if (StringUtils.isNotBlank(this.maxRequestPerHour)) {
			try {
				return Integer.parseInt(this.maxRequestPerHour);
			} catch (Exception e) {
			}
		}

		return -1;
	}

	public String getHttpProxy() {
		if (StringUtils.isNotBlank(httpProxyHost) && StringUtils.isNotBlank("httpProxyPort")) {
			return "http://" + httpProxyHost + ":" + httpProxyPort;
		}
		return null;
	}

	public String getSocksProxy() {
		if (StringUtils.isNotBlank(socksProxyHost) && StringUtils.isNotBlank("socksProxyPort")) {
			return socksProxyHost + ":" + socksProxyPort;
		}
		return null;
	}

	public String getApiModel() {
		return StringUtils.isBlank(this.openaiApiKey) ? ApiModel.CHAT_GPT_UNOFFICIAL_PROXY_API : ApiModel.CHAT_GPT_API;
	}

	public ModelConfig toChatConfig() {
		ModelConfig config = new ModelConfig();
		config.setApiModel(openaiApiModel);
		return config;
	}
}
