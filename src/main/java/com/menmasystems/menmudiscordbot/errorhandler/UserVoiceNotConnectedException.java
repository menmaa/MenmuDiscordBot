package com.menmasystems.menmudiscordbot.errorhandler;

public class UserVoiceNotConnectedException extends RuntimeException {
    public UserVoiceNotConnectedException(String message) {
        super(message);
    }

    public UserVoiceNotConnectedException() {
        super();
    }
}
