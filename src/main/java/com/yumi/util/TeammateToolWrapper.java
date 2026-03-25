package com.yumi.util;

import com.anthropic.core.JsonValue;

import java.util.function.Function;

public class TeammateToolWrapper<T> {
    private final Function<T, String> fun;
    private final Class<T> type;
    private final TeammateManager manager;

    public TeammateToolWrapper(Function<T, String> fun, Class<T> type, TeammateManager manager) {
        this.fun = fun;
        this.type = type;
        this.manager = manager;
    }

    public Function<T, String> getCommand() {
        return fun;
    }

    public String executeCommand(String sender, JsonValue input) {
        T convert = input.convert(type);
        if (convert instanceof MessageBus.BusSender aBusSender) {
            aBusSender.setSender(sender);
        }
        if (convert instanceof MessageBus.BroadcastCommand broadcastCommand) {
            broadcastCommand.setTeammates(this.manager.memberNames());
        }
        return fun.apply(convert);
    }
}
