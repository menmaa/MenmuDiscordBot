package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
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
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(Menmu::getGuildData)
                .doOnNext(guildData -> {
                    if(guildData.getQueueOnRepeat() == null) {
                        AudioTrack np = guildData.getAudioPlayer().getPlayingTrack();
                        MenmuTrackScheduler trackScheduler = guildData.getTrackScheduler();
                        List<AudioTrack> queueAsList = trackScheduler.getQueueAsList();

                        if(np != null) queueAsList.add(0, np);
                        if(queueAsList.size() == 0) {
                            Menmu.sendErrorMessage(channel, ":no_entry_sign: Queue is empty!", null);
                            return;
                        }

                        guildData.setQueueOnRepeat(queueAsList);
                        Menmu.sendSuccessMessage(channel, ":white_check_mark: Current queue repeat enabled.");
                    } else {
                        guildData.setQueueOnRepeat(null);
                        Menmu.sendErrorMessage(channel, ":no_entry: Current queue repeat disabled.", null);
                    }
                }).then();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!repeatqueue";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `repeatqueue`");
            embedCreateSpec.setDescription("Enables/disables current guild music queue repeat.");
            embedCreateSpec.addField("Usage", "`"+command+"`", true);
        }).block();
    }
}
