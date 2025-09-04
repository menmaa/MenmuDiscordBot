package com.menmasystems.menmudiscordbot;

import com.google.api.services.youtube.model.VideoSnippet;
import com.menmasystems.menmudiscordbot.errorhandlers.InvalidQueuePositionException;
import com.menmasystems.menmudiscordbot.errorhandlers.MusicQueueEmptyException;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * MenmuTrackScheduler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 28/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class MenmuTrackScheduler extends AudioEventAdapter {

    private Guild guild;
    private final AudioPlayer audioPlayer;
    public BlockingQueue<AudioTrack> queue;

    public MenmuTrackScheduler(Guild guild, AudioPlayer audioPlayer) {
        this.setGuild(guild);
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
    }

    public MenmuTrackScheduler play() {
        if(audioPlayer.getPlayingTrack() == null) {
            AudioTrack next = queue.poll();
            GuildManager guildManager = Menmu.getGuildManager(guild.getId());
            if(next == null && guildManager.getQueueOnRepeat() != null) {
                List<AudioTrack> repeatingQueue = Menmu.getGuildManager(guild.getId()).getQueueOnRepeat();
                for(AudioTrack track : repeatingQueue) {
                    queue.offer(track.makeClone());
                }

                audioPlayer.startTrack(queue.poll(), false);
            } else audioPlayer.startTrack(next, false);
        }
        return this;
    }

    public MenmuTrackScheduler stop() {
        if(audioPlayer.getPlayingTrack() != null) {
            audioPlayer.stopTrack();
        }
        return this;
    }

    public Mono<MenmuTrackScheduler> skip() {
        AudioTrack np = audioPlayer.getPlayingTrack();
        if (queue.size() == 0 && np == null) {
            return Mono.error(new MusicQueueEmptyException());
        }

        return Mono.just(this)
                .map(MenmuTrackScheduler::stop)
                .map(MenmuTrackScheduler::play);
    }

    public Mono<MenmuTrackScheduler> skipTo(int position) {
        List<AudioTrack> trackList = getQueueAsList();

        if(position >= 1 && position <= trackList.size()) {
            queue.clear();

            for(AudioTrack track : trackList.subList(position-1, trackList.size()))
                queue.offer(track);

            return Mono.just(this)
                    .map(MenmuTrackScheduler::stop)
                    .map(MenmuTrackScheduler::play);
        }
        return Mono.error(new InvalidQueuePositionException());
    }

    public void queue(AudioTrack track) {
        queue.offer(track);

        List<AudioTrack> repeatingQueue = Menmu.getGuildManager(guild.getId()).getQueueOnRepeat();
        if(repeatingQueue != null)
            repeatingQueue.add(track);
    }

    public MenmuTrackScheduler purgeQueue() {
        queue.clear();

        List<AudioTrack> repeatingQueue = Menmu.getGuildManager(guild.getId()).getQueueOnRepeat();
        if(repeatingQueue != null)
            repeatingQueue.clear();

        return this;
    }

    public List<AudioTrack> getQueueAsList() {
        return new LinkedList<>(queue);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        MenmuTrackData trackData = track.getUserData(MenmuTrackData.class);
        if(!trackData.onRepeat && Menmu.getGuildManager(guild.getId()).getQueueOnRepeat() == null) {
            if(track.getSourceManager().getSourceName().equals("youtube") && !trackData.ytInfoFetched) {
                trackData.url = "https://www.youtube.com/watch?v=" + track.getIdentifier();
                VideoSnippet snippet = Menmu.getYoutubeSearch().getYtVideoDataById(track.getIdentifier());
                if(snippet != null) {
                    trackData.channelName = snippet.getChannelTitle();
                    trackData.thumbnailUrl = snippet.getThumbnails().getDefault().getUrl();
                    trackData.ytInfoFetched = true;
                }
            }

            MessageChannel channel = Menmu.getGuildManager(getGuild().getId()).getBoundMessageChannel();

            long length = track.getInfo().length;
            String duration;
            if(length == Units.DURATION_MS_UNKNOWN)
                duration = "Live Stream";
            else
                duration = String.format("%02d min, %02d sec",
                        TimeUnit.MILLISECONDS.toMinutes(length),
                        TimeUnit.MILLISECONDS.toSeconds(length) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length))
                );

            String title = (track.getInfo().title != null) ? track.getInfo().title : "Unknown Title";
            channel.createEmbed(spec -> {
                spec.setColor(Color.GREEN);
                spec.setAuthor("Now Playing", Menmu.INVITE_URL, getGuild().getIconUrl(Image.Format.PNG).orElse(null));
                spec.setTitle(title);
                spec.setUrl(trackData.url);
                spec.setThumbnail(trackData.thumbnailUrl);
                spec.setTimestamp(trackData.dateTimeRequested);
                spec.addField("Channel Name", trackData.channelName, true);
                spec.addField("Duration", duration, true);
                spec.setFooter("Requested by " + trackData.requestedBy.getDisplayName(), trackData.requestedBy.getAvatarUrl());
            }).subscribe();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        MenmuTrackData trackData = track.getUserData(MenmuTrackData.class);
        if(endReason.mayStartNext) {
            GuildManager guildManager = Menmu.getGuildManager(guild.getId());
            if(guildManager.isRepeatCurrentTrack()) {
                trackData.onRepeat = true;
                player.startTrack(track.makeClone(), false);
            } else {
                AudioTrack next = queue.poll();
                if(next == null && guildManager.getQueueOnRepeat() != null) {
                    List<AudioTrack> repeatingQueue = Menmu.getGuildManager(guild.getId()).getQueueOnRepeat();
                    for(AudioTrack rTrack : repeatingQueue) {
                        queue.offer(rTrack.makeClone());
                    }

                    audioPlayer.startTrack(queue.poll(), false);
                } else audioPlayer.startTrack(next, false);
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        MessageChannel channel = Menmu.getGuildManager(getGuild().getId()).getBoundMessageChannel();
        String message = ":no_entry_sign: Eh... I'm sorry but I was unable to play `" + track.getInfo().title + "`. Skipping ahead.";
        Menmu.sendErrorMessage(channel, message, exception.getMessage()).subscribe();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {

    }

    public Mono<AudioTrack> removeQueue(int position) {
        GuildManager guildManager = Menmu.getGuildManager(guild.getId());
        List<AudioTrack> audioTracks = guildManager.getQueueOnRepeat();

        if(audioTracks == null)
            audioTracks = getQueueAsList();

        if(position - 1 < 0 || position - 1 >= audioTracks.size())
            return Mono.error(new InvalidQueuePositionException());

        AudioTrack removed = audioTracks.remove(position - 1);
        queue.clear();
        for(AudioTrack track : audioTracks) {
            if(track.getState() != AudioTrackState.INACTIVE) continue;
            queue.offer(track);
        }
        if(guildManager.getQueueOnRepeat() != null) {
            guildManager.setQueueOnRepeat(audioTracks);
        }
        return Mono.just(removed);
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }
}
