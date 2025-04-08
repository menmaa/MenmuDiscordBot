package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * RepeatCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 01/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class RepeatCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {

        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .map(guildData -> {
                    InteractionApplicationCommandCallbackSpec.Builder specBuilder =
                            InteractionApplicationCommandCallbackSpec.builder();

                    if(guildData.isRepeatCurrentTrack()) {
                        guildData.setRepeatCurrentTrack(false);
                        specBuilder.addEmbed(Menmu.createErrorEmbedSpec(":no_entry: Song repeat disabled.", null));

                    } else {
                        guildData.setRepeatCurrentTrack(true);
                        specBuilder.addEmbed(Menmu.createSuccessEmbedSpec(":white_check_mark: Song repeat enabled."));
                    }

                    return specBuilder.build();
                }).flatMap(event::reply);
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `repeat`")
                        .description("Enables/disables current song repeat.")
                        .addField("Usage", "`/repeat`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
