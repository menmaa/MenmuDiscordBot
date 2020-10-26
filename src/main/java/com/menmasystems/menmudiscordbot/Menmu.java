package com.menmasystems.menmudiscordbot;

import com.google.gson.Gson;
import com.menmasystems.menmudiscordbot.commandhandlers.*;
import com.menmasystems.menmudiscordbot.errorhandlers.UnknownCommandException;
import com.menmasystems.menmudiscordbot.eventhandlers.*;
import com.menmasystems.menmudiscordbot.interfaces.CommandHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.GuildUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    public static final String VERSION_NUMBER = "0.1.2 (Beta)";
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
        AudioSourceManagers.registerRemoteSources(getPlayerManager());

        discordGateway = Objects.requireNonNull(DiscordClient.create(getConfig().botToken).login().block());
        getDiscordGateway().on(ReadyEvent.class).subscribe(new ReadyEventHandler());
        getDiscordGateway().on(GuildCreateEvent.class).subscribe(new GuildCreateEventHandler());
        getDiscordGateway().on(GuildDeleteEvent.class).subscribe(new GuildDeleteEventHandler());
        getDiscordGateway().on(GuildUpdateEvent.class).subscribe(new GuildUpdateEventHandler());
        getDiscordGateway().on(MessageCreateEvent.class).subscribe(new MessageCreateEventHandler());
        getDiscordGateway().on(VoiceStateUpdateEvent.class).subscribe(new VoiceStateUpdateEventHandler());
        getDiscordGateway().on(ReactionAddEvent.class).subscribe(new ReactionAddEventHandler());

        getDiscordGateway().onDisconnect().block();
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

        commandHandlers.put("kill", new KillCommandHandler());
        commandHandlers.put("hug", new HugCommandHandler());
        commandHandlers.put("wink", new WinkCommandHandler());
        commandHandlers.put("kiss", new KissCommandHandler());
        commandHandlers.put("punch", new PunchCommandHandler());
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

    public static void sendErrorMessage(@Nullable MessageChannel channel, String message, @Nullable String errorMessage) {
        if(channel != null) {
            channel.createEmbed(embedCreateSpec -> {
                embedCreateSpec.setColor(Color.RED);
                embedCreateSpec.setDescription(message);
                if(errorMessage != null)
                    embedCreateSpec.addField("Error Message", errorMessage, false);
            }).subscribe();
        }
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
