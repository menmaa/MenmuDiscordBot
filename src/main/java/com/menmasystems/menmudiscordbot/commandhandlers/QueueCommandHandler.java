package com.menmasystems.menmudiscordbot.commandhandlers;

import com.menmasystems.menmudiscordbot.*;
import com.menmasystems.menmudiscordbot.errorhandler.CommandExecutionException;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.menmasystems.menmudiscordbot.manager.GuildManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * QueueCommandHandler.java
 * Menmu Discord Bot
 *
 * Created by Menma on 02/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class QueueCommandHandler implements CommandHandler {
    @Override
    public Mono<Void> handle(MenmuCommandInteractionEvent event) {
        if(event.getInteraction().getGuildId().isEmpty())
            return Mono.error(new CommandExecutionException("play", "Guild ID is empty."));

        GuildManager guildManager = Managers.getGuildManager(event.getInteraction().getGuildId().get());
        AudioPlayer player = guildManager.getAudioPlayer();
        MenmuTrackScheduler trackScheduler = guildManager.getTrackScheduler();
        List<AudioTrack> rTrackList = guildManager.getQueueOnRepeat();
        List<AudioTrack> trackList = (rTrackList != null) ? new LinkedList<>(rTrackList) : trackScheduler.getQueueAsList();

        final AudioTrack np = player.getPlayingTrack();
        if(np != null) {
            trackList.add(0, np);
        }

        if(trackList.isEmpty()) {
            event.sendErrorInteractionReply(":no_entry_sign: Music Queue Empty.", null).subscribe();
            return Mono.empty();
        }

        Guild guild = guildManager.getGuild();
        int pages = (int) Math.ceil(trackList.size() / 10.0);

        if(guildManager.getMusicQueueMessage() != null) {
            guildManager.getMusicQueueMessage().removeAllReactions().subscribe();
            guildManager.setMusicQueueMessage(null);
            guildManager.setMusicQueuePage(0);
        }

        EmbedCreateSpec.Builder spec = EmbedCreateSpec.builder();
        spec.author(guild.getName(), Menmu.INVITE_URL, guild.getIconUrl(Image.Format.PNG).orElse(null));
        spec.title("Music Queue" + ((rTrackList != null) ? " - Repeat Enabled" : ""));

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i <= 10; i++) {
            if(i == trackList.size()) break;
            AudioTrack track = trackList.get(i);
            MenmuTrackData trackData = track.getUserData(MenmuTrackData.class);

            long length = track.getInfo().length;
            String duration;
            if(length == Units.DURATION_MS_UNKNOWN)
                duration = "Live Stream";
            else
                duration = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(length),
                        TimeUnit.MILLISECONDS.toSeconds(length) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length))
                );

            if(np != null) {
                if(i == 0) sb.append("=> [");
                else sb.append(i).append(". [");
            } else sb.append(i+1).append(". [");

            sb.append(track.getInfo().title);
            sb.append("](");
            sb.append(trackData.getUrl());
            sb.append(") - ");
            sb.append(duration);
            sb.append(" - Requested by ");
            sb.append(trackData.getRequestedBy().getDisplayName());
            sb.append("\n\n");

            if(np != null && i == 0) {
                sb.append("\n");
            }
        }
        spec.description(sb.toString());

        String iconUrl = null;
        if(event.getInteraction().getMember().isPresent()) {
            iconUrl = event.getInteraction().getMember().get().getAvatarUrl();
        }

        int trackListSize = trackList.size() - ((np != null) ? 1 : 0);
        long totalLength = 0;
        for(AudioTrack track : trackList) {
            long length = track.getInfo().length;
            if(length == Units.DURATION_MS_UNKNOWN) continue;
            totalLength += length;
        }
        if(np != null) {
            long npLength = np.getInfo().length;
            totalLength -= (npLength != Units.DURATION_MS_UNKNOWN) ? npLength : 0;
        }

        long hours = TimeUnit.MILLISECONDS.toHours(totalLength);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalLength) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalLength));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalLength) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalLength));
        String footer = "Page 1/%d | %d tracks in queue | %02d:%02d:%02d total queue length";
        spec.footer(String.format(footer, pages, trackListSize, hours, minutes, seconds), iconUrl);

        return Mono.just(InteractionApplicationCommandCallbackSpec.builder().addEmbed(spec.build()).build())
                .flatMap(event::reply)
                .then(event.getReply())
                .doOnNext(message -> {
                    if(pages > 1) {
                        message.addReaction(ReactionEmoji.unicode("⬅️")).subscribe();
                        message.addReaction(ReactionEmoji.unicode("➡️")).subscribe();

                        guildManager.setMusicQueueMessage(message);
                        guildManager.setMusicQueuePage(1);
                    }
                }).then();
    }

    @Override
    public void helpHandler(MenmuCommandInteractionEvent event) {
        event.getClient().getSelf()
                .map(self -> EmbedCreateSpec.builder()
                        .color(Menmu.DEFAULT_EMBED_COLOR)
                        .author(self.getUsername() + "'s Helpdesk", Menmu.INVITE_URL, self.getAvatarUrl())
                        .title("Command: `queue`")
                        .description("Displays the contents of the guild music queue.")
                        .addField("Usage", "`/queue`", true)
                        .build())
                .flatMap(embedSpec -> event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embedSpec).build()))
                .subscribe();
    }
}
