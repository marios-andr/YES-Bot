package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class RoastCommand implements AbstractCommand {

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] roast = getInput(event);
        if (check(roast)) {
            ArrayList<String> roasts = new ArrayList<>();
            Random rand = new Random();
            try {
                InputStream path = getClass().getResourceAsStream("/text/roasts.txt");
                BufferedReader myReader = new BufferedReader(new InputStreamReader(path));
                String string;
                while ((string = myReader.readLine()) != null) {
                    roasts.add(string);
                }
                myReader.close();
            } catch (Exception e) {
                System.out.println("An error occurred whilst attempting to parse roasts.txt ");
                e.printStackTrace();
            }
            int next = rand.nextInt(roasts.size());
            if (roast.length != 2) {
                event.getChannel().sendMessage(roasts.get(next)).queue();
            } else {
                event.getChannel().sendMessage(roast[1] + " " + roasts.get(next)).queue();
            }
        }
    }

    @Override
    public String getName() {
        return "roast";
    }

    @Override
    public String[] getArgs() {
        return new String[] {"target"};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Roast someone who annoys you. Guaranteed burns.");
        desc.add("target: Ping the person you want to roast. Not necessary.");
        return StringUtils.join(desc, String.format("%n", ""));
    }

    @Override
    public String getCategory() {
        return ":frog: Fun";
    }
}
