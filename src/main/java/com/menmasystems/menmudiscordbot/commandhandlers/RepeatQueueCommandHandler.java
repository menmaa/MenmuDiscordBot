package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RepeatQueueCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 03/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class RepeatQueueCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .doOnNext(guildData -> {
                    if(guildData.getQueueOnRepeat() == null) {
                        AudioTrack np = guildData.getAudioPlayer().getPlayingTrack();
                        MenmuTrackScheduler trackScheduler = guildData.getTrackScheduler();
                        List<AudioTrack> queueAsList = trackScheduler.getQueueAsList();

                        if(np != null) queueAsList.add(0, np);
                        if(queueAsList.isEmpty()) {
                            Menmu.sendErrorInteractionReply(event, ":no_entry_sign: Queue is empty!", null).subscribe();
                            return;
                        }

                        guildData.setQueueOnRepeat(queueAsList);
                        Menmu.sendSuccessInteractionReply(event, ":white_check_mark: Current queue repeat enabled.").subscribe();
                    } else {
                        guildData.setQueueOnRepeat(null);
                        Menmu.sendErrorInteractionReply(event, ":no_entry: Current queue repeat disabled.", null).subscribe();
                    }
                }).then();
    }

    @Override
    public void helpHandler(ChatInputInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `repeatqueue`")
                        .description("Enables/disables current guild music queue repeat.")
                        .addField("Usage", "`/repeatqueue`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
