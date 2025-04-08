package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

/**
 * ClearCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 01/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class ClearCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .map(GuildData::getTrackScheduler)
                .map(MenmuTrackScheduler::purgeQueue)
                .flatMap(queue -> {
                    EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();
                    embedBuilder.color(Color.GREEN);
                    embedBuilder.description(":white_check_mark: Music queue purged.");

                    InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder()
                            .addEmbed(embedBuilder.build()).build();

                    return event.reply(spec);
                });
    }

    @Override
    public void helpHandler(ChatInputInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `clear`")
                        .description("Clears/purges the guild music queue.")
                        .addField("Usage", "`/clear`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
