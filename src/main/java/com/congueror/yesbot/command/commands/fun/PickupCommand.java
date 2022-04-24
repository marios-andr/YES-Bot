package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class PickupCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] pickup = getInput(event);
        if (check(pickup)) {
            ArrayList<String> pickups = new ArrayList<>();
            Random rand = new Random();
            try {
                InputStream path = getClass().getResourceAsStream("/text/pickups.txt");
                BufferedReader myReader = new BufferedReader(new InputStreamReader(path));
                String string;
                while ((string = myReader.readLine()) != null) {
                    pickups.add(string);
                }
                myReader.close();
            } catch (Exception e) {
                System.out.println("An error occurred whilst parsing pickups.txt");
                e.printStackTrace();
            }
            int next = rand.nextInt(pickups.size());
            if (pickup.length != 2) {
                event.getChannel().sendMessage(pickups.get(next)).queue();
            } else {
                event.getChannel().sendMessage(pickup[1] + " " + pickups.get(next)).queue();
            }
        }
    }

    @Override
    public String getName() {
        return "pickup";
    }

    @Override
    public String[] getArgs() {
        return new String[] {"target"};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("if you have trouble getting a girl, use this command and she'll fall in love with you in seconds and vice versa.");
        desc.add("target: Ping the person you want to pick-up. Not necessary.");
        return StringUtils.join(desc, String.format("%n", ""));
    }

    @Override
    public String getCategory() {
        return ":frog: Fun";
    }
}
