package com.menmasystems.menmudiscordbot.commandhandlers;

import com.google.api.services.youtube.model.SearchResult;
import com.menmasystems.menmudiscordbot.*;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

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

        GuildData guildData = Menmu.getGuildData(event.getInteraction().getGuildId().get());
        List<ApplicationCommandInteractionOption> options = event.getOptions();

        if(event.getOption("url").isPresent()) {
            if(event.getInteraction().getMember().isEmpty()) return Mono.error(new CommandExecutionException("play", "Member is empty"));

            final String loadItem;
            MenmuTrackData trackData = new MenmuTrackData(event.getInteraction().getMember().get());

            @SuppressWarnings("OptionalGetWithoutIsPresent")
            String url = event.getOption("url")
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .get();

            event.deferReply().block();

            if(url.startsWith("http://") || url.startsWith("https://")) {
                // Looks like a url, just load it up for request on player manager later.
                loadItem = trackData.url = url;
            } else {
                // Assuming text, call search on YouTube API see if we can get results
                try {
                    SearchResult ytResults = Menmu.getYoutubeSearch().getYtVideoDataBySearchQuery(url);
                    if(ytResults != null) {
                        loadItem = trackData.url = "https://www.youtube.com/watch?v=" + ytResults.getId().getVideoId();
                        trackData.channelName = ytResults.getSnippet().getChannelTitle();
                        trackData.thumbnailUrl = ytResults.getSnippet().getThumbnails().getDefault().getUrl();
                        trackData.ytInfoFetched = true;
                    } else {
                        String msg = ":no_entry_sign: Search for `" + url + "` returned no results.";
                        InteractionFollowupCreateSpec spec = InteractionFollowupCreateSpec.builder().content(msg).build();
                        event.createFollowup(spec).block();
                        return Mono.empty();
                    }
                } catch (RuntimeException e) {
                    return Mono.error(e);
                }
            }

            // Now we can attempt to load the track or playlist.
            Menmu.getPlayerManager().loadItem(loadItem, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    trackData.dateTimeRequested = Instant.now();
                    track.setUserData(trackData);
                    MenmuTrackScheduler trackScheduler = guildData.getTrackScheduler();
                    trackScheduler.queue(track);
                    List<AudioTrack> repeatingQueue = guildData.getQueueOnRepeat();
                    int size = (repeatingQueue != null) ? repeatingQueue.size() : trackScheduler.queue.size();
                    EmbedCreateSpec spec = Menmu.createSuccessEmbedSpec(
                            String.format(":white_check_mark: Enqueued `%s` to position %d", track.getInfo().title, size)
                    );
                    event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(spec).build()).block();

                    play(event, guildData).doOnError(e -> {
                        if(e instanceof CommandExecutionException)
                            ((CommandExecutionException) e).createErrorMessage(event, true).subscribe();
                        String msg = ":no_entry_sign: Cannot auto start playing. Use command `play` to play manually.";
                        EmbedCreateSpec errorSpec = Menmu.createErrorEmbedSpec(msg, null);
                        event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(errorSpec).build()).block();
                    }).subscribe();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for(AudioTrack track : playlist.getTracks()) {
                        MenmuTrackData menmuTrackData = new MenmuTrackData(event.getInteraction().getMember().get());
                        menmuTrackData.dateTimeRequested = Instant.now();
                        if(track.getSourceManager().getSourceName().equals("youtube"))
                            menmuTrackData.url = "https://www.youtube.com/watch?v=" + track.getIdentifier();
                        track.setUserData(menmuTrackData);
                        guildData.getTrackScheduler().queue(track);
                    }
                    EmbedCreateSpec spec = Menmu.createSuccessEmbedSpec(
                            String.format(":white_check_mark: Enqueued %d songs from playlist `%s`",
                                    playlist.getTracks().size(), playlist.getName()));
                    event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(spec).build()).block();

                    play(event, guildData).doOnError(e -> {
                        if(e instanceof CommandExecutionException)
                            ((CommandExecutionException) e).createErrorMessage(event, true).subscribe();
                        String msg = ":no_entry_sign: Cannot auto start playing. Use command `play` to play manually.";
                        EmbedCreateSpec errorSpec = Menmu.createErrorEmbedSpec(msg, null);
                        event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(errorSpec).build()).block();
                    }).subscribe();
                }

                @Override
                public void noMatches() {
                    EmbedCreateSpec spec = Menmu.createErrorEmbedSpec(":no_entry_sign: Error: No Matches", null);
                    event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(spec).build()).block();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    String message = ":no_entry_sign: Eh... I'm sorry, but I was unable to load that track. Please try again.";
                    EmbedCreateSpec spec = Menmu.createErrorEmbedSpec(message, exception.getMessage());
                    event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(spec).build()).block();
                }
            });
        } else {
            AudioPlayer guildAudioPlayer = guildData.getAudioPlayer();
            MenmuTrackScheduler guildTrackScheduler = guildData.getTrackScheduler();
            if(guildAudioPlayer.isPaused()) {
                guildAudioPlayer.setPaused(false);
                EmbedCreateSpec spec = Menmu.createSuccessEmbedSpec(":play_pause: Resuming player...");
                event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(spec).build()).block();
            } else if(guildAudioPlayer.getPlayingTrack() != null || !guildTrackScheduler.queue.isEmpty()) {
                play(event, guildData).block();
            } else {
                String msg = ":no_entry_sign: Player is not paused, or music queue is empty.";
                EmbedCreateSpec errorSpec = Menmu.createErrorEmbedSpec(msg, null);
                event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(errorSpec).build()).block();
            }
        }
        return Mono.empty();
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

    private Mono<Void> play(MenmuCommandInteractionEvent event, GuildData guildData) {
        if (guildData.getVoiceConnection() == null) {
            return Menmu.getCommandHandler("join")
                    .cast(JoinCommandHandler.class)
                    .flatMap(commandHandler -> commandHandler.internalHandle(event, true))
                    .doOnSuccess(unused -> guildData.getTrackScheduler().play()).then();
        }
        return Mono.just(guildData.getTrackScheduler().play()).then();
    }
}
