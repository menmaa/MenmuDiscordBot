package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * StopCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 07/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class StopCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(Menmu::getGuildData)
                .map(GuildData::getTrackScheduler)
                .map(MenmuTrackScheduler::stop)
                .then();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!stop";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `stop`");
            embedCreateSpec.setDescription("Stops playing current track if already playing.");
            embedCreateSpec.addField("Usage", "`"+command+"`", true);
        }).block();
    }
}
