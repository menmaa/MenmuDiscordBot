package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackData;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * QueueCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 02/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class QueueCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        if(event.getGuildId().isEmpty())
            return Mono.error(new CommandExecutionException("play", "Guild ID is empty."));

        GuildData guildData = Menmu.getGuildData(event.getGuildId().get());
        AudioPlayer player = guildData.getAudioPlayer();
        MenmuTrackScheduler trackScheduler = guildData.getTrackScheduler();
        List<AudioTrack> rTrackList = guildData.getQueueOnRepeat();
        List<AudioTrack> trackList = (rTrackList != null) ? new LinkedList<>(rTrackList) : trackScheduler.getQueueAsList();

        final AudioTrack np = player.getPlayingTrack();
        if(np != null) {
            trackList.add(0, np);
        }

        if(trackList.size() == 0) {
            Menmu.sendErrorMessage(channel, ":no_entry_sign: Music Queue Empty.", null);
            return Mono.empty();
        }

        Guild guild = guildData.getGuild();
        int pages = (int) Math.ceil(trackList.size() / 10.0);

        if(guildData.getMusicQueueMessage() != null) {
            guildData.getMusicQueueMessage().removeAllReactions().subscribe();
            guildData.setMusicQueueMessage(null);
            guildData.setMusicQueuePage(0);
        }

        return channel.createEmbed(spec -> {
            spec.setAuthor(guild.getName(), Menmu.INVITE_URL, guild.getIconUrl(Image.Format.PNG).orElse(null));
            spec.setTitle("Music Queue" + ((rTrackList != null) ? " - Repeat Enabled" : ""));

            StringBuilder sb = new StringBuilder();

            for(int i = 0; i <= 10; i++) {
                if(i == trackList.size()) break;
                AudioTrack track = trackList.get(i);
                MenmuTrackData trackData = track.getUserData(MenmuTrackData.class);

                long length = track.getInfo().length;
                String duration;
                if(length == Units.DURATION_MS_UNKNOWN)
                    duration = "Live Stream";
                else
                    duration = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(length),
                            TimeUnit.MILLISECONDS.toSeconds(length) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length))
                    );

                if(np != null) {
                    if(i == 0) sb.append("=> [");
                    else sb.append(i).append(". [");
                } else sb.append(i+1).append(". [");

                sb.append(track.getInfo().title);
                sb.append("](");
                sb.append(trackData.url);
                sb.append(") - ");
                sb.append(duration);
                sb.append(" - Requested by ");
                sb.append(trackData.requestedBy.getDisplayName());
                sb.append("\n\n");

                if(np != null && i == 0) {
                    sb.append("\n");
                }
            }
            spec.setDescription(sb.toString());

            String iconUrl = null;
            if(event.getMember().isPresent()) {
                iconUrl = event.getMember().get().getAvatarUrl();
            }

            int trackListSize = trackList.size() - ((np != null) ? 1 : 0);
            long totalLength = 0;
            for(AudioTrack track : trackList) {
                long length = track.getInfo().length;
                if(length == Units.DURATION_MS_UNKNOWN) continue;
                totalLength += length;
            }
            if(np != null) {
                long npLength = np.getInfo().length;
                totalLength -= (npLength != Units.DURATION_MS_UNKNOWN) ? npLength : 0;
            }

            long hours = TimeUnit.MILLISECONDS.toHours(totalLength);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(totalLength) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalLength));
            long seconds = TimeUnit.MILLISECONDS.toSeconds(totalLength) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalLength));
            String footer = "Page 1/%d | %d tracks in queue | %02d:%02d:%02d total queue length";
            spec.setFooter(String.format(footer, pages, trackListSize, hours, minutes, seconds), iconUrl);
        }).doOnNext(message -> {
            if(pages > 1) {
                message.addReaction(ReactionEmoji.unicode("⬅️")).subscribe();
                message.addReaction(ReactionEmoji.unicode("➡️")).subscribe();

                guildData.setMusicQueueMessage(message);
                guildData.setMusicQueuePage(1);
            }
        }).then();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!queue";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `queue`");
            embedCreateSpec.setDescription("Displays the contents of the guild music queue.");
            embedCreateSpec.addField("Usage", "`"+command+"`", true);
        }).block();
    }
}
