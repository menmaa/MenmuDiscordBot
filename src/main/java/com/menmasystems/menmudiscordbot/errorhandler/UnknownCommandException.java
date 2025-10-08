package com.menmasystems.menmudiscordbot.errorhandler;

/**
 * UnknownCommandException.java
 * Menmu Discord Bot
 * <p>
 * Created by Menma on 30/08/2020
 * Copyright © 2020 Menma Systems. All Rights Reserved.
 */

public class UnknownCommandException extends Exception {

    public UnknownCommandException(String command) {
        super(command);
    }
}
