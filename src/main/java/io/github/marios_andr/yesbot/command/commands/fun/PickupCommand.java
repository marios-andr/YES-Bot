package io.github.marios_andr.yesbot.command.commands.fun;

import io.github.marios_andr.yesbot.Constants;
import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.Command;
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
public class PickupCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] pickup = getInput(event.getMessage());
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
                Constants.LOG.error("An error occurred whilst parsing pickups.txt: ", e);
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
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
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
            Constants.LOG.error("An error occurred whilst parsing pickups.txt: ", e);
        }

        int next = rand.nextInt(pickups.size());
        var op = event.getOption("target");
        if (op != null) {
            event.getHook().sendMessage(op.getAsUser().getAsMention() + " " + pickups.get(next)).queue();
        } else {
            event.getHook().sendMessage(pickups.get(next)).queue();
        }
    }

    @Override
    public String getName() {
        return "pickup";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.USER, "target", "The person you want to pick-up.", false)
        };
    }

    @Override
    public String getCommandDescription() {
        return "if you have trouble getting a girl, use this command and she'll fall in love with you in seconds and vice versa.";
    }

    @Override
    public String getCategory() {
        return FUN;
    }
}
