package com.yumi.util;

import com.anthropic.core.JsonValue;

import java.util.function.Function;

public class ToolWrapper <T> {
    private final Function<T, String> fun;
    private final Class<T> type;

    public ToolWrapper(Function<T, String> fun, Class<T> type) {
        this.fun = fun;
        this.type = type;
    }

    public Function<T, String> getCommand() {
        return fun;
    }

    public String executeCommand(JsonValue input) {
        T convert = input.convert(type);
        return fun.apply(convert);
    }
}
