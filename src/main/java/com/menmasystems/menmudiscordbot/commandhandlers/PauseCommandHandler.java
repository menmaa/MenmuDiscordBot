package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .map(GuildData::getAudioPlayer)
                .doOnNext(audioPlayer -> {
                    if(!audioPlayer.isPaused()) {
                        audioPlayer.setPaused(true);
                        Menmu.sendErrorInteractionReply(event, ":play_pause: Player paused.", null).subscribe();
                    } else {
                        audioPlayer.setPaused(false);
                        Menmu.sendSuccessInteractionReply(event, ":play_pause: Resuming player...").subscribe();
                    }
                }).then();
    }

    @Override
    public void helpHandler(ChatInputInteractionEvent event) {
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
