package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * KissCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 06/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class KissCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        if(event.getGuildId().isEmpty())
            return Mono.error(new CommandExecutionException("kiss", "Guild ID is empty."));

        if(event.getMember().isEmpty())
            return Mono.error(new CommandExecutionException("kiss", "Member is empty."));

        try {
            List<User> users = event.getMessage().getUserMentions().collectSortedList().block();
            if(users == null) return Mono.error(new CommandExecutionException("kiss", "users is null."));
            if(users.size() == 0 || users.size() > 5) {
                Menmu.sendErrorMessage(channel, ":no_entry_sign: You need to mention up to a maximum of 5 people. " +
                        "Check command `help kiss` for usage information.", null);
                return Mono.empty();
            }

            Member author = event.getMember().get();

            StringBuilder sb = new StringBuilder(author.getDisplayName()).append(" is trying to kiss ");
            for(int i = 0; i < 5; i++) {
                if(i == users.size()) break;
                Member mentioned = users.get(i).asMember(event.getGuildId().get()).block();
                if(mentioned == null) return Mono.error(new CommandExecutionException("kiss", "mentioned is null"));
                sb.append(mentioned.getDisplayName()).append(", ");
            }
            int lastComma = sb.lastIndexOf(", ");
            sb.delete(lastComma, lastComma+2).append(". UwU");

            channel.createEmbed(embedCreateSpec -> {
                embedCreateSpec.setAuthor(sb.toString(), null, author.getAvatarUrl());
                embedCreateSpec.setImage(String.format("%skiss%d.gif", Menmu.RES_URL, Menmu.rng(5)));
                embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            }).block();
        } catch (RuntimeException e) {
            return Mono.error(e);
        }

        return Mono.empty();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!kiss";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `kiss`");
            embedCreateSpec.setDescription("Attempt to kiss a friend. Or friends :)");
            embedCreateSpec.addField("Usage", "`"+command+" [@User Mentions] (Up to 5)`", true);
            embedCreateSpec.addField("Examples", "`"+command+" @" + Menmu.menma.getTag() + "`\n" +
                    "`"+command+" @" + Menmu.menma.getTag() + " @" + self.getTag() + "`", true);
        }).block();
    }
}
