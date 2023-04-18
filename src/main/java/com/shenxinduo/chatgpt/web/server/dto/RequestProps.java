package com.shenxinduo.chatgpt.web.server.dto;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.shenxinduo.chatgpt.web.server.cache.CacheChatMessage;
import com.shenxinduo.chatgpt.web.server.cache.CacheChatMessage.CacheChatMessageBuilder;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import lombok.Data;

@Data
public class RequestProps {

	private String prompt;
	private ChatContext options;
	private String systemMessage;
	private Double temperature;
	private Double top_p;

	public CacheChatMessage toUserCacheChatMessage() {
		CacheChatMessageBuilder builder = CacheChatMessage.builder().id(UUID.randomUUID().toString())
				.content(this.prompt).role(ChatMessageRole.USER.value());
		if (options != null && StringUtils.isNotBlank(options.getParentMessageId())) {
			builder.parentMessageId(options.getParentMessageId());
		}
		return builder.build();
	}
}
