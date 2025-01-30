package com.congueror.yesbot.util;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.WebInterface;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ConsolePrintStream extends PrintStream {

    public ConsolePrintStream(@NotNull OutputStream out) {
        super(out);
    }

    @Override
    public void write(@NotNull byte[] buf, int off, int len) {
        byte[] newBuf = new byte[len];
        for (int i = off; i < len; i++) {
            newBuf[i] = buf[i];
        }
        sendToConsole(new String(newBuf, StandardCharsets.UTF_8));
        super.write(buf, off, len);
    }

    private void sendToConsole(String out) {
        String write = "";
        try {
            if (out.equals("\r\n") || (out.length() >= 4 && out.substring(0, 4).contains("\tat")))
                write = Constants.LOG_FILE.writePlain(out);
            else
                write = Constants.LOG_FILE.write(out);
        } catch (IOException ignored) {
        }
        WebInterface.sendToConsole(write);
    }
}
