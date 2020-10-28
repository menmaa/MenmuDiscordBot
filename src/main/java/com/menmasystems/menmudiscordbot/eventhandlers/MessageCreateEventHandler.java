package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.errorhandlers.UnknownCommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * MessageCreateEventHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class MessageCreateEventHandler implements Consumer<MessageCreateEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MessageCreateEventHandler.class);

    @Override
    public void accept(MessageCreateEvent messageCreateEvent) {
        String msg = messageCreateEvent.getMessage().getContent();

        Mono.just(Menmu.getConfig().cmdPrefix)
                .filter(cmdPrefix -> msg.startsWith(cmdPrefix + "!"))
                .map(cmdPrefix -> Arrays.asList(msg.substring(cmdPrefix.length() + 1).split(" ")))
                .flatMap(params -> messageCreateEvent.getMessage().getChannel()
                        .flatMap(channel -> Menmu.getCommandHandler(params.get(0).toLowerCase())
                                .doOnError(UnknownCommandException.class, unused -> {
                                    String message = ":no_entry_sign: I'm sorry, I don't seem to be able to recognize this command.";
                                    Menmu.sendErrorMessage(channel, message, null);
                                })
                                .flatMap(commandHandler -> commandHandler.handle(messageCreateEvent, channel, params))
                                .doOnError(CommandExecutionException.class, error -> error.createErrorMessage(channel).subscribe())
                                .doOnError(error -> !(error instanceof CommandExecutionException), error ->
                                    logger.error("There was an error trying to execute command " + params.get(0), error)
                                ).thenReturn(channel)
                        )
                        .flatMap(channel -> Mono.justOrEmpty(messageCreateEvent.getGuildId())
                        .map(Menmu::getGuildData)
                        .filter(guildData -> System.currentTimeMillis() - guildData.devPhaseMessage > 21600000)
                        .flatMap(guildData -> channel.createEmbed(embedCreateSpec -> {
                            embedCreateSpec.setColor(Color.YELLOW);
                            embedCreateSpec.setTitle(":warning: Warning!");
                            embedCreateSpec.setDescription("Please be aware I am still in development phase and I may often be " +
                                    "unable to complete some requests or may have other functionality problems. " +
                                    "My creator **Menma** wants to thank you for trying me out and he would like to know all about my problems. " +
                                    "Please notify him if you ever notice I am unable to function properly when I should! " +
                                    "You can contact him in discord at `Menma#0001`. Thank you! :heart:");
                            embedCreateSpec.setFooter("This message is triggered during development phase on every command execution and will not appear again for at least 6 hours afterwards.", null);
                        }).doFinally(onFinally -> guildData.devPhaseMessage = System.currentTimeMillis())))
                ).subscribe();
    }
}
