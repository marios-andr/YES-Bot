package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class RNGCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] rng = getInput(event);
        if (check(rng)) {
            int min = Integer.parseInt(rng[1]);
            int max = Integer.parseInt(rng[2]);
            if (rng.length != 3) {
                event.getChannel().sendMessage("Please specify minimum and maximum value exclusively in that order.").queue();
            } else if (min < max) {
                int randomValue = (int) Math.floor(Math.random() * (max - min + 1) + min);
                event.getChannel().sendMessage("Randomized Value: " + randomValue).queue();
            } else {
                event.getChannel().sendMessage("Maximum needs to be higher than minimum...obviously...").queue();
            }
        }
    }

    @Override
    public String getName() {
        return "rng";
    }

    @Override
    public String[] getArgs() {
        return new String[] {"min", "max"};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Get a pseudorandom number between the two numbers.");
        desc.add("min: Minimum number the bot can get.");
        desc.add("max: Maximum number the bot can get.");
        return StringUtils.join(desc, String.format("%n", ""));
    }
}
