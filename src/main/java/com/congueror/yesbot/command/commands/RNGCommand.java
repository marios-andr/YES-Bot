package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class RNGCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] rng = getInput(event.getMessage());
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
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        int min = event.getOption("min").getAsInt();
        int max = event.getOption("max").getAsInt();
        if (min < max) {
            int randomValue = (int) Math.floor(Math.random() * (max - min + 1) + min);
            event.getHook().sendMessage("Randomized Value: " + randomValue).queue();
        } else {
            event.getHook().sendMessage("Maximum needs to be higher than minimum...obviously...").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "rng";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.INTEGER, "min", "The minimum number. Inclusive.", true),
                new OptionData(OptionType.INTEGER, "max", "The maximum number. Inclusive.", true)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Get a pseudorandom number between the two numbers.";
    }
}
