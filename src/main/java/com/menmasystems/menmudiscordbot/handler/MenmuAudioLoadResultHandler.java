package com.menmasystems.menmudiscordbot.handler;

import com.menmasystems.menmudiscordbot.Menmu;
import com.menmasystems.menmudiscordbot.MenmuCommandInteractionEvent;
import com.menmasystems.menmudiscordbot.MenmuTrackData;
import com.menmasystems.menmudiscordbot.MenmuTrackScheduler;
import com.menmasystems.menmudiscordbot.commandhandlers.JoinCommandHandler;
import com.menmasystems.menmudiscordbot.errorhandler.CommandExecutionException;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;

public class MenmuAudioLoadResultHandler implements AudioLoadResultHandler {

    private final GuildManager guildManager;
    private final MenmuCommandInteractionEvent interactionEvent;
    private final MenmuTrackData trackData;

    public MenmuAudioLoadResultHandler(GuildManager guildManager, MenmuCommandInteractionEvent interactionEvent, MenmuTrackData trackData) {
        this.guildManager = guildManager;
        this.interactionEvent = interactionEvent;
        this.trackData = trackData;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        trackData.setDateTimeRequested(Instant.now());
        audioTrack.setUserData(trackData);
        MenmuTrackScheduler trackScheduler = guildManager.getTrackScheduler();
        trackScheduler.queue(audioTrack);

        List<AudioTrack> repeatingQueue = guildManager.getQueueOnRepeat();
        int size = (repeatingQueue != null) ? repeatingQueue.size() : trackScheduler.queue.size();
        var specMsg = String.format(":white_check_mark: Enqueued `%s` to position %d", audioTrack.getInfo().title, size);
        var spec = InteractionFollowupCreateSpec.builder()
                .addEmbed(Menmu.createSuccessEmbedSpec(specMsg))
                .build();

        interactionEvent.createFollowup(spec)
                .then(startPlayer())
                .subscribe();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        for(AudioTrack track : playlist.getTracks()) {
            MenmuTrackData menmuTrackData = new MenmuTrackData(interactionEvent.getInteraction().getMember().get());
            menmuTrackData.setDateTimeRequested(Instant.now());
            if(track.getSourceManager().getSourceName().equals("youtube"))
                menmuTrackData.setUrl("https://www.youtube.com/watch?v=" + track.getIdentifier());
            track.setUserData(menmuTrackData);
            guildManager.getTrackScheduler().queue(track);
        }

        var specMsg = String.format(":white_check_mark: Enqueued %d songs from playlist `%s`",
                playlist.getTracks().size(), playlist.getName());
        var spec = InteractionFollowupCreateSpec.builder()
                .addEmbed(Menmu.createSuccessEmbedSpec(specMsg))
                .build();

        interactionEvent.createFollowup(spec)
                .then(startPlayer())
                .subscribe();
    }

    @Override
    public void noMatches() {
        EmbedCreateSpec spec = Menmu.createErrorEmbedSpec(":no_entry_sign: Error: No Matches", null);
        interactionEvent.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(spec).build()).block();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        String message = ":no_entry_sign: Eh... I'm sorry, but I was unable to load that track. Please try again.";
        EmbedCreateSpec spec = Menmu.createErrorEmbedSpec(message, exception.getMessage());
        interactionEvent.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(spec).build()).block();
    }

    private Mono<Void> startPlayer() {
        return joinVoiceChannel()
                .then(Mono.fromCallable(guildManager::getTrackScheduler))
                .map(MenmuTrackScheduler::play)
                .publishOn(Schedulers.boundedElastic())
                .doOnError(CommandExecutionException.class, e -> e.createErrorMessage(interactionEvent, true).subscribe())
                .then();
    }

    private Mono<Void> joinVoiceChannel() {
        if (guildManager.getVoiceConnection() == null) {
            return Menmu.getCommandHandler("join")
                    .cast(JoinCommandHandler.class)
                    .flatMap(commandHandler -> commandHandler.internalHandle(interactionEvent, true));
        }

        return Mono.empty();
    }
}
