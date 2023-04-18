package com.shenxinduo.chatgpt.web.server.common;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.shenxinduo.chatgpt.web.server.cache.CacheChatMessage;
import com.shenxinduo.chatgpt.web.server.cache.ChatMessageCache;
import com.shenxinduo.chatgpt.web.server.util.SpringContextUtils;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class ChatMessageCreate {

	private int maxNumTokens;

	private StringBuffer prompt = new StringBuffer();

	private List<ChatMessage> messages = Lists.newLinkedList();

	private ChatMessage system;

	private ChatMessageCache cache = SpringContextUtils.getBean(ChatMessageCache.class);

	private ChatMessageCreate() {
	}

	public static ChatMessageCreate builder() {
		int maxModelTokens = 4000;
		int maxResponseTokens = 1000;
		return builder(maxModelTokens, maxResponseTokens);
	}

	public static ChatMessageCreate builder(int maxModelTokens, int maxResponseTokens) {
		ChatMessageCreate create = new ChatMessageCreate();
		create.maxNumTokens = maxModelTokens - maxResponseTokens;
		return create;
	}

	public static ChatMessageCreate builder(int maxNumTokens) {
		ChatMessageCreate create = new ChatMessageCreate();
		create.maxNumTokens = maxNumTokens;
		return create;
	}

	public List<ChatMessage> getMessages() {
		List<ChatMessage> list = null;
		if (system != null) {
			list = Lists.newArrayListWithCapacity(messages.size() + 1);
			list.add(system);
			list.addAll(messages);
		} else {
			list = Lists.newArrayList(messages);
		}

		return list;
	}

	public void addSystemMessage(String content) {
		this.prompt.setLength(0);
		this.prompt.append("Instructions:\n").append(content);
		system = new ChatMessage(ChatMessageRole.SYSTEM.value(), content);
	}

	public void addUserMessage(String content) {
		addMessage(ChatMessageRole.USER.value(), content);
	}

	public void addAssistantMessage(String content) {
		addMessage(ChatMessageRole.ASSISTANT.value(), content);
	}

	public void addUserMessage(String parentMessageId, String content) {
		String id = parentMessageId;
		boolean flag = addMessage(ChatMessageRole.USER.value(), content);
		while (flag) {
			if (StringUtils.isBlank(id)) {
				break;
			}
			CacheChatMessage msg = cache.getMessageById(id);
			if (msg == null) {
				break;
			}
			id = msg.getParentMessageId();
			flag = addMessage(msg.getRole(), msg.getContent());
		}
	}

	private boolean addMessage(String role, String content) {
		if (this.prompt.length() > 0) {
			this.prompt.append("\n\n");
		}
		if (ChatMessageRole.USER.value().equals(role)) {
			this.prompt.append("user:\n").append(content);
		} else {
			this.prompt.append("user:\n").append(content);
		}

		if (isValidPrompt()) {
			messages.add(0, new ChatMessage(role, content));
			return true;
		}
		return false;
	}

	private boolean isValidPrompt() {
		return getTokenCount() <= maxNumTokens;
	}

	/**
	 * token数量估算，https://platform.openai.com/tokenizer
	 * 
	 * A helpful rule of thumb is that one token generally corresponds to ~4
	 * characters of text for common English text. This translates to roughly ¾ of a
	 * word (so 100 tokens ~= 75 words).
	 */
	private int getTokenCount() {
		double count = 0.0;
		for (int i = 0; i < prompt.length(); i++) {
			if (isChineseChar(prompt.charAt(i))) {
				count += 2;
			} else {
				count += 1.4;
			}
		}

		return (int) Math.ceil(count);
	}

	private static boolean isChineseChar(char c) {
		return c >= '\u4e00' && c <= '\u9fcc';
	}
}
