package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.errorhandler.CommandExecutionException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ChatInputInteractionEventHandler implements Consumer<ChatInputInteractionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ChatInputInteractionEventHandler.class);

    @Override
    public void accept(ChatInputInteractionEvent event) {
        String commandName = event.getCommandName();

        Menmu.getCommandHandler(commandName)
            .flatMap(commandHandler -> commandHandler.handle(new MenmuCommandInteractionEvent(event)))
            .onErrorResume(CommandExecutionException.class, error -> error.createErrorMessage(event).then())
            .doOnError(error -> logger.error("There was an error trying to execute command {}", commandName, error))
            .subscribe();
    }
}
