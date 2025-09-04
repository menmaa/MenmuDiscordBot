package com.menmasystems.menmudiscordbot.manager;

import com.menmasystems.menmudiscordbot.MenmuAudioProvider;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.errorhandlers.VoiceChannelNotConnected;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * GuildData.java
 * Menmu Discord Bot
 *
 * Created by Menma on 28/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class GuildManager {

    private Guild guild;
    private final AudioPlayer audioPlayer;
    private final MenmuAudioProvider audioProvider;
    private final MenmuTrackScheduler trackScheduler;
    private VoiceConnection voiceConnection;
    private MessageChannel boundMessageChannel;
    private boolean repeatCurrentTrack;
    private Message musicQueueMessage;
    private int musicQueuePage;
    private List<AudioTrack> queueOnRepeat;
    public long devPhaseMessage = 0;

    public GuildManager(Guild guild, AudioPlayer audioPlayer, MenmuTrackScheduler trackScheduler) {
        this.setGuild(guild);
        this.audioPlayer = audioPlayer;
        this.trackScheduler = trackScheduler;
        this.audioProvider = new MenmuAudioProvider(audioPlayer);
    }

    public Mono<VoiceChannel> joinVoiceChannel(VoiceChannel voiceChannel, MessageChannel boundChannel) {
        VoiceChannelJoinSpec spec = VoiceChannelJoinSpec.builder()
                .provider(getAudioProvider())
                .selfDeaf(true)
                .build();

        return voiceChannel.join(spec)
                .doOnSuccess(voiceConnection -> {
                    setVoiceConnection(voiceConnection);
                    setBoundMessageChannel(boundChannel);
                })
                .thenReturn(voiceChannel);
    }

    public Mono<Void> leaveVoiceChannel() {
        return Mono.justOrEmpty(getVoiceConnection())
                .switchIfEmpty(Mono.error(new VoiceChannelNotConnected()))
                .flatMap(VoiceConnection::disconnect)
                .doOnSuccess(_ -> {
                    setVoiceConnection(null);
                    setBoundMessageChannel(null);
                });
    }

    public VoiceConnection getVoiceConnection() {
        return voiceConnection;
    }

    public void setVoiceConnection(VoiceConnection voiceConnection) {
        this.voiceConnection = voiceConnection;
    }

    public MessageChannel getBoundMessageChannel() {
        return boundMessageChannel;
    }

    public void setBoundMessageChannel(MessageChannel boundMessageChannel) {
        this.boundMessageChannel = boundMessageChannel;
    }

    public MenmuAudioProvider getAudioProvider() {
        return audioProvider;
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public MenmuTrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public boolean isRepeatCurrentTrack() {
        return repeatCurrentTrack;
    }

    public void setRepeatCurrentTrack(boolean repeatCurrentTrack) {
        this.repeatCurrentTrack = repeatCurrentTrack;
    }

    public Message getMusicQueueMessage() {
        return musicQueueMessage;
    }

    public void setMusicQueueMessage(Message musicQueueMessage) {
        this.musicQueueMessage = musicQueueMessage;
    }

    public int getMusicQueuePage() {
        return musicQueuePage;
    }

    public void setMusicQueuePage(int musicQueuePage) {
        this.musicQueuePage = musicQueuePage;
    }

    public List<AudioTrack> getQueueOnRepeat() {
        return queueOnRepeat;
    }

    public void setQueueOnRepeat(List<AudioTrack> queueOnRepeat) {
        this.queueOnRepeat = queueOnRepeat;
    }
}
