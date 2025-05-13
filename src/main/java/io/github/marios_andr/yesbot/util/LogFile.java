package io.github.marios_andr.yesbot.util;

import io.github.marios_andr.yesbot.Constants;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class LogFile {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private final File file;

    public LogFile() {
        Date date = new Date();

        File directory = new File("./logs/");
        this.file = new File("./logs/" + FORMAT.format(date) + ".txt");
        try {
            directory.mkdir();
            this.file.createNewFile();
        } catch (Exception e) {
            Constants.LOG.error("Something went wrong while initializing the Log File", e);
        }
    }

    protected LogFile(String name) {
        this.file = new File("./logs/" + name);
    }

    public static String[] getAll() {
        File directory = new File("./logs/");

        var logs = directory.listFiles(File::isFile);
        return Arrays.stream(logs).map(File::getName).toArray(String[]::new);
    }

    public static String read(String name) throws IOException {
        return new LogFile(name).read();
    }

    public String getName() {
        return this.file.getName();
    }

    public String read() throws IOException {
        StringBuilder chars = new StringBuilder();
        try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
            int ch;
            while ((ch = br.read()) != -1) {
                chars.append((char) ch);
            }
        } catch (Exception e) {

        }

        return chars.toString();
    }

    public String write(String out) throws IOException {
        String full = "[" + FORMAT.format(new Date()) + "] " + out;
        FileWriter writer = new FileWriter(this.file, true);
        writer.write(full);
        writer.close();
        return full;
    }

    public String writePlain(String out) throws IOException {
        FileWriter writer = new FileWriter(this.file, true);
        writer.write(out);
        writer.close();
        return out;
    }
}
