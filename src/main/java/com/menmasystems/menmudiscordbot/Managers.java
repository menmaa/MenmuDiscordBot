package com.menmasystems.menmudiscordbot;

import com.menmasystems.menmudiscordbot.manager.GuildManager;
import discord4j.common.util.Snowflake;

import java.util.HashMap;
import java.util.Map;

public class Managers {
    private static final Map<Snowflake, GuildManager> connectedGuilds = new HashMap<>();

    public static GuildManager getGuildManager(Snowflake guildId) {
        return connectedGuilds.get(guildId);
    }

    public static void addConnectedGuild(Snowflake guildId, GuildManager data) {
        connectedGuilds.put(guildId, data);
    }

    public static void removeConnectedGuild(Snowflake guildId) {
        connectedGuilds.remove(guildId);
    }
}
