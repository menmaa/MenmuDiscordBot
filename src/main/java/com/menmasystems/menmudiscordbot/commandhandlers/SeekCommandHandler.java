package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeParseException;
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
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        if(event.getOption("timestamp").isEmpty()) {
            return event.sendErrorInteractionReply(":no_entry_sign: No time specified.", null);
        }

        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .map(GuildData::getAudioPlayer)
                .map(AudioPlayer::getPlayingTrack)
                .doOnNext(track -> {
                    if(!track.isSeekable()) {
                        event.sendErrorInteractionReply(":no_entry_sign: The current playing track does not support seek.", null).subscribe();
                        return;
                    }

                    try {
                        @SuppressWarnings("OptionalGetWithoutIsPresent")
                        String timestamp = event.getOption("timestamp")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asString)
                                .get();

                        String[] hms = timestamp.split(":");
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
                            throw new DateTimeParseException(null, timestamp, 0);
                        }
                        int time = second + 60 * minute + 3600 * hour;
                        long seekTo = TimeUnit.MILLISECONDS.convert(time, TimeUnit.SECONDS);
                        if(seekTo < track.getDuration()) {
                            track.setPosition(seekTo);
                            event.sendSuccessInteractionReply(String.format(":fast_forward: Seeking to `%02d:%02d:%02d`.", hour, minute, second)).subscribe();
                        } else {
                            event.sendErrorInteractionReply(":no_entry_sign: Time inserted exceeds the duration of the track.", null).subscribe();
                        }
                    } catch (NumberFormatException | DateTimeParseException e) {
                        event.sendErrorInteractionReply(":no_entry_sign: Invalid timestamp. Required format: `minute:second` or `hour:minute:second`.", null).subscribe();
                    }
                }).doOnSuccess(track -> {
                    if(track == null) {
                        event.sendErrorInteractionReply(":no_entry_sign: There is nothing playing right now.", null).subscribe();
                    }
                }).then();
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `seek`")
                        .description("Enables/disables current guild music queue repeat.")
                        .addField("Usage", "`/seek [timestamp formatted in m:ss/h:mm:ss]`", true)
                        .addField("Examples", "`/seek 4:20`\n`/seek 1:13:37`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
