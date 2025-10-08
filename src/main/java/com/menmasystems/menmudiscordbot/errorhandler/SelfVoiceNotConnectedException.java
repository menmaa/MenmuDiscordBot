package com.menmasystems.menmudiscordbot.errorhandler;

public class SelfVoiceNotConnectedException extends RuntimeException {
    public SelfVoiceNotConnectedException() {
        super();
    }

    public SelfVoiceNotConnectedException(String message) {
        super(message);
    }
}
