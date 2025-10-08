package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * ReadyEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 28/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class ReadyEventHandler implements Consumer<ReadyEvent> {

    private static final String ACTIVITY_MSG = "v" + Menmu.VERSION_NUMBER + " | /help";
    private static final Snowflake MENMA_USER_ID = Snowflake.of(303676987975663616L);

    private static Disposable presenceTask = null;

    @Override
    public void accept(ReadyEvent readyEvent) {
        if(presenceTask != null && !presenceTask.isDisposed()) {
            presenceTask.dispose();
            presenceTask = null;
        }

        ClientPresence presence = ClientPresence.online(ClientActivity.custom(ACTIVITY_MSG));

        presenceTask = readyEvent.getClient().updatePresence(presence)
                .then(readyEvent.getClient().getUserById(MENMA_USER_ID))
                .doOnNext(Menmu::setMenma)
                .then(Mono.delay(Duration.ofHours(1)))
                .repeat()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
