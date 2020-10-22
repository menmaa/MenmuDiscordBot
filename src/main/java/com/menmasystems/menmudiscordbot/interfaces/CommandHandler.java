package com.menmasystems.menmudiscordbot.interfaces;

import com.menmasystems.menmudiscordbot.Menmu;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * CommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public interface CommandHandler {
    Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params);
    default void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setDescription("No Help Message provided for this command.");
        }).subscribe();
    }
}
