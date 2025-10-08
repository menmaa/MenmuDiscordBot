package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Managers;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * StopCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 07/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class StopCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Managers::getGuildManager)
                .map(GuildManager::getTrackScheduler)
                .map(MenmuTrackScheduler::stop)
                .then(event.sendErrorInteractionReply(":stop_button: Music player stopped!", null));
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `stop`")
                        .description("Stops playing current track if already playing.")
                        .addField("Usage", "`/stop`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
