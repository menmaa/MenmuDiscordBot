package com.menmasystems.menmudiscordbot.interfaces;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * CommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public interface CommandHandler {
    Mono<Void> handle(MenmuCommandInteractionEvent event);

    default void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                    .color(Menmu.DEFAULT_EMBED_COLOR)
                    .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                    .description("No Help Message provided for this command.")
                    .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
