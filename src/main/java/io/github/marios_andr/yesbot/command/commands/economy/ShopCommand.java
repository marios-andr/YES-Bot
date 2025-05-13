package io.github.marios_andr.yesbot.command.commands.economy;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.shop.Shop;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

public class ShopCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] shop = getInput(event.getMessage());
        if (check(shop)) {
            Message reference = event.getMessage();

            if (shop.length == 1) {
                Shop shop1 = new Shop(reference.getAuthor().getId());
                event.getChannel().sendFiles(FileUpload.fromData(shop1.drawShop())).setMessageReference(reference).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

    }

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.STRING, "item", "The item to be bought", false)
        };
    }

    @Override
    public String getCommandDescription() {
        return "A place where you can purchase items with points you have accumulated";
    }
}
