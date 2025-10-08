package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Managers;
import discord4j.core.event.domain.guild.GuildDeleteEvent;

import java.util.function.Consumer;

/**
 * GuildDeleteEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class GuildDeleteEventHandler implements Consumer<GuildDeleteEvent> {
    @Override
    public void accept(GuildDeleteEvent guildDeleteEvent) {
        Managers.removeConnectedGuild(guildDeleteEvent.getGuildId());
    }
}
