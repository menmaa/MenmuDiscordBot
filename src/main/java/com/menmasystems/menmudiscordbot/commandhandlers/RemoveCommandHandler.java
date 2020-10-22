package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.InvalidQueuePositionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RemoveCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 03/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class RemoveCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        try {
            int position = Integer.parseInt(params.get(1));

            return Mono.justOrEmpty(event.getGuildId())
                    .map(Menmu::getGuildData)
                    .map(GuildData::getTrackScheduler)
                    .flatMap(trackScheduler -> trackScheduler.removeQueue(position))
                    .doOnSuccess(removed -> {
                        String msg = ":no_entry: Track `%s` has been removed from the music queue.";
                        Menmu.sendErrorMessage(channel, String.format(msg, removed.getInfo().title), null);
                    })
                    .onErrorResume(error -> error instanceof InvalidQueuePositionException, error -> {
                        Menmu.sendErrorMessage(channel, ":no_entry_sign: Track does not exist in the music queue.", null);
                        return Mono.empty();
                    }).then();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            Menmu.sendErrorMessage(channel, ":no_entry_sign: Invalid position number or no number provided. Correct Usage: `remove [position in queue]`. e.g.: `remove 3`", null);
        }
        return Mono.empty();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!remove";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `remove`");
            embedCreateSpec.setDescription("Removes the song in the inserted position in the guild music queue, from it.");
            embedCreateSpec.addField("Usage", "`"+command+" [position]`", true);
            embedCreateSpec.addField("Example", "`"+command+" 3`", true);
        }).block();
    }
}
