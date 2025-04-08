package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ChatInputInteractionEventHandler implements Consumer<ChatInputInteractionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ChatInputInteractionEventHandler.class);

    @Override
    public void accept(ChatInputInteractionEvent chatInputInteractionEvent) {
        String commandName = chatInputInteractionEvent.getCommandName();

        Menmu.getCommandHandler(commandName)
            .flatMap(commandHandler -> commandHandler.handle(chatInputInteractionEvent))
            .doOnError(error -> !(error instanceof CommandExecutionException), error ->
                    logger.error("There was an error trying to execute command " + commandName, error)
            ).subscribe();
    }
}
