package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

/**
 * LeaveCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class LeaveCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .flatMap(guildData -> Mono.justOrEmpty(guildData.getVoiceConnection())
                        .switchIfEmpty(Mono.error(new CommandExecutionException("leave", CommandExecutionException.ErrorType.SELF_VOICE_STATE_NULL)))
                        .flatMap(voiceConnection -> event.getClient().getSelf()
                        .flatMap(self -> voiceConnection.disconnect())
                        .then(guildData.setVoiceConnection(null))
                        .then(guildData.setBoundMessageChannel(null))
                        .then(event.getClient().getSelf())
                        .flatMap(self -> event.getInteraction().getChannel()
                                .flatMap(channel -> channel.createEmbed(spec -> {
                                    spec.setColor(Color.RED);
                                    spec.setDescription(":no_entry: Left voice channels. Thanks for trying out " + self.getUsername() + "-san!");
                                })).then())));
    }

    @Override
    public void helpHandler(ChatInputInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `leave`")
                        .description("Disconnects me from voice channels.")
                        .addField("Usage", "`/leave`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
