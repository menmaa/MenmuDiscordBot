package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * PauseCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 02/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class PauseCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .map(GuildData::getAudioPlayer)
                .doOnNext(audioPlayer -> audioPlayer.setPaused(!audioPlayer.isPaused()))
                .flatMap(audioPlayer -> {
                    if(audioPlayer.isPaused())
                        return event.sendErrorInteractionReply(":play_pause: Player paused.", null);

                    return event.sendSuccessInteractionReply(":play_pause: Resuming player...");
                });
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `pause`")
                        .description("Pauses the music player.")
                        .addField("Usage", "`/pause`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
