package com.congueror.yesbot.command.commands.economy;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.shop.Shop;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ShopCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] shop = getInput(event);
        if (check(shop)) {
            Message reference = event.getMessage();

            if (shop.length == 1) {
                Shop shop1 = new Shop(reference.getAuthor().getId());
                event.getChannel().sendFiles(FileUpload.fromData(shop1.drawShop())).setMessageReference(reference).queue();
            }
        }
    }

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public String[] getArgs() {
        return new String[] {"item"};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("A place where you can purchase items with points you have accumulated");
        return StringUtils.join(desc, String.format("%n", ""));
    }
}
