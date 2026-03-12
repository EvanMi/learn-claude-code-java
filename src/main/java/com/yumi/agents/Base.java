package com.yumi.agents;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ToolUseBlockParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yumi.constant.Constants;
import com.yumi.util.EnvLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Base {
    static {
        EnvLoader.loadEnv();
    }

    protected static AnthropicClient client = AnthropicOkHttpClient.builder()
            .baseUrl(System.getProperty(Constants.ANTHROPIC_BASE_URL_KEY))
            .apiKey(System.getProperty(Constants.ANTHROPIC_API_KEY_KEY))
            .build();

    protected static String MODEL = System.getProperty(Constants.MODEL_ID_KEY);

    protected static String WORKDIR = System.getProperty("user.dir");

    protected static void addAssistants(List<MessageParam> messages, Message response) {
        // Append assistant turn
        for (ContentBlock contentBlock : response.content()) {
            if (contentBlock.isToolUse()) {
                contentBlock.toolUse().ifPresent(toolUseBlock -> {
                    var list = new ArrayList<ContentBlockParam>();
                    list.add(ContentBlockParam.ofToolUse(ToolUseBlockParam.builder()
                            .id(toolUseBlock.id())
                            .name(toolUseBlock.name())
                            .input(ToolUseBlockParam.Input.builder()
                                    .putAllAdditionalProperties(
                                            Objects.requireNonNull(
                                                    toolUseBlock._input()
                                                            .convert(new TypeReference<Map<String, JsonValue>>() {}))
                                    ).build())
                            .build()));
                    messages.add(
                            MessageParam.builder().role(MessageParam.Role.ASSISTANT).content(MessageParam.Content.ofBlockParams(list)).build()
                    );
                });
            }
            if (contentBlock.isText()) {
                contentBlock.text().ifPresent(textUseBlock -> {
                    var list = new ArrayList<ContentBlockParam>();
                    list.add(ContentBlockParam.ofText(TextBlockParam.builder().text(textUseBlock.text()).build()));
                    messages.add(
                            MessageParam.builder().role(MessageParam.Role.ASSISTANT).content(MessageParam.Content.ofBlockParams(list)).build()
                    );
                });
            }
        }
    }

}
