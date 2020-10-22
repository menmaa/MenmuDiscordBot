package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RepeatCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 01/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class RepeatCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {

        return Mono.justOrEmpty(event.getGuildId())
                .map(Menmu::getGuildData)
                .doOnNext(guildData -> {
                    if(guildData.isRepeatCurrentTrack()) {
                        guildData.setRepeatCurrentTrack(false);
                        Menmu.sendErrorMessage(channel, ":no_entry: Song repeat disabled.", null);
                    } else {
                        guildData.setRepeatCurrentTrack(true);
                        Menmu.sendSuccessMessage(channel, ":white_check_mark: Song repeat enabled.");
                    }
                }).then();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!repeat";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `repeat`");
            embedCreateSpec.setDescription("Enables/disables current song repeat.");
            embedCreateSpec.addField("Usage", "`"+command+"`", true);
        }).block();
    }
}
