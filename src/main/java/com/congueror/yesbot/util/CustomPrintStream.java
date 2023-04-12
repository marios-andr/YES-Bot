package com.congueror.yesbot.util;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class CustomPrintStream extends PrintStream {
    Consumer<String> onChange;

    public CustomPrintStream(@NotNull OutputStream out, Consumer<String> onChange) {
        super(out);
        this.onChange = onChange;
    }

    @Override
    public void write(@NotNull byte[] buf, int off, int len) {
        byte[] newBuf = new byte[len];
        for (int i = off; i < len; i++) {
            newBuf[i] = buf[i];
        }
        onChange.accept(new String(newBuf, StandardCharsets.UTF_8));
        super.write(buf, off, len);
    }
}
