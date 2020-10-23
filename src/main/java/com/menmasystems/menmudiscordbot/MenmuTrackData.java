package com.menmasystems.menmudiscordbot;

import discord4j.core.object.entity.Member;

import java.time.Instant;

/**
 * MenmuTrackData.java
 * Menmu Discord Bot
 *
 * Created by Menma on 01/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class MenmuTrackData {
    public boolean onRepeat = false;
    public Member requestedBy;
    public String url;
    public String thumbnailUrl = "";
    public String channelName = "Unknown Channel";
    public Instant dateTimeRequested;
    public boolean ytInfoFetched = false;
    public int startRetries = 0;

    public MenmuTrackData(Member requestedBy) {
        this.requestedBy = requestedBy;
    }
}
