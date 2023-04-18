package com.shenxinduo.chatgpt.web.server.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import retrofit2.Retrofit;

@Configuration
public class OpenAiConfig {
	@Resource
	private EnvConfig envConfig;

	@Bean
	public OpenAiService openAiService() {
		Builder builder = OpenAiService.defaultClient(envConfig.getOpenaiApiKey(), Duration.ofSeconds(10)).newBuilder();
		if (envConfig.getHttpProxy() != null) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(envConfig.getHttpProxyHost(),
					Integer.parseInt(envConfig.getHttpProxyPort())));
			builder.proxy(proxy);
		} else if (envConfig.getSocksProxy() != null) {
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(envConfig.getHttpProxyHost(),
					Integer.parseInt(envConfig.getHttpProxyPort())));
			builder.proxy(proxy);
		}
		OkHttpClient client = builder.build();

		ObjectMapper mapper = OpenAiService.defaultObjectMapper();
		Retrofit retrofit = OpenAiService.defaultRetrofit(client, mapper);
		OpenAiApi api = retrofit.create(OpenAiApi.class);
		OpenAiService service = new OpenAiService(api, client.dispatcher().executorService());
		return service;
	}
}
