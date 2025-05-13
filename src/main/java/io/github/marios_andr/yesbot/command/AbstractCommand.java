package io.github.marios_andr.yesbot.command;

import io.github.marios_andr.yesbot.Constants;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractCommand extends ListenerAdapter {
    protected static final String UTILITY = ":tools: Utility";
    protected static final String TESTING = ":robot: Testing";
    protected static final String VOICE = ":loud_sound: Voice";
    protected static final String FUN = ":frog: Fun";
    protected static final String CHESS = ":chess_pawn: Chess";

    @Override
    public abstract void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event);

    public abstract String getName();

    public abstract OptionData[] getArgs();

    public abstract String getCommandDescription();

    public String getCategory() {
        return UTILITY;
    }

    @Nullable
    public Scope getScope() {
        return Scope.GUILD;
    }

    public DefaultMemberPermissions getPermissions() {
        return DefaultMemberPermissions.ENABLED;
    }

    public CommandData createCommand() {
        var com = Commands.slash(getName(), getCommandDescription().substring(0, Math.min(getCommandDescription().length(), 100)));
        com.addOptions(getArgs());
        com.setDefaultPermissions(getPermissions());
        return com;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

    }



    /**
     * Checks whether it is a valid command.
     */
    public boolean check(String[] input) {
        return input[0].equalsIgnoreCase(Constants.PREFIX + getName());
    }

    /**
     * Gets the full command string in an array
     */
    public String[] getInput(Message msg) {
        return msg.getContentRaw().split(" ");
    }

    public String getCommandAndArgs() {
        String[] args = new String[getArgs().length];
        for (int i = 0; i < args.length; i++) {
            args[i] = " [" + getArgs()[i].getName() + "]";
        }
        return getName() + StringUtils.join(args, ", ");
    }

    public String mention(String id) {
        return "<@" + id + ">";
    }

    public boolean hasMentions(Message message) {
        return message.getMentions().getMembers().size() >= 1 && message.getMentions().getMembers().get(0) != null;
    }

    public static AbstractCommand getCommand(String key) {
        if (key.contains(Constants.PREFIX)) {
            key = key.substring(1);
        }
        for (AbstractCommand cmd : Constants.COMMANDS) {
            if (cmd.getName().equals(key)) {
                return cmd;
            }
        }
        return null;
    }

    public void sendRandomPost(MessageReceivedEvent event, Message reference, String... subreddits) {
        /*
        int rand = new Random().nextInt(0, subreddits.length);
        var post = Reddit.getRandomSubmission(subreddits[rand]);
        if (post == null)
            Constants.LOG.error("Reddit API was unable to find random post.");

        if (post.isOver18() && !event.getChannel().asTextChannel().isNSFW()) {
            event.getChannel().sendMessage("Post was nsfw, but channel is not.").setMessageReference(reference).queue();
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
            //embed.setFooter(post.getCreated());
            embed.setColor(Color.RED);
            event.getChannel().sendMessageEmbeds(embed.build()).setMessageReference(reference).queue();
            if (!sendAfter.isEmpty() && !sendAfter.isBlank())
                event.getChannel().sendMessage(sendAfter).queue();
        }*/
    }

    public void sendRandomPost(SlashCommandInteractionEvent event, String... subreddits) {
        /*
        int rand = new Random().nextInt(0, subreddits.length);
        var post = Reddit.getRandomSubmission(subreddits[rand]);
        if (post == null) {
            Constants.LOG.error("Reddit API was unable to find random post.");
            return;
        }


        if (post.isOver18() && !event.getChannel().asTextChannel().isNSFW()) {
            event.getHook().sendMessage("Post was nsfw, but channel is not.").queue();
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
            //embed.setFooter(post.getCreated().toString());
            embed.setColor(Color.RED);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            if (!sendAfter.isEmpty() && !sendAfter.isBlank())
                event.getHook().sendMessage(sendAfter).queue();
        }*/
    }

    public AudioChannel joinVC(MessageReceivedEvent event) {
        return joinVC(event.getGuild(), event.getGuildChannel(), event.getMember(), event.getChannel());
    }

    public AudioChannel joinVC(SlashCommandInteractionEvent event) {
        return joinVC(event.getGuild(), event.getGuildChannel(), event.getMember(), event.getChannel());
    }

    @Nullable
    private AudioChannel joinVC(Guild guild, GuildMessageChannelUnion guildChannel, Member member, MessageChannelUnion channel) {
        if (guild.getSelfMember().hasPermission(guildChannel, Permission.VOICE_CONNECT)) {
            AudioChannel connectedChannel = Objects.requireNonNull(Objects.requireNonNull(member).getVoiceState()).getChannel();
            if (connectedChannel == null) {
                channel.sendMessage("not in channel").queue();
                return null;
            }
            AudioManager audioManager = guild.getAudioManager();
            audioManager.openAudioConnection(connectedChannel);
            return connectedChannel;
        }
        return null;
    }


    public enum Scope {
        GUILD, GLOBAL
    }
}
