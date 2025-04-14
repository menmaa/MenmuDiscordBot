package com.menmasystems.menmudiscordbot;

import com.google.gson.Gson;
import com.menmasystems.menmudiscordbot.commandhandlers.*;
import com.menmasystems.menmudiscordbot.errorhandlers.UnknownCommandException;
import com.menmasystems.menmudiscordbot.eventhandlers.*;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.GuildUpdateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateMono;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Menmu.java
 * Menmu Discord Bot
 *
 * Created by Menma on 27/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class Menmu {

    public static final String VERSION_NUMBER = "0.2 (Beta)";
    public static final String INVITE_URL = "https://menmasystems.com/menmu/invite";
    public static final String RES_URL = "https://menmasystems.com/menmu/resources/";
    public static final Color DEFAULT_EMBED_COLOR = Color.of(0x47FFFF);

    public static User menma;
    public static ScheduledFuture<?> presenceTask;

    private static GatewayDiscordClient discordGateway;
    private static AudioPlayerManager playerManager;
    private static YoutubeSearch youtubeSearch;
    private static Configuration config;
    private static final Map<Snowflake, GuildData> connectedGuilds = new HashMap<>();
    private static final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private static final ScheduledExecutorService gpScheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        try {
            FileReader fileReader = new FileReader("config.json");
            config = new Gson().fromJson(fileReader, Configuration.class);
        } catch (IOException e) {
            System.err.println("ERROR: Unable to read from configuration file (config.json). " + e.getMessage());
            return;
        }

        registerCommandHandlers();

        youtubeSearch = new YoutubeSearch();
        playerManager = new DefaultAudioPlayerManager();
        YoutubeAudioSourceManager ytAudioSourceManager = new YoutubeAudioSourceManager(new Music(), new Web(), new WebEmbedded(), new Tv(), new TvHtml5Embedded());
        ytAudioSourceManager.useOauth2(getConfig().ytOAuth2RefreshToken, false);
        playerManager.registerSourceManager(ytAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(getPlayerManager(), com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);

        discordGateway = Objects.requireNonNull(DiscordClient.create(getConfig().botToken).login().block());
        getDiscordGateway().on(ReadyEvent.class).subscribe(new ReadyEventHandler());
        getDiscordGateway().on(GuildCreateEvent.class).subscribe(new GuildCreateEventHandler());
        getDiscordGateway().on(GuildDeleteEvent.class).subscribe(new GuildDeleteEventHandler());
        getDiscordGateway().on(GuildUpdateEvent.class).subscribe(new GuildUpdateEventHandler());
        getDiscordGateway().on(ChatInputInteractionEvent.class).subscribe(new ChatInputInteractionEventHandler());
        getDiscordGateway().on(VoiceStateUpdateEvent.class).subscribe(new VoiceStateUpdateEventHandler());
        getDiscordGateway().on(ReactionAddEvent.class).subscribe(new ReactionAddEventHandler());
        registerCommands();
        getDiscordGateway().onDisconnect().block();
    }

    private static void registerCommands() {
        long applicationId = getDiscordGateway().getRestClient().getApplicationId().block();

        List<ApplicationCommandRequest> commandRequestList = new ArrayList<>();

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("help")
                .description("Displays the help box")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("command")
                        .description("Command to view details of")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build()
                )
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("join")
                .description("Joins current voice channel")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("leave")
                .description("Leaves current voice channel")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("play")
                .description("Adds an entry to the playing queue")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("url")
                        .description("URL/Link of Youtube Video OR YouTube search phrase")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("stop")
                .description("Stops playing current track")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("repeat")
                .description("Enables/disables current song repeat")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("clear")
                .description("Clears/purges the guild music queue")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("skip")
                .description("Stops playing the current track and skips to the next or moves to the specified position")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("position")
                        .description("Position in queue to skip to")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .required(false)
                        .build()
                )
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("pause")
                .description("Pauses the music player")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("queue")
                .description("Displays the contents of the guild music queue")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("repeatqueue")
                .description("Enables/disables current guild music queue repeat")
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("remove")
                .description("Removes the entry in the specified position from the guild music queue")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("position")
                        .description("Position in queue to remove entry")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .required(true)
                        .build()
                )
                .build());

        commandRequestList.add(ApplicationCommandRequest.builder()
                .name("seek")
                .description("Seeks to the inserted timestamp position in the currently playing track")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("timestamp")
                        .description("Time to seek to, formatted in m:ss/h:mm:ss")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                )
                .build());


        getDiscordGateway().getRestClient().getApplicationService()
                .bulkOverwriteGlobalApplicationCommand(applicationId, commandRequestList)
                .subscribe();
    }

    private static void registerCommandHandlers() {
        commandHandlers.put("help", new HelpCommandHandler());
        commandHandlers.put("join", new JoinCommandHandler());
        commandHandlers.put("leave", new LeaveCommandHandler());
        commandHandlers.put("play", new PlayCommandHandler());
        commandHandlers.put("stop", new StopCommandHandler());
        commandHandlers.put("repeat", new RepeatCommandHandler());
        commandHandlers.put("clear", new ClearCommandHandler());
        commandHandlers.put("skip", new SkipCommandHandler());
        commandHandlers.put("pause", new PauseCommandHandler());
        commandHandlers.put("queue", new QueueCommandHandler());
        commandHandlers.put("repeatqueue", new RepeatQueueCommandHandler());
        commandHandlers.put("remove", new RemoveCommandHandler());
        commandHandlers.put("seek", new SeekCommandHandler());
    }

    public static Mono<CommandHandler> getCommandHandler(String command) {
        if(!commandHandlers.containsKey(command))
            return Mono.error(new UnknownCommandException(command));
        return Mono.justOrEmpty(commandHandlers.get(command));
    }

    public static GatewayDiscordClient getDiscordGateway() {
        return discordGateway;
    }

    public static AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public static Configuration getConfig() {
        return config;
    }

    public static GuildData getGuildData(Snowflake guildId) {
        return connectedGuilds.get(guildId);
    }

    public static void addConnectedGuild(Snowflake guildId, GuildData data) {
        connectedGuilds.put(guildId, data);
    }

    public static void removeConnectedGuild(Snowflake guildId) {
        connectedGuilds.remove(guildId);
    }

    public static void sendSuccessMessage(@Nullable MessageChannel channel, String message) {
        if(channel != null) {
            channel.createEmbed(embedCreateSpec -> {
                embedCreateSpec.setColor(Color.GREEN);
                embedCreateSpec.setDescription(message);
            }).subscribe();
        }
    }

    public static EmbedCreateSpec createSuccessEmbedSpec(String message) {
        return EmbedCreateSpec.builder().color(Color.GREEN).description(message).build();
    }

    public static MessageCreateMono sendErrorMessage(MessageChannel channel, String message, @Nullable String errorMessage) {
        EmbedCreateSpec.Builder embedCreateSpecBuilder = EmbedCreateSpec.builder();
        embedCreateSpecBuilder.color(Color.RED);
        embedCreateSpecBuilder.description(message);
        if(errorMessage != null)
            embedCreateSpecBuilder.addField("Error Message", errorMessage, false);

        return channel.createMessage(embedCreateSpecBuilder.build());
    }

    public static EmbedCreateSpec createErrorEmbedSpec(String message, @Nullable String errorMessage) {
        EmbedCreateSpec.Builder spec = EmbedCreateSpec.builder();
        spec.color(Color.RED);
        spec.description(message);
        if(errorMessage != null)
            spec.addField("Error Message", errorMessage, false);

        return spec.build();
    }

    public static YoutubeSearch getYoutubeSearch() {
        return youtubeSearch;
    }

    public static int rng(int max) {
        return (int) (Math.random() * max + 1);
    }

    public static ScheduledExecutorService getGpScheduledExecutor() {
        return gpScheduledExecutor;
    }
}
