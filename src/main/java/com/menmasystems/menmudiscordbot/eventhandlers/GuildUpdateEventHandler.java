package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Managers;
import discord4j.core.event.domain.guild.GuildUpdateEvent;
import discord4j.core.object.entity.Guild;

import java.util.function.Consumer;

/**
 * GuildUpdateEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 31/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class GuildUpdateEventHandler implements Consumer<GuildUpdateEvent> {
    @Override
    public void accept(GuildUpdateEvent guildUpdateEvent) {
        Guild guild = guildUpdateEvent.getCurrent();
        Managers.getGuildManager(guild.getId()).setGuild(guild);
        Managers.getGuildManager(guild.getId()).getTrackScheduler().setGuild(guild);
    }
}
