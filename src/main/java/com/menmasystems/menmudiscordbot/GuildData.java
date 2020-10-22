package com.menmasystems.menmudiscordbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.voice.VoiceConnection;

import java.util.List;

/**
 * GuildData.java
 * Menmu Discord Bot
 *
 * Created by Menma on 28/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class GuildData {

    private Guild guild;
    private final AudioPlayer audioPlayer;
    private final MenmuAudioProvider audioProvider;
    private final MenmuTrackScheduler trackScheduler;
    private VoiceConnection voiceConnection;
    private TextChannel boundTextChannel;
    private boolean repeatCurrentTrack;
    private Message musicQueueMessage;
    private int musicQueuePage;
    private List<AudioTrack> queueOnRepeat;
    public long devPhaseMessage = 0;

    public GuildData(Guild guild, AudioPlayer audioPlayer, MenmuTrackScheduler trackScheduler) {
        this.setGuild(guild);
        this.audioPlayer = audioPlayer;
        this.trackScheduler = trackScheduler;
        this.audioProvider = new MenmuAudioProvider(audioPlayer);
    }

    public VoiceConnection getVoiceConnection() {
        return voiceConnection;
    }

    public void setVoiceConnection(VoiceConnection voiceConnection) {
        this.voiceConnection = voiceConnection;
    }

    public TextChannel getBoundTextChannel() {
        return boundTextChannel;
    }

    public void setBoundTextChannel(TextChannel boundTextChannel) {
        this.boundTextChannel = boundTextChannel;
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
