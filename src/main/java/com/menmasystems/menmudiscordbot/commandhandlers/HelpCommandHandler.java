package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * HelpCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 06/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class HelpCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        return event.getClient().getSelf()
                .flatMap(self -> {
                    if(event.getOption("command").isPresent()) {
                        //noinspection OptionalGetWithoutIsPresent
                        return event.getOption("command")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asString)
                                .map(Menmu::getCommandHandler)
                                .get()
                                .doOnSuccess(commandHandler -> commandHandler.helpHandler(event))
                                .onErrorResume(error -> {
                                    String message = ":no_entry_sign: I'm sorry, I cannot display help for an invalid command. " +
                                            "Please try command `help` with no parameters for a list of available commands.";
                                    event.sendErrorInteractionReply(message, null).subscribe();
                                    return Mono.empty();
                                }).then();
                    } else {
                        return Mono.just(EmbedCreateSpec.builder())
                                .map(builder -> builder.color(Menmu.DEFAULT_EMBED_COLOR))
                                .map(builder -> builder.author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl()))
                                .map(builder -> builder.thumbnail(self.getAvatarUrl()))
                                .map(builder -> builder.description("Hello! I'm " + self.getUsername() + "! Here is a list of the commands I am responding to. " +
                                        "If you would like to view more information for any of those, use command `/help [command]`." +
                                        "E.g.: `/help play` to display help for command `play`. " +
                                        "[Click here to invite me to your own server!](" + Menmu.INVITE_URL + ")"))
                                .map(builder -> builder.addField("<a:musical_note:752807255497244672> Music Commands", "`play` `pause` `stop` `queue` `clear` " +
                                        "`join` `leave` `repeat` `repeatqueue` `skip` `seek` `remove`", false))
                                .map(builder -> builder.footer("Created by " + Menmu.menma.getTag(), Menmu.menma.getAvatarUrl()))
                                .map(builder -> InteractionApplicationCommandCallbackSpec.builder().addEmbed(builder.build()).build())
                                .flatMap(event::reply);
                    }
                }).then();
    }
}
