package com.menmasystems.menmudiscordbot.errorhandler;

public enum ErrorMessage {
    USER_VOICE_STATE_NULL(":no_entry_sign: You are not connected to any voice channels."),
    SELF_VOICE_STATE_NULL(":no_entry_sign: I am not connected to any voice channels."),
    VOICE_CONNECTION_ERROR(":no_entry_sign: There was an error trying to connect to voice. If the issue persists, please contact `menma_`.");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
