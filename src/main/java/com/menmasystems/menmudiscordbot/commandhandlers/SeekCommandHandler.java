package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SeekCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 04/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class SeekCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        if(params.size() < 2) {
            Menmu.sendErrorMessage(channel, ":no_entry_sign: No time specified.", null);
            return Mono.empty();
        }

        return Mono.justOrEmpty(event.getGuildId())
                .map(Menmu::getGuildData)
                .map(GuildData::getAudioPlayer)
                .map(AudioPlayer::getPlayingTrack)
                .doOnNext(track -> {
                    if(!track.isSeekable()) {
                        Menmu.sendErrorMessage(channel, ":no_entry_sign: The current playing track does not support seek.", null);
                        return;
                    }

                    try {
                        String[] hms = params.get(1).split(":");
                        int hour, minute, second;
                        if (hms.length == 3) {
                            hour = Integer.parseInt(hms[0]);
                            minute = Integer.parseInt(hms[1]);
                            second = Integer.parseInt(hms[2]);
                        } else if (hms.length == 2) {
                            hour = 0;
                            minute = Integer.parseInt(hms[0]);
                            second = Integer.parseInt(hms[1]);
                        } else {
                            throw new DateTimeParseException(null, params.get(1), 0);
                        }
                        int time = second + 60 * minute + 3600 * hour;
                        long seekTo = TimeUnit.MILLISECONDS.convert(time, TimeUnit.SECONDS);
                        if(seekTo < track.getDuration()) {
                            track.setPosition(seekTo);
                            Menmu.sendSuccessMessage(channel, String.format(":fast_forward: Seeking to `%02d:%02d:%02d`.", hour, minute, second));
                        } else {
                            Menmu.sendErrorMessage(channel, ":no_entry_sign: Time inserted exceeds the duration of the track.", null);
                        }
                    } catch (NumberFormatException | DateTimeParseException e) {
                        Menmu.sendErrorMessage(channel, ":no_entry_sign: Invalid timestamp. Required format: `minute:second` or `hour:minute:second`.", null);
                    }
                }).doOnSuccess(track -> {
                    if(track == null) {
                        Menmu.sendErrorMessage(channel, ":no_entry_sign: There is nothing playing right now.", null);
                    }
                }).then();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!seek";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `seek`");
            embedCreateSpec.setDescription("Seeks to the inserted timestamp position in the currently playing song.");
            embedCreateSpec.addField("Usage", "`"+command+" [timestamp formatted in m:ss/h:mm:ss]`", true);
            embedCreateSpec.addField("Examples", "`"+command+" 4:20`\n`"+command+" 1:13:37`", true);
        }).block();
    }
}
