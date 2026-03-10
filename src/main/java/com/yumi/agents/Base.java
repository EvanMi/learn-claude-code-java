package com.yumi.agents;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.yumi.constant.Constants;
import com.yumi.util.EnvLoader;

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

}
