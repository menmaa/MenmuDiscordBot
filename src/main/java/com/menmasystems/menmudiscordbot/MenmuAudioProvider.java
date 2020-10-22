package com.menmasystems.menmudiscordbot;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import discord4j.voice.AudioProvider;

import java.nio.ByteBuffer;

/**
 * MenmuAudioProvider.java
 * Menmu Discord Bot
 * <p>
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class MenmuAudioProvider extends AudioProvider {
    private final MutableAudioFrame frame = new MutableAudioFrame();
    private final AudioPlayer player;

    public MenmuAudioProvider(AudioPlayer player) {
        super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        this.player = player;
        this.frame.setBuffer(getBuffer());
    }

    @Override
    public boolean provide() {
        boolean didProvide = player.provide(frame);
        if (didProvide) getBuffer().flip();
        return didProvide;
    }
}
