package com.menmasystems.menmudiscordbot.errorhandler;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

/**
 * CommandExecutionException.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class CommandExecutionException extends Exception {

    public String command;
    public ErrorMessage errorType;

    public CommandExecutionException(String command, String errorMessage) {
        super("Command=" + command + ", ErrorMessage=" + errorMessage);
    }

    public CommandExecutionException(String command, ErrorMessage errorType) {
        super("Command=" + command + ", Error=" + errorType.name());
        this.command = command;
        this.errorType = errorType;
    }

    public Mono<Message> createErrorMessage(ChatInputInteractionEvent event, boolean followUp) {
        if(errorType == null) return Mono.empty();

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.RED)
                .description(errorType.getMessage())
                .build();

        if(followUp) {
            var spec = InteractionFollowupCreateSpec.builder().addEmbed(embed).build();
            return event.createFollowup(spec);
        }

        var spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
        return event.reply(spec).then(event.getReply());
    }

    public Mono<Message> createErrorMessage(ChatInputInteractionEvent event) {
        return createErrorMessage(event, false);
    }
}
