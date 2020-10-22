package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException.ErrorType;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * JoinCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class JoinCommandHandler implements CommandHandler {

    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        String connecting = ":warning: Connecting to voice... Due to a technical issue, connection process may take up to 1 minute.";
        String connected = ":white_check_mark: Joined `%s` and bound to `%s`.";

        return Mono.justOrEmpty(event.getGuildId())
                .map(Menmu::getGuildData)
                .flatMap(guildData -> channel.createMessage(connecting)
                        .flatMap(msg -> Mono.justOrEmpty(event.getMember())
                                .flatMap(Member::getVoiceState)
                                .switchIfEmpty(Mono.error(new CommandExecutionException("join", ErrorType.USER_VOICE_STATE_NULL)))
                                .flatMap(VoiceState::getChannel)
                                .flatMap(voiceChannel -> voiceChannel.join(spec -> {
                                    spec.setProvider(guildData.getAudioProvider());
                                    spec.setTimeout(Duration.ofMinutes(1));
                                }).doOnSuccess(voiceConnection -> {
                                    TextChannel textChannel = (TextChannel) channel;
                                    guildData.setVoiceConnection(voiceConnection);
                                    guildData.setBoundTextChannel(textChannel);
                                    String message = String.format(connected, voiceChannel.getName(), textChannel.getName());
                                    Menmu.sendSuccessMessage(channel, message);
                                }).onErrorMap(error -> new CommandExecutionException("join", ErrorType.VOICE_CONNECTION_ERROR, error))
                                        .doFinally(unused -> msg.delete().subscribe())))).then();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!join";

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle("Command: `join`");
            embedCreateSpec.setDescription("Connects me to the voice channel you are currently in.");
            embedCreateSpec.addField("Usage", "`"+command+"`", true);
        }).block();
    }
}
