package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.*;
import com.menmasystems.menmudiscordbot.errorhandler.CommandExecutionException;
import com.menmasystems.menmudiscordbot.errorhandler.YouTubeSearchEmptyResultSetException;
import com.menmasystems.menmudiscordbot.handler.MenmuAudioLoadResultHandler;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * PlayCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 31/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class PlayCommandHandler implements CommandHandler {

    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        if(event.getInteraction().getGuildId().isEmpty())
            return Mono.error(new CommandExecutionException("play", "Guild ID is empty."));

        Snowflake guildId = event.getInteraction().getGuildId().get();
        Member member = event.getInteraction().getMember().get();
        GuildManager guildManager = Managers.getGuildManager(guildId);

        if(event.getOption("input").isEmpty())
            return handleEmptyInput(event, guildManager);

        if(event.getInteraction().getMember().isEmpty())
            return Mono.error(new CommandExecutionException("play", "Member is empty"));

        // noinspection OptionalGetWithoutIsPresent
        String input = event.getOption("input")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get();

        return event.deferReply()
                .then(Mono.defer(() -> {
                    if(input.startsWith("https://") || input.startsWith("http://")) {
                        return Mono.just(new MenmuTrackData(input, member));
                    }

                    return searchYouTube(input, member);
                }))
                .publishOn(Schedulers.boundedElastic())
                .doOnError(YouTubeSearchEmptyResultSetException.class, err -> {
                    var msg = ":no_entry_sign: Search for `" + input + "` returned no results.";
                    var spec = InteractionFollowupCreateSpec.builder().content(msg).build();
                    event.createFollowup(spec).subscribe();
                })
                .doOnNext(trackData -> {
                    Menmu.getPlayerManager().loadItem(trackData.getUrl(), new MenmuAudioLoadResultHandler(guildManager, event, trackData));
                })
                .then();
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        final String title = "Command: `play`";
        final String description = "Adds a song or a playlist to the guild queue and starts playing if not already playing. " +
                "Will also resume playing if previously paused.";
        final String usage = "`/play`\n`/play [link]`\n`/play [youtube search phrase]`";
        final String examples = "`/play`\n`/play https://www.youtube.com/watch?v=jrzUsHNGZHc`\n`/play knock knock penny's door`";

        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title(title)
                        .description(description)
                        .addField("Usage", usage, true)
                        .addField("Examples", examples, true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }

    private Mono<Void> handleEmptyInput(MenmuCommandInteractionEvent event, GuildManager guildManager) {
        AudioPlayer guildAudioPlayer = guildManager.getAudioPlayer();
        MenmuTrackScheduler guildTrackScheduler = guildManager.getTrackScheduler();

        if(guildAudioPlayer.isPaused()) {
            guildAudioPlayer.setPaused(false);
            var spec = InteractionFollowupCreateSpec.builder()
                    .addEmbed(Menmu.createSuccessEmbedSpec(":play_pause: Resuming player..."))
                    .build();

            return event.createFollowup(spec).then();
        }

        if(guildAudioPlayer.getPlayingTrack() != null || !guildTrackScheduler.queue.isEmpty()) {
            return startPlayer(event, guildManager);
        }

        String msg = ":no_entry_sign: Player is not paused, or music queue is empty.";
        var spec = InteractionFollowupCreateSpec.builder()
                .addEmbed(Menmu.createErrorEmbedSpec(msg, null))
                .build();

        return event.createFollowup(spec).then();
    }

    private Mono<MenmuTrackData> searchYouTube(String searchQuery, Member requestedBy) {
        MenmuTrackData trackData = new MenmuTrackData(requestedBy);

        return Mono.justOrEmpty(Menmu.getYoutubeSearch().getYtVideoDataBySearchQuery(searchQuery))
                .switchIfEmpty(Mono.error(new YouTubeSearchEmptyResultSetException(searchQuery + " returned no results.")))
                .doOnNext(result -> {
                    trackData.setUrl("https://www.youtube.com/watch?v=" + result.getId().getVideoId());
                    trackData.setChannelName(result.getSnippet().getChannelTitle());
                    trackData.setThumbnailUrl(result.getSnippet().getThumbnails().getDefault().getUrl());
                    trackData.setYtInfoFetched(true);
                })
                .thenReturn(trackData);
    }

    private Mono<Void> startPlayer(MenmuCommandInteractionEvent event, GuildManager guildManager) {
        if (guildManager.getVoiceConnection() == null) {
            return Menmu.getCommandHandler("join")
                    .cast(JoinCommandHandler.class)
                    .flatMap(commandHandler -> commandHandler.internalHandle(event, true))
                    .doOnSuccess(unused -> guildManager.getTrackScheduler().play()).then();
        }
        return Mono.just(guildManager.getTrackScheduler().play()).then();
    }
}
