package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Guild;

import java.util.function.Consumer;

/**
 * GuildCreateEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class GuildCreateEventHandler implements Consumer<GuildCreateEvent> {
    @Override
    public void accept(GuildCreateEvent guildCreateEvent) {
        Guild guild = guildCreateEvent.getGuild();
        AudioPlayer player = Menmu.getPlayerManager().createPlayer();
        MenmuTrackScheduler trackScheduler = new MenmuTrackScheduler(guild, player);
        player.addListener(trackScheduler);
        Menmu.addConnectedGuild(guild.getId(), new GuildData(guild, player, trackScheduler));
    }
}
