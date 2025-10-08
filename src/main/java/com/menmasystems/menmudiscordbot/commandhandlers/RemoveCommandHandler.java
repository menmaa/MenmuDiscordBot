package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Managers;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.errorhandler.InvalidQueuePositionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * RemoveCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 03/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class RemoveCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
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
                    .flatMap(trackScheduler -> trackScheduler.removeQueue((int) position))
                    .doOnSuccess(removed -> {
                        String msg = ":no_entry: Track `%s` has been removed from the music queue.";
                        event.sendErrorInteractionReply(String.format(msg, removed.getInfo().title), null).subscribe();
                    })
                    .onErrorResume(InvalidQueuePositionException.class, error -> {
                        String msg = ":no_entry_sign: Track does not exist in the music queue.";
                        return event.sendErrorInteractionReply(msg, null).then(Mono.empty());
                    })
                    .then();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.sendErrorInteractionReply(":no_entry_sign: Invalid position number or no number provided. Correct Usage: `remove [position in queue]`. e.g.: `remove 3`", null).subscribe();
        }
        return Mono.empty();
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `remove`")
                        .description("Removes the entry in the specified position from the guild music queue.")
                        .addField("Usage", "`/remove [position]`", true)
                        .addField("Example", "`/remove 3", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
