package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;

import java.util.function.Consumer;

/**
 * VoiceStateUpdateEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class VoiceStateUpdateEventHandler implements Consumer<VoiceStateUpdateEvent> {

    @Override
    public void accept(VoiceStateUpdateEvent voiceStateUpdateEvent) {
        VoiceState current = voiceStateUpdateEvent.getCurrent();
        if(current.getUserId().equals(current.getClient().getSelfId())) {
            if(voiceStateUpdateEvent.getOld().isEmpty()) return;
            VoiceState old = voiceStateUpdateEvent.getOld().get();
            if(current.getChannelId().isEmpty() && old.getChannelId().isPresent()) {
                GuildData guildData = Menmu.getGuildData(current.getGuildId());
                guildData.setVoiceConnection(null).then(guildData.setBoundMessageChannel(null));
            }
        }
    }
}
