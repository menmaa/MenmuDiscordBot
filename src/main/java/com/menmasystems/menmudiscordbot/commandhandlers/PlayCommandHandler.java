package com.menmasystems.menmudiscordbot.commandhandlers;

import com.google.api.services.youtube.model.SearchResult;
import com.menmasystems.menmudiscordbot.GuildData;
import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuTrackData;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.errorhandlers.CommandExecutionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PlayCommandHandler.class);

    @Override
    public Mono<Void> handle(MessageCreateEvent event, MessageChannel channel, List<String> params) {
        if(event.getGuildId().isEmpty())
            return Mono.error(new CommandExecutionException("play", "Guild ID is empty."));

        GuildData guildData = Menmu.getGuildData(event.getGuildId().get());

        if(params.size() >= 2) {
            if(event.getMember().isEmpty()) return Mono.error(new CommandExecutionException("play", "Member is empty"));

            final String loadItem;
            MenmuTrackData trackData = new MenmuTrackData(event.getMember().get());

            if(params.get(1).startsWith("http://") || params.get(1).startsWith("https://")) {
                // Looks like a url, just load it up for request on player manager later.
                loadItem = trackData.url = params.get(1);
            } else {
                // Assuming text params, rebuilding into string
                StringBuilder sb = new StringBuilder();
                for(String s : params.subList(1, params.size())) {
                    sb.append(s).append(" ");
                }
                String searchText = sb.toString().trim();

                // Now we call search on YouTube API see if we can get results
                try {
                    Message message = channel.createMessage(":mag_right: Searching for `" + searchText + "`...").block();
                    SearchResult ytResults = Menmu.getYoutubeSearch().getYtVideoDataBySearchQuery(searchText);
                    if(ytResults != null) {
                        loadItem = trackData.url = "https://www.youtube.com/watch?v=" + ytResults.getId().getVideoId();
                        trackData.channelName = ytResults.getSnippet().getChannelTitle();
                        trackData.thumbnailUrl = ytResults.getSnippet().getThumbnails().getDefault().getUrl();
                        trackData.ytInfoFetched = true;
                    } else {
                        String msg = ":no_entry_sign: Search for `" + searchText + "` returned no results.";
                        if(message != null) message.edit(spec -> spec.setContent(msg)).block();
                        else channel.createMessage(msg).block();
                        return Mono.empty();
                    }
                    if(message != null) message.delete().subscribe();
                } catch (RuntimeException e) {
                    return Mono.error(e);
                }
            }

            // Now we can attempt to load the track or playlist.
            final Message enqueuingMessage = channel.createMessage(":cd: Enqueuing...").block();
            Menmu.getPlayerManager().loadItem(loadItem, new AudioLoadResultHandler() {
                int trackLoadRetries = 0;

                @Override
                public void trackLoaded(AudioTrack track) {
                    trackData.dateTimeRequested = Instant.now();
                    track.setUserData(trackData);
                    MenmuTrackScheduler trackScheduler = guildData.getTrackScheduler();
                    trackScheduler.queue(track);
                    if(enqueuingMessage != null) enqueuingMessage.delete().subscribe();
                    List<AudioTrack> repeatingQueue = guildData.getQueueOnRepeat();
                    int size = (repeatingQueue != null) ? repeatingQueue.size() : trackScheduler.queue.size();
                    Menmu.sendSuccessMessage(channel, String.format(":white_check_mark: Enqueued `%s` to position %d",
                                    track.getInfo().title, size));

                    play(event, channel, guildData).doOnError(e -> {
                        if(e instanceof CommandExecutionException)
                            ((CommandExecutionException) e).createErrorMessage(channel).subscribe();
                        String msg = ":no_entry_sign: Cannot auto start playing. Use command `play` to play manually.";
                        Menmu.sendErrorMessage(channel, msg, null);
                    }).subscribe();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for(AudioTrack track : playlist.getTracks()) {
                        MenmuTrackData menmuTrackData = new MenmuTrackData(event.getMember().get());
                        menmuTrackData.dateTimeRequested = Instant.now();
                        if(track.getSourceManager().getSourceName().equals("youtube"))
                            menmuTrackData.url = "https://www.youtube.com/watch?v=" + track.getIdentifier();
                        track.setUserData(menmuTrackData);
                        guildData.getTrackScheduler().queue(track);
                    }
                    if(enqueuingMessage != null) enqueuingMessage.delete().subscribe();
                    Menmu.sendSuccessMessage(channel,
                            String.format(":white_check_mark: Enqueued %d songs from playlist `%s`",
                                    playlist.getTracks().size(), playlist.getName()));

                    play(event, channel, guildData).doOnError(e -> {
                        if(e instanceof CommandExecutionException)
                            ((CommandExecutionException) e).createErrorMessage(channel).subscribe();
                        String msg = ":no_entry_sign: Cannot auto start playing. Use command `play` to play manually.";
                        Menmu.sendErrorMessage(channel, msg, null);
                    }).subscribe();
                }

                @Override
                public void noMatches() {
                    if(enqueuingMessage != null) enqueuingMessage.delete().subscribe();
                    Menmu.sendErrorMessage(channel, ":no_entry_sign: Error: No Matches", null);
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    if(trackLoadRetries < MenmuTrackScheduler.MAX_TRACK_START_RETRIES) {
                        Menmu.getPlayerManager().loadItem(loadItem, this);
                        trackLoadRetries++;
                        return;
                    }
                    if(enqueuingMessage != null) enqueuingMessage.delete().subscribe();
                    if(exception.severity == Severity.COMMON || exception.severity == Severity.SUSPICIOUS) {
                        String message = ":no_entry_sign: Eh... I'm sorry, but I was unable to load that track.";
                        Menmu.sendErrorMessage(channel, message, exception.getMessage());
                    } else {
                        logger.error("There was an error trying to load that track.", exception);
                    }
                }
            });
        } else {
            AudioPlayer guildAudioPlayer = guildData.getAudioPlayer();
            MenmuTrackScheduler guildTrackScheduler = guildData.getTrackScheduler();
            if(guildAudioPlayer.isPaused()) {
                guildAudioPlayer.setPaused(false);
                Menmu.sendSuccessMessage(channel, ":play_pause: Resuming player...");
            } else if(guildAudioPlayer.getPlayingTrack() != null || guildTrackScheduler.queue.size() > 0) {
                play(event, channel, guildData).block();
            } else {
                Menmu.sendErrorMessage(channel, ":no_entry_sign: Player is not paused, or music queue is empty.", null);
            }
        }
        return Mono.empty();
    }

    @Override
    public void helpHandler(MessageChannel channel, User self) {
        channel.createEmbed(embedCreateSpec -> {
            final String command = Menmu.getConfig().cmdPrefix + "!play";
            final String title = "Command: `play`";
            final String description = "Adds a song or a playlist to the guild queue and starts playing if not already playing. " +
                    "Will also resume playing if previously paused.";
            final String usage = String.format("`%s`\n`%s [link]`\n`%s [youtube search phrase]`", command, command, command);
            final String examples = String.format("`%s`\n`%s https://www.youtube.com/watch?v=jrzUsHNGZHc`\n`%s knock knock penny's door`", command, command, command);

            embedCreateSpec.setColor(Menmu.DEFAULT_EMBED_COLOR);
            embedCreateSpec.setAuthor(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl());
            embedCreateSpec.setTitle(title);
            embedCreateSpec.setDescription(description);
            embedCreateSpec.addField("Usage", usage, true);
            embedCreateSpec.addField("Examples", examples, true);
        }).block();
    }

    private Mono<Void> play(MessageCreateEvent event, MessageChannel channel, GuildData guildData) {
        if (guildData.getVoiceConnection() == null) {
            return Menmu.getCommandHandler("join")
                    .flatMap(commandHandler -> commandHandler.handle(event, channel, null))
                    .doOnSuccess(unused -> guildData.getTrackScheduler().play()).then();
        }
        return Mono.just(guildData.getTrackScheduler().play()).then();
    }
}
