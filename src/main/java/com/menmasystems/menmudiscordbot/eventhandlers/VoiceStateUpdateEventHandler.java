package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Managers;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * VoiceStateUpdateEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class VoiceStateUpdateEventHandler implements Consumer<VoiceStateUpdateEvent> {

    private final Logger logger = LoggerFactory.getLogger(VoiceStateUpdateEventHandler.class);

    @Override
    public void accept(VoiceStateUpdateEvent voiceStateUpdateEvent) {
        VoiceState current = voiceStateUpdateEvent.getCurrent();

        if(!current.getUserId().equals(current.getClient().getSelfId())) return;
        if(voiceStateUpdateEvent.getOld().isEmpty()) return;

        VoiceState old = voiceStateUpdateEvent.getOld().get();
        if(current.getChannelId().isEmpty() && old.getChannelId().isPresent()) {
            GuildManager guildManager = Managers.getGuildManager(current.getGuildId());
            guildManager.setVoiceConnection(null);
            guildManager.setBoundMessageChannel(null);
            logger.info("Left voice channel {} due to VoiceStateUpdateEvent.", old.getChannelId().get().asLong());
        }
    }
}
