package com.congueror.yesbot.util;

import com.congueror.yesbot.Constants;

import java.io.*;
import java.text.SimpleDateFormat;
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

    public String readAll() throws IOException {
        StringBuilder chars = new StringBuilder();
        try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
            int ch;
            while ((ch = br.read()) != -1) {
                chars.append((char) ch);
            }
        } catch (Exception e) {

        }

        return chars.toString();

        /*
        FileReader reader = new FileReader(this.file);
        char[] chars = new char[this.size];
        reader.read(chars);
        return chars;*/
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
