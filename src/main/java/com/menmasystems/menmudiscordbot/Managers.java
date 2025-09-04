package com.menmasystems.menmudiscordbot;

import com.menmasystems.menmudiscordbot.manager.GuildManager;
import discord4j.common.util.Snowflake;

public class Managers {
    public static GuildManager getGuildManager(Snowflake guildId) {
        return Menmu.getGuildManager(guildId);
    }
}
