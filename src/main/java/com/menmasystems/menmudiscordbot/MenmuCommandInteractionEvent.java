package com.menmasystems.menmudiscordbot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class MenmuCommandInteractionEvent extends ChatInputInteractionEvent {
    public MenmuCommandInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    public MenmuCommandInteractionEvent(ChatInputInteractionEvent event) {
        super(event.getClient(), event.getShardInfo(), event.getInteraction());
    }

    public EmbedCreateSpec createSuccessEmbedSpec(String message) {
        return EmbedCreateSpec.builder().color(Color.GREEN).description(message).build();
    }

    public Mono<Void> sendSuccessInteractionReply(String message, boolean followUp) {
        if(followUp)
            return Mono.just(createSuccessEmbedSpec(message))
                    .map(embedSpec -> InteractionFollowupCreateSpec.builder().addEmbed(embedSpec).build())
                    .flatMap(this::createFollowup).then();

        return Mono.just(createSuccessEmbedSpec(message))
                .map(embedSpec -> InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build())
                .flatMap(this::reply);
    }

    public Mono<Void> sendSuccessInteractionReply(String message) {
        return sendSuccessInteractionReply(message, false);
    }

    public EmbedCreateSpec createErrorEmbedSpec(String message, @Nullable String errorMessage) {
        EmbedCreateSpec.Builder spec = EmbedCreateSpec.builder();
        spec.color(Color.RED);
        spec.description(message);
        if(errorMessage != null)
            spec.addField("Error Message", errorMessage, false);

        return spec.build();
    }

    public Mono<Void> sendErrorInteractionReply(String message, @Nullable String errorMessage, boolean followUp) {
        if(followUp)
            return Mono.just(createErrorEmbedSpec(message, errorMessage))
                    .map(embedSpec -> InteractionFollowupCreateSpec.builder().addEmbed(embedSpec).build())
                    .flatMap(this::createFollowup).then();

        return Mono.just(createErrorEmbedSpec(message, errorMessage))
                .map(embedSpec -> InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build())
                .flatMap(this::reply);
    }

    public Mono<Void> sendErrorInteractionReply(String message, @Nullable String errorMessage) {
        return sendErrorInteractionReply(message, errorMessage, false);
    }
}
