package com.menmasystems.menmudiscordbot;

import discord4j.core.object.entity.Member;

import java.time.Instant;

/**
 * MenmuTrackData.java
 * Menmu Discord Bot
 * <p>
 * Created by Menma on 01/09/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class MenmuTrackData {
    private boolean onRepeat = false;
    private Member requestedBy;
    private String url;
    private String thumbnailUrl = "";
    private String channelName = "Unknown Channel";
    private Instant dateTimeRequested;
    private boolean ytInfoFetched = false;

    public MenmuTrackData(String url, Member requestedBy) {
        this.setUrl(url);
        this.setRequestedBy(requestedBy);
    }

    public MenmuTrackData(Member requestedBy) {
        this.setRequestedBy(requestedBy);
    }

    public Member getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Member requestedBy) {
        this.requestedBy = requestedBy;
    }

    public boolean isOnRepeat() {
        return onRepeat;
    }

    public void setOnRepeat(boolean onRepeat) {
        this.onRepeat = onRepeat;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Instant getDateTimeRequested() {
        return dateTimeRequested;
    }

    public void setDateTimeRequested(Instant dateTimeRequested) {
        this.dateTimeRequested = dateTimeRequested;
    }

    public boolean isYtInfoFetched() {
        return ytInfoFetched;
    }

    public void setYtInfoFetched(boolean ytInfoFetched) {
        this.ytInfoFetched = ytInfoFetched;
    }
}
