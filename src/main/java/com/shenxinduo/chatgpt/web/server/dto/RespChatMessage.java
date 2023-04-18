package com.shenxinduo.chatgpt.web.server.dto;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RespChatMessage<T> {

	private String id;
	private String text;
	private String role;
	private String name;
	private String delta;
	private T detail;
	private String parentMessageId;
	private String conversationId;

	public void addText(String content) {
		if (StringUtils.isNotBlank(content)) {
			text += content;
		}
	}
}
