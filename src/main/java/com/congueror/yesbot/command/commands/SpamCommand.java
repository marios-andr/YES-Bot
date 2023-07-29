package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import static com.congueror.yesbot.Constants.LOCKED;
import static com.congueror.yesbot.Constants.STOP;

@Command
public class SpamCommand extends AbstractCommand {

    Thread spamThread;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] spam = getInput(event.getMessage());
        var auth = event.getMember();
        if (check(spam) && !event.isWebhookMessage() && !event.getAuthor().isBot() && auth.hasPermission(Permission.MESSAGE_MANAGE)) {
            StringBuilder full = new StringBuilder();
            for (int i = 1; i < spam.length; i++) {
                full.append(" ").append(spam[i]);
            }

            if (spamThread == null || !spamThread.isAlive()) {
                spamThread = new Thread() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 20; i++) {
                            if (STOP || LOCKED) {
                                STOP = false;
                                break;
                            }
                            event.getChannel().sendMessage(full.toString()).queue();
                            try {
                                Thread.sleep(1200);
                            } catch (Exception e) {
                                Constants.LOG.error("Thread Exception: ", e);
                            }
                        }

                        super.run();
                    }
                };
                spamThread.start();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {}

    @Override
    public String getName() {
        return "spam";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{
                new OptionData(OptionType.STRING, "message", "The message to be spammed.")
        };
    }

    @Override
    public String getCommandDescription() {
        return "Spam your favorite person!";
    }

    @Override
    public Scope getScope() {
        return null;
    }
}
