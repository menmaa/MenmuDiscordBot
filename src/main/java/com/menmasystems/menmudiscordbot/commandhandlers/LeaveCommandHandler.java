package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.Managers;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.errorhandler.CommandExecutionException;
import com.menmasystems.menmudiscordbot.errorhandler.ErrorMessage;
import com.menmasystems.menmudiscordbot.errorhandler.SelfVoiceNotConnectedException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

/**
 * LeaveCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */
public class LeaveCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getGuildId())
                .map(Managers::getGuildManager)
                .flatMap(GuildManager::leaveVoiceChannel)
                .onErrorMap(SelfVoiceNotConnectedException.class, err -> new CommandExecutionException("leave", ErrorMessage.SELF_VOICE_STATE_NULL))
                .then(event.getClient().getSelf())
                .flatMap(self -> event.sendErrorInteractionReply(":no_entry: Left voice channels. Thanks for trying out " + self.getUsername() + "-san!", null));
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `leave`")
                        .description("Disconnects me from voice channels.")
                        .addField("Usage", "`/leave`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
