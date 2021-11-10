package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * ReadyEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 28/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class ReadyEventHandler implements Consumer<ReadyEvent> {

    Logger logger = LoggerFactory.getLogger(ReadyEventHandler.class);

    @Override
    public void accept(ReadyEvent readyEvent) {
        if(Menmu.presenceTask == null) {
            Menmu.presenceTask = Menmu.getGpScheduledExecutor().scheduleAtFixedRate(() -> {
                try {
                    String activityMsg = "v" + Menmu.VERSION_NUMBER + " | m!help";
                    readyEvent.getClient().updatePresence(ClientPresence.online(ClientActivity.playing(activityMsg))).block();
                    Menmu.menma = readyEvent.getClient().getUserById(Snowflake.of(303676987975663616L)).block();
                } catch (RuntimeException e) {
                    logger.error("Runtime exception occured when trying to execute presence task.", e);
                }
            }, 0, 1, TimeUnit.MINUTES);
        }
    }
}
