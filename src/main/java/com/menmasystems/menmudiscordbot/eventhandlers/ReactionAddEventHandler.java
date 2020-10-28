package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackData;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Image;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * ReactionAddEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 03/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class ReactionAddEventHandler implements Consumer<ReactionAddEvent> {
    @Override
    public void accept(ReactionAddEvent event) {
        if(event.getGuildId().isEmpty()) return;
        if(event.getUserId().equals(event.getClient().getSelfId())) return;

        GuildData guildData = Menmu.getGuildData(event.getGuildId().get());
        Message queueMessage = guildData.getMusicQueueMessage();

        if(queueMessage == null) return;
        if(!event.getMessageId().equals(queueMessage.getId())) return;

        event.getEmoji().asUnicodeEmoji().ifPresent(emoji -> {
            String rawEmoji = emoji.getRaw();
            if(rawEmoji.equals("⬅️") || rawEmoji.equals("➡️")) {
                queueMessage.removeReaction(event.getEmoji(), event.getUserId()).subscribe();
                MenmuTrackScheduler trackScheduler = guildData.getTrackScheduler();
                List<AudioTrack> rTrackList = guildData.getQueueOnRepeat();
                List<AudioTrack> trackList = (rTrackList != null) ? new LinkedList<>(rTrackList) : trackScheduler.getQueueAsList();
                int pages = (int) Math.ceil(trackList.size() / 10.0);
                int currentPage = guildData.getMusicQueuePage();

                if(rawEmoji.equals("➡️") && currentPage < pages)
                    currentPage++;
                else if(rawEmoji.equals("⬅️") && currentPage > 1)
                    currentPage--;
                else return;

                final int page = currentPage;
                queueMessage.edit(messageEditSpec -> messageEditSpec.setEmbed(spec -> {
                    Guild guild = guildData.getGuild();
                    spec.setAuthor(guild.getName(), Menmu.INVITE_URL, guild.getIconUrl(Image.Format.PNG).orElse(null));
                    spec.setTitle("Music Queue" + ((rTrackList != null) ? " - Repeat Enabled" : ""));

                    StringBuilder sb = new StringBuilder();

                    AudioTrack np = guildData.getAudioPlayer().getPlayingTrack();
                    if(np != null) {
                        trackList.add(0, np);
                    }

                    for(int i = page * 10 - 10; i < page * 10; i++) {
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
                    spec.setDescription(sb.toString());

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
                    String footer = "Page %d/%d | %d tracks in queue | %02d:%02d:%02d total queue length";
                    spec.setFooter(String.format(footer, page, pages, trackListSize, hours, minutes, seconds), iconUrl);
                })).subscribe(message -> {
                    guildData.setMusicQueueMessage(message);
                    guildData.setMusicQueuePage(page);
                });
            }
        });
    }
}
