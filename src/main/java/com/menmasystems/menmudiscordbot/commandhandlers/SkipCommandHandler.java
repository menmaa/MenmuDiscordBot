package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Managers;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.errorhandler.InvalidQueuePositionException;
import com.menmasystems.menmudiscordbot.errorhandler.MusicQueueEmptyException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * SkipCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 02/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class SkipCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        if(event.getOption("position").isEmpty()) {
            return Mono.justOrEmpty(event.getInteraction().getGuildId())
                    .map(Managers::getGuildManager)
                    .map(GuildManager::getTrackScheduler)
                    .flatMap(MenmuTrackScheduler::skip)
                    .doOnSuccess(unused -> event.sendSuccessInteractionReply(":white_check_mark: Song skipped!").subscribe())
                    .doOnError(MusicQueueEmptyException.class, error -> event.sendErrorInteractionReply(":no_entry_sign: Queue is empty!", null).subscribe()).then();
        } else {
            try {
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                long position = event.getOption("position")
                        .flatMap(ApplicationCommandInteractionOption::getValue)
                        .map(ApplicationCommandInteractionOptionValue::asLong)
                        .get();

                if(position > Integer.MAX_VALUE || position < Integer.MIN_VALUE) {
                    throw new NumberFormatException();
                }

                return Mono.justOrEmpty(event.getInteraction().getGuildId())
                        .map(Managers::getGuildManager)
                        .map(GuildManager::getTrackScheduler)
                        .flatMap(trackScheduler -> trackScheduler.skipTo((int) position))
                        .doOnSuccess(unused -> {
                            event.sendSuccessInteractionReply(":white_check_mark: Skipped to position " + position + " in queue.").subscribe();
                        }).doOnError(InvalidQueuePositionException.class, error ->
                                event.sendErrorInteractionReply(":no_entry_sign: No track in position " + position + " found in queue.", null).subscribe())
                        .then();
            } catch (NumberFormatException e) {
                event.sendErrorInteractionReply(":no_entry_sign: That is not a valid number!", null).subscribe();
            }
        }
        return Mono.empty();
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        final String title = "Command: `skip`";
        final String description = "Stops playing the current track and skips to the next in the guild music queue. " +
                "If a number is specified as a parameter, moves to that position in queue, skipping all songs before it.";
        final String usage = "`/skip`\n`/skip [position]`";
        final String examples = "`/skip`\n`/skip 5`";

        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title(title)
                        .description(description)
                        .addField("Usage", usage, true)
                        .addField("Examples", examples, true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
