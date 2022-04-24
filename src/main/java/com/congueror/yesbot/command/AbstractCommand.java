package com.congueror.yesbot.command;

import com.congueror.yesbot.BotListenerAdapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.commons.lang3.StringUtils;

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
}
