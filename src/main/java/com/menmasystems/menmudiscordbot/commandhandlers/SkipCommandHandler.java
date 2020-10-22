package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.errorhandlers.InvalidQueuePositionException;
import com.menmasystems.menmudiscordbot.errorhandlers.MusicQueueEmptyException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * SkipCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 02/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class SkipCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        if(params.size() < 2) {
            return Mono.justOrEmpty(event.getGuildId())
                    .map(Menmu::getGuildData)
                    .map(GuildData::getTrackScheduler)
                    .flatMap(MenmuTrackScheduler::skip)
                    .doOnSuccess(unused -> Menmu.sendSuccessMessage(channel, ":white_check_mark: Song skipped!"))
                    .doOnError(MusicQueueEmptyException.class, error -> Menmu.sendErrorMessage(channel, ":no_entry_sign: Queue is empty!", null)).then();
        } else {
            try {
                int position = Integer.parseInt(params.get(1));

                return Mono.justOrEmpty(event.getGuildId())
                        .map(Menmu::getGuildData)
                        .map(GuildData::getTrackScheduler)
                        .flatMap(trackScheduler -> trackScheduler.skipTo(position))
                        .doOnSuccess(unused ->
                                Menmu.sendSuccessMessage(channel, ":white_check_mark: Skipped to position " + position + " in queue."))
                        .doOnError(InvalidQueuePositionException.class, error ->
                                Menmu.sendErrorMessage(channel, ":no_entry_sign: No track in position " + position + " found in queue.", null))
                        .then();
            } catch (NumberFormatException e) {
                Menmu.sendErrorMessage(channel, ":no_entry_sign: That is not a valid number!", null);
            }
        }
        return Mono.empty();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!skip";
            final String title = "Command: `skip`";
            final String description = "Stops playing the current track and skips to the next in the guild music queue. " +
                    "If a number is specified as a parameter, moves to that position in queue, skipping all songs before it.";
            final String usage = String.format("`%s`\n`%s [position]`",  command, command);
            final String examples = String.format("`%s`\n`%s 5`", command, command);

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle(title);
            embedCreateSpec.setDescription(description);
            embedCreateSpec.addField("Usage", usage, true);
            embedCreateSpec.addField("Examples", examples, true);
        }).block();
    }
}
