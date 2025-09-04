package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException.ErrorType;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * JoinCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class JoinCommandHandler implements CommandHandler {

    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        return internalHandle(event, false);
    }

    public Mono<Void> internalHandle(MenmuCommandInteractionEvent event, boolean followUp) {
        String connectedMsg = ":white_check_mark: Joined `%s` and bound to `%s`.";
        String channelName = event.getInteraction().getData().channel().get().name().get();

        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildManager)
                .flatMap(guildManager -> Mono.justOrEmpty(event.getInteraction().getMember())
                        .flatMap(Member::getVoiceState)
                        .switchIfEmpty(Mono.error(new CommandExecutionException("join", ErrorType.USER_VOICE_STATE_NULL)))
                        .flatMap(VoiceState::getChannel)
                        .zipWith(event.getInteraction().getChannel())
                        .flatMap(tuple -> guildManager.joinVoiceChannel(tuple.getT1(), tuple.getT2()))
                        .map(voiceChannel -> String.format(connectedMsg, voiceChannel.getName(), channelName))
                        .flatMap(message -> event.sendSuccessInteractionReply(message, followUp))
                        .onErrorMap(error -> new CommandExecutionException("join", ErrorType.VOICE_CONNECTION_ERROR, error))
                );
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `join`")
                        .description("Connects me to the voice channel you are currently in.")
                        .addField("Usage", "`/join`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
