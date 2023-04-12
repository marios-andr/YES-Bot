package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

@Command
public class RoastCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] roast = getInput(event.getMessage());
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
                Constants.LOG.error("An error occurred whilst attempting to parse roasts.txt: ", e);
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
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
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
            Constants.LOG.error("An error occurred whilst attempting to parse roasts.txt: ", e);
        }

        int next = rand.nextInt(roasts.size());
        var op = event.getOption("target");
        if (op != null) {
            event.getHook().sendMessage(op.getAsUser().getAsMention() + " " + roasts.get(next)).queue();
        } else {
            event.getHook().sendMessage(roasts.get(next)).queue();
        }
    }

    @Override
    public String getName() {
        return "roast";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.USER, "target", "The person you want to roast.", false)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Roast someone who annoys you. Guaranteed burns.";
    }

    @Override
    public String getCategory() {
        return FUN;
    }
}
