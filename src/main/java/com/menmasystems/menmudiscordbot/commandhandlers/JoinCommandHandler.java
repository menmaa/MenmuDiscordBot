package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException.ErrorType;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
        String connected = ":white_check_mark: Joined `%s` and bound to `%s`.";

        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Menmu::getGuildData)
                .flatMap(guildData -> Mono.justOrEmpty(event.getInteraction().getMember())
                        .flatMap(Member::getVoiceState)
                        .switchIfEmpty(Mono.error(new CommandExecutionException("join", ErrorType.USER_VOICE_STATE_NULL)))
                        .flatMap(VoiceState::getChannel)
                        .flatMap(voiceChannel -> voiceChannel.join(spec -> {
                            spec.setProvider(guildData.getAudioProvider());
                            spec.setTimeout(Duration.ofMinutes(1));
                        })
                        .flatMap(guildData::setVoiceConnection)
                        .then(event.getInteraction().getChannel())
                        .flatMap(guildData::setBoundMessageChannel)
                        .doOnSuccess(channel -> {
                            String channelName = event.getInteraction().getData().channel().get().name().get();
                            String message = String.format(connected, voiceChannel.getName(), channelName);
                            event.sendSuccessInteractionReply(message, followUp).subscribe();
                        })
                        .onErrorMap(error -> new CommandExecutionException("join", ErrorType.VOICE_CONNECTION_ERROR, error))))
                .then();
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
