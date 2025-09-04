package com.menmasystems.menmudiscordbot.errorhandlers;

public class VoiceChannelNotConnected extends RuntimeException {
    public VoiceChannelNotConnected(String message) {
        super(message);
    }

    public VoiceChannelNotConnected() {
        super("Not connected to a voice channel");
    }
}
