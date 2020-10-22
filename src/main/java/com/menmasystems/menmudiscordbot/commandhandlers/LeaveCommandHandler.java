package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * LeaveCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class LeaveCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {

        return Mono.justOrEmpty(event.getGuildId())
                .map(Menmu::getGuildData)
                .flatMap(guildData -> Mono.justOrEmpty(guildData.getVoiceConnection())
                        .switchIfEmpty(Mono.error(new CommandExecutionException("leave", CommandExecutionException.ErrorType.SELF_VOICE_STATE_NULL)))
                        .flatMap(voiceConnection -> event.getClient().getSelf()
                        .flatMap(self -> voiceConnection.disconnect().doOnSuccess(unused -> {
                            guildData.setVoiceConnection(null);
                            guildData.setBoundTextChannel(null);
                            channel.createEmbed(spec -> {
                                spec.setColor(Color.RED);
                                spec.setDescription(":no_entry: Left voice channels. Thanks for trying out " + self.getUsername() + "-san!");
                            }).subscribe();
                        }))));
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!leave";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `leave`");
            embedCreateSpec.setDescription("Disconnects me from voice channels.");
            embedCreateSpec.addField("Usage", "`"+command+"`", true);
        }).block();
    }
}
