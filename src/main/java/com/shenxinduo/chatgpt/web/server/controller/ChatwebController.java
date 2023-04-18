package com.shenxinduo.chatgpt.web.server.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenxinduo.chatgpt.web.server.cache.CacheChatMessage;
import com.shenxinduo.chatgpt.web.server.cache.ChatMessageCache;
import com.shenxinduo.chatgpt.web.server.common.ChatMessageCreate;
import com.shenxinduo.chatgpt.web.server.config.EnvConfig;
import com.shenxinduo.chatgpt.web.server.dto.ChatwebResult;
import com.shenxinduo.chatgpt.web.server.dto.ModelConfig;
import com.shenxinduo.chatgpt.web.server.dto.RequestProps;
import com.shenxinduo.chatgpt.web.server.dto.RespChatMessage;
import com.shenxinduo.chatgpt.web.server.dto.SessionAuth;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api")
@Slf4j
public class ChatwebController {
	@Resource
	private EnvConfig envConfig;

	@Resource
	private OpenAiService openAiService;

	@Resource
	private ChatMessageCache cache;

	@PostMapping("session")
	public ChatwebResult<?> session() {
		boolean auth = StringUtils.isNotBlank(envConfig.getAuthSecretKey());
		return ChatwebResult.newSuccessResult(new SessionAuth(envConfig.getApiModel(), auth));
	}

	@PostMapping("verify")
	public ChatwebResult<?> verify(@RequestBody JSONObject param) {
		String token = param.getString("token");
		if (StringUtils.isBlank(token)) {
			return ChatwebResult.newFailMsgResult("Secret key is empty");
		}

		if (!token.equals(envConfig.getAuthSecretKey())) {
			return ChatwebResult.newFailMsgResult("密钥无效 | Secret key is invalid");
		}

		return ChatwebResult.newSuccessMsgResult("Verify successfully");
	}

	@PostMapping("config")
	public ChatwebResult<?> config() {
		ModelConfig cc = new ModelConfig();
		cc.setHttpsProxy(envConfig.getHttpProxy());
		cc.setSocksProxy(envConfig.getSocksProxy());
		cc.setApiModel(envConfig.getApiModel());
		cc.setTimeoutMs(envConfig.getTimeoutMs());
		// 未调用chatgpt-web中的接口获取数据
		cc.setUsage("-1");

		return ChatwebResult.newSuccessResult(cc);
	}

	@PostMapping("chat-process")
	public void chatProcess(@RequestBody RequestProps requestProps, HttpServletResponse response) {
		OutputStream outputStream = null;
		try {
			response.setContentType("application/octet-stream");
			outputStream = response.getOutputStream();
			PrintWriter writer = new PrintWriter(outputStream);

			CacheChatMessage userMessage = requestProps.toUserCacheChatMessage();
			List<ChatMessage> messages = createChatMessage(requestProps);

			AtomicBoolean firstChunk = new AtomicBoolean(true);
			ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder().model("gpt-3.5-turbo")
					.messages(messages);
			if (requestProps.getTop_p() != null) {
				builder.topP(requestProps.getTop_p());
			}
			if (requestProps.getTemperature() != null) {
				builder.temperature(requestProps.getTemperature());
			}
			ChatCompletionRequest chatCompletionRequest = builder.build();

			RespChatMessage<ChatCompletionChunk> result = RespChatMessage.<ChatCompletionChunk>builder()
					.role(ChatMessageRole.ASSISTANT.value()).id(UUID.randomUUID().toString())
					.parentMessageId(userMessage.getId()).text("").build();

			openAiService.streamChatCompletion(chatCompletionRequest).doOnError(Throwable::printStackTrace)
					.blockingForEach(resp -> {
						if (StringUtils.isNotBlank(resp.getId())) {
							result.setId(resp.getId());
						}

						if (resp.getChoices() != null && !resp.getChoices().isEmpty()) {
							ChatMessage delta = resp.getChoices().get(0).getMessage();
							result.setDelta(delta.getContent());
							result.addText(delta.getContent());
							if (StringUtils.isNotBlank(delta.getRole())) {
								result.setRole(delta.getRole());
							}
							result.setDetail(resp);
							String json = JSONObject.toJSONString(result);
							if (!firstChunk.get()) {
								json = "\n" + json;
							}
							writer.write(json);
							writer.flush();
							firstChunk.set(false);
						}
					});

			CacheChatMessage respMessage = CacheChatMessage.builder().id(result.getId())
					.parentMessageId(result.getParentMessageId()).content(result.getText()).role(result.getRole())
					.build();
			cache.putMessage(userMessage);
			cache.putMessage(respMessage);
		} catch (Exception e) {
			log.error("请求异常：requestProps = {}", requestProps, e);
			String json = JSONObject.toJSONString(ChatwebResult.newFailMsgResult("请求异常"));
			PrintWriter writer = new PrintWriter(outputStream);
			writer.write(json);
			writer.flush();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private List<ChatMessage> createChatMessage(RequestProps requestProps) {
		ChatMessageCreate create = ChatMessageCreate.builder();
		String systemMessage = requestProps.getSystemMessage();
		if (StringUtils.isNotBlank(systemMessage)) {
			create.addSystemMessage(systemMessage);
		}

		if (requestProps.getOptions() != null
				&& StringUtils.isNotBlank(requestProps.getOptions().getParentMessageId())) {
			create.addUserMessage(requestProps.getOptions().getParentMessageId(), requestProps.getPrompt());
		} else {
			create.addUserMessage(requestProps.getPrompt());
		}
		return create.getMessages();
	}

}
