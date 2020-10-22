package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * HelpCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 06/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class HelpCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {

        return event.getClient().getSelf()
                .flatMap(self -> {
                    if(params.size() >= 2) {
                        return Menmu.getCommandHandler(params.get(1))
                                .doOnSuccess(commandHandler -> commandHandler.helpHandler(channel, self))
                                .onErrorResume(error -> {
                                    String message = ":no_entry_sign: I'm sorry, I cannot display help for an invalid command. " +
                                            "Please try command `help` with no parameters for a list of available commands.";
                                    Menmu.sendErrorMessage(channel, message, null);
                                    return Mono.empty();
                                }).then();
                    } else {
                        return channel.createEmbed(spec -> {
                            final String cmd = Menmu.getConfig().cmdPrefix + "!help";
                            spec.setColor(Menmu.DEFAULT_EMBED_COLOR);
                            spec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
                            spec.setThumbnail(self.getAvatarUrl());
                            spec.setDescription("Hello! I'm " + self.getUsername() + "! Here is a list of the commands I am responding to. " +
                                    "If you would like to view more information for any of those, use command `"+cmd+" [command]`." +
                                    "E.g.: `"+cmd+" play` to display help for command `play`. " +
                                    "[Click here to invite me to your own server!](" + Menmu.INVITE_URL + ")");
                            spec.addField("<a:musical_note:752807255497244672> Music Commands", "`play` `pause` `stop` `queue` `clear` " +
                                    "`join` `leave` `repeat` `repeatqueue` `skip` `seek` `remove`", false);
                            spec.addField("<a:rainbowuwu:752806107197603901> Fun Commands", "`kill` `hug` `wink` `kiss` `punch`", false);
                            spec.setFooter("Created by " + Menmu.menma.getTag(), Menmu.menma.getAvatarUrl());
                        });
                    }
                }).then();
    }
}
