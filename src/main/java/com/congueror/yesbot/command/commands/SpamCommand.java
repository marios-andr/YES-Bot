package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import static com.congueror.yesbot.Constants.LOCKED;
import static com.congueror.yesbot.Constants.STOP;

public class SpamCommand implements Command {

    Thread spamThread;

    @Override
    public void handle(MessageReceivedEvent e) {
        String[] spam = getInput(e);
        var auth = e.getMember();
        if (check(spam) && !e.isWebhookMessage() && !e.getAuthor().isBot() && auth.hasPermission(Permission.MESSAGE_MANAGE)) {
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
                            e.getChannel().sendMessage(full.toString()).queue();
                            try {
                                Thread.sleep(1200);
                            } catch (Exception e) {
                                System.out.println("Exception");
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
    public String getName() {
        return "spam";
    }

    @Override
    public String[] getArgs() {
        return new String[]{"target"};
    }

    @Override
    public String getDescription() {
        return "Spam your favorite person!";
    }
}
