package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.congueror.yesbot.Constants.getJson;
import static com.congueror.yesbot.Constants.optionalString;

@Command
public class ChessComCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] chess = getInput(event.getMessage());
        if (check(chess)) {
            Message reference = event.getMessage();

            if (chess.length == 2) {
                String username = chess[1];

                JsonObject json = getJson("https://api.chess.com/pub/player/" + username);
                if (json != null) {
                    String avatar = optionalString(json.get("avatar")); // optional
                    long id = json.get("player_id").getAsLong();
                    String url = json.get("url").getAsString();
                    String name = optionalString(json.get("name")); // optional
                    String title = optionalString(json.get("title")); // optional
                    int followers = json.get("followers").getAsInt();
                    String country = getJson(json.get("country").getAsString()).get("name").getAsString();
                    String location = optionalString(json.get("location")); // optional
                    Date last_online = new Date(TimeUnit.SECONDS.toMillis(json.get("last_online").getAsLong()));
                    Date joined = new Date(TimeUnit.SECONDS.toMillis(json.get("joined").getAsLong()));
                    String status = json.get("status").getAsString();
                    boolean is_streamer = json.get("is_streamer").getAsBoolean();
                    String twitch = optionalString(json.get("twitch_url")); // optional
                    boolean verified = json.get("verified").getAsBoolean();


                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setThumbnail(avatar);
                    embed.setTitle(username, url);
                    embed.setDescription((name == null ? username : name) + (title == null ? "" : ", " + title) + ", " + country + (location == null ? "" : " " + location));
                    embed.addField("UserId", id + "", true);
                    embed.addField("Followers", followers + "", true);
                    embed.addField("Status", status, true);
                    embed.addField("Last Online", DateFormat.getDateInstance().format(last_online), true);
                    embed.addField("Joined", DateFormat.getDateInstance().format(joined), true);
                    if (twitch != null && is_streamer)
                        embed.addField("Twitch Url", twitch, false);
                    if (verified)
                        embed.setFooter("Verified User");

                    embed.setColor(Color.RED);
                    event.getChannel().sendMessageEmbeds(embed.build()).setMessageReference(reference).queue();
                } else {
                    event.getChannel().sendMessage("Invalid username.").setMessageReference(reference).queue();
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var op = event.getOption("userName");

        if (op != null) {
            String username = op.getAsString();

            JsonObject json = getJson("https://api.chess.com/pub/player/" + username);
            if (json != null) {
                String avatar = optionalString(json.get("avatar")); // optional
                long id = json.get("player_id").getAsLong();
                String url = json.get("url").getAsString();
                String name = optionalString(json.get("name")); // optional
                String title = optionalString(json.get("title")); // optional
                int followers = json.get("followers").getAsInt();
                String country = getJson(json.get("country").getAsString()).get("name").getAsString();
                String location = optionalString(json.get("location")); // optional
                Date last_online = new Date(TimeUnit.SECONDS.toMillis(json.get("last_online").getAsLong()));
                Date joined = new Date(TimeUnit.SECONDS.toMillis(json.get("joined").getAsLong()));
                String status = json.get("status").getAsString();
                boolean is_streamer = json.get("is_streamer").getAsBoolean();
                String twitch = optionalString(json.get("twitch_url")); // optional
                boolean verified = json.get("verified").getAsBoolean();


                EmbedBuilder embed = new EmbedBuilder();
                embed.setThumbnail(avatar);
                embed.setTitle(username, url);
                embed.setDescription((name == null ? username : name) + (title == null ? "" : ", " + title) + ", " + country + (location == null ? "" : " " + location));
                embed.addField("UserId", id + "", true);
                embed.addField("Followers", followers + "", true);
                embed.addField("Status", status, true);
                embed.addField("Last Online", DateFormat.getDateInstance().format(last_online), true);
                embed.addField("Joined", DateFormat.getDateInstance().format(joined), true);
                if (twitch != null && is_streamer)
                    embed.addField("Twitch Url", twitch, false);
                if (verified)
                    embed.setFooter("Verified User");

                embed.setColor(Color.RED);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            } else {
                event.getHook().sendMessage("Invalid username.").queue();
            }
        }
    }

    @Override
    public String getName() {
        return "chesscom";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{
                new OptionData(OptionType.STRING, "user_name", "Player that information will be displayed for.")
        };
    }

    @Override
    public String getCommandDescription() {
        return "Displays information on the given player from chess.com.";
    }

    @Override
    public String getCategory() {
        return CHESS;
    }
}
