package io.github.marios_andr.yesbot;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class BotListenerAdapter extends ListenerAdapter {

    public BotListenerAdapter() {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var cmd = AbstractCommand.getCommand(event.getName());
        if (cmd != null) {
            event.deferReply().queue();
            cmd.onSlashCommandInteraction(event);
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Constants.LOG.debug("[{} | {}]: {}\n", event.getAuthor().getName(), event.getGuild().getName(), event.getMessage().getContentDisplay());

        if (event.getAuthor().isBot() && event.isWebhookMessage()) {
            return;
        }

        if (Constants.LOCKED)
            return;

        //Secret Commands
        String cases = event.getMessage().getContentRaw();
        if (cases.equalsIgnoreCase("im alone") || cases.equalsIgnoreCase("i'm alone") || cases.equalsIgnoreCase("im lonely") || cases.equalsIgnoreCase("i'm lonely") || cases.equalsIgnoreCase("i am lonely") || cases.equalsIgnoreCase("i am alone")) {
            AudioChannel connectedChannel = event.getMember().getVoiceState().getChannel();
            if (connectedChannel == null)
                return;

            AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.openAudioConnection(connectedChannel);
            event.getChannel().sendMessage("Not anymore!").queue();
        }

        if (cases.equalsIgnoreCase("what have I done")) {
            event.getChannel().sendMessage("https://tenor.com/view/star-wars-anakin-skywalker-what-have-i-done-confused-sad-gif-3575836").queue();
        }

        if (event.getMessage().getContentRaw().equals("|shutdown") && event.getAuthor().getId().equals(Constants.getSettings().owner_snowflake())) {
            event.getChannel().sendMessage("Shutting down!").queue();
            event.getJDA().shutdown();
            return;
        }

        //Handle Commands
        String cmd = event.getMessage().getContentRaw().split(" ")[0];
        var command = AbstractCommand.getCommand(cmd);
        if (command != null) {
            AbstractCommand.getCommand(cmd).onMessageReceived(event);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        for (AbstractCommand cmd : Constants.COMMANDS) {
            cmd.onMessageReactionAdd(event);
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() == null) {
            AudioChannel memberLeaveVC = event.getChannelLeft();
            List<Member> members = memberLeaveVC.getMembers();
            if (members.contains(event.getGuild().getMember(Objects.requireNonNull(event.getJDA().getUserById("727830791664697395"))))) {
                if (memberLeaveVC.getMembers().toArray().length == 1) {
                    AudioManager audioManager = event.getGuild().getAudioManager();
                    audioManager.closeAudioConnection();
                }
            }
        }
    }
}
