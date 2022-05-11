package com.congueror.yesbot.command;

import com.congueror.yesbot.BotListenerAdapter;
import com.congueror.yesbot.RedditUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;

public interface AbstractCommand {

    void handle(MessageReceivedEvent event);

    String getName();

    String[] getArgs();

    String getDescription();

    default String getCategory() {
        return ":tools: Utility";
    }

    /**
     * Checks whether it is a valid command.
     */
    default boolean check(String[] input) {
        return input[0].equalsIgnoreCase(BotListenerAdapter.PREFIX + getName());
    }

    /**
     * Gets the full command string in an array
     */
    default String[] getInput(MessageReceivedEvent e) {
        return e.getMessage().getContentRaw().split(" ");
    }

    default String getCommandAndArgs() {
        String[] args = new String[getArgs().length];
        for (int i = 0; i < args.length; i++) {
            args[i] = " [" + getArgs()[i] + "]";
        }
        return getName() + StringUtils.join(args, ", ");
    }

    default void handleMessageReaction(MessageReactionAddEvent event) {
    }

    default boolean isReactionMessage(MessageReactionAddEvent e, Message message, User user) {
        return message != null && e.getMessageIdLong() == message.getIdLong() && user.equals(e.getUser());
    }

    default boolean isMention(Message message) {
        return message.getMentionedMembers().size() >= 1 && message.getMentionedMembers().get(0) != null;
    }

    static AbstractCommand getCommand(String key) {
        if (key.contains(String.valueOf(BotListenerAdapter.PREFIX))) {
            key = key.substring(1);
        }
        for (AbstractCommand cmd : BotListenerAdapter.COMMANDS) {
            if (cmd.getName().equals(key.toLowerCase())) {
                return cmd;
            }
        }
        return null;
    }

    default void sendRandomPost(MessageReceivedEvent event, Message reference, String[] subreddits) {
        int rand = new Random().nextInt(0, subreddits.length);
        var post = RedditUser.getRandomSubmission(subreddits[rand]);

        if (post.isNsfw() && !event.getTextChannel().isNSFW()) {
            event.getChannel().sendMessage("Post was nsfw, but channel is not.").reference(reference).queue();
        } else {
            String sendAfter = "";
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription("[Click Me](https://www.reddit.com" + post.getPermalink() + ")");
            if (post.getUrl().contains(".gifv")) {
                embed.setImage(post.getUrl().replace(".gifv", ".gif"));
            } else if (post.getUrl().contains("redgifs.com") || post.getUrl().contains("gfycat.com")) {
                try {
                    URL url = new URL(post.getUrl());
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String inputLine;
                    String file = "";

                    while ((inputLine = in.readLine()) != null) {
                        int index = inputLine.indexOf("<meta property=\"og:video\" content=\"") + "<meta property=\"og:video\" content=\"".length();
                        file = inputLine.substring(index, inputLine.indexOf("\">", index));
                    }

                    if (file.contains(".mp4"))
                        sendAfter = file;
                    else
                        embed.setImage(file);

                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                embed.setImage(post.getUrl());
            }
            embed.setFooter(post.getCreated().toString());
            embed.setColor(Color.RED);
            event.getChannel().sendMessageEmbeds(embed.build()).reference(reference).queue();
            if (!sendAfter.isEmpty() && !sendAfter.isBlank())
                event.getChannel().sendMessage(sendAfter).queue();
        }
    }
}
