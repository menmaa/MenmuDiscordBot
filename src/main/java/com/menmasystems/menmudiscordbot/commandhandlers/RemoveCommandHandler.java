package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.InvalidQueuePositionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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
    public Mono<Void> handle(ChatInputInteractionEvent event) {
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
                    .map(Menmu::getGuildData)
                    .map(GuildData::getTrackScheduler)
                    .flatMap(trackScheduler -> trackScheduler.removeQueue((int) position))
                    .doOnSuccess(removed -> {
                        String msg = ":no_entry: Track `%s` has been removed from the music queue.";
                        Menmu.sendErrorInteractionReply(event, String.format(msg, removed.getInfo().title), null).subscribe();
                    })
                    .onErrorResume(error -> error instanceof InvalidQueuePositionException, error -> {
                        Menmu.sendErrorInteractionReply(event, ":no_entry_sign: Track does not exist in the music queue.", null).subscribe();
                        return Mono.empty();
                    }).then();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            Menmu.sendErrorInteractionReply(event, ":no_entry_sign: Invalid position number or no number provided. Correct Usage: `remove [position in queue]`. e.g.: `remove 3`", null).subscribe();
        }
        return Mono.empty();
    }

    @Override
    public void helpHandler(ChatInputInteractionEvent event) {
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
