package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ClearCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 01/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class ClearCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(Menmu::getGuildData)
                .map(GuildData::getTrackScheduler)
                .map(MenmuTrackScheduler::purgeQueue)
                .flatMap(queue -> channel.createEmbed(spec -> {
                    spec.setColor(Color.GREEN);
                    spec.setDescription(":white_check_mark: Music queue purged.");
                })).then();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!clear";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `clear`");
            embedCreateSpec.setDescription("Clears/purges the guild music queue.");
            embedCreateSpec.addField("Usage", "`"+command+"`", true);
        }).block();
    }
}
