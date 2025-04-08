package com.menmasystems.menmudiscordbot.eventhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;

public class ChatInputInteractionEventHandler implements Consumer<ChatInputInteractionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ChatInputInteractionEventHandler.class);

    @Override
    public void accept(ChatInputInteractionEvent chatInputInteractionEvent) {
        String commandName = chatInputInteractionEvent.getCommandName();

        Menmu.getCommandHandler(commandName)
            .flatMap(commandHandler -> commandHandler.handle(new MenmuCommandInteractionEvent(chatInputInteractionEvent)))
            .onErrorResume(error -> {
                if(error instanceof CommandExecutionException) {
                    return ((CommandExecutionException) error).createErrorMessage(chatInputInteractionEvent).then();
                }

                return Mono.error(error);
            })
            .doOnError(error -> logger.error("There was an error trying to execute command " + commandName, error))
            .subscribe();
    }
}
