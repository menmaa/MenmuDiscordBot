package com.menmasystems.menmudiscordbot.errorhandlers;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * CommandExecutionException.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class CommandExecutionException extends Exception {

    public enum ErrorType {
        UNSPECIFIED,
        USER_VOICE_STATE_NULL,
        SELF_VOICE_STATE_NULL,
        VOICE_CONNECTION_ERROR
    }

    public String command;
    public Throwable throwable;
    public ErrorType errorType;
    private Logger logger = LoggerFactory.getLogger(CommandExecutionException.class);

    public CommandExecutionException(String command, String errorMessage) {
        super("Command=" + command + ", ErrorMessage=" + errorMessage);
    }

    public CommandExecutionException(String command, ErrorType errorType) {
        super("Command=" + command + ", Error=" + errorType.name());
        this.command = command;
        this.errorType = errorType;
    }

    public CommandExecutionException(String command, ErrorType errorType, Throwable throwable) {
        this(command, errorType);
        this.throwable = throwable;
        this.addSuppressed(throwable);
    }

    public Mono<Message> createErrorMessage(MessageChannel channel) {
        if(errorType == null) return Mono.empty();

        switch (errorType.name()) {
            case "UNSPECIFIED": {
                if(throwable != null)
                    logger.error("There was an error trying to execute command " + command, throwable);
                return channel.createEmbed(spec -> {
                    spec.setColor(Color.RED);
                    spec.setDescription(":no_entry_sign: There was an error trying to execute command `" + command + "`. " +
                            "If the issue persists, please contact `Menma#0001`.");
                });
            }
            case "USER_VOICE_STATE_NULL":
                return channel.createEmbed(spec -> {
                    spec.setColor(Color.RED);
                    spec.setDescription(":no_entry_sign: You are not connected to any voice channels.");
                });
            case "SELF_VOICE_STATE_NULL":
                return channel.createEmbed(spec -> {
                    spec.setColor(Color.RED);
                    spec.setDescription(":no_entry_sign: I am not connected to any voice channels.");
                });
            case "VOICE_CONNECTION_ERROR":
                return channel.createEmbed(spec -> {
                    spec.setColor(Color.RED);
                    spec.setDescription(":no_entry_sign: There was an error trying to connect to voice. " +
                            "If the issue persists, please contact `Menma#0001`.");
                });
        }
        return Mono.empty();
    }
}
