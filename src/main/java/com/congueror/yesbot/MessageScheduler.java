package com.congueror.yesbot;

import com.congueror.yesbot.mongodb.Mongo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bson.Document;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.congueror.yesbot.Constants.getJson;
import static com.congueror.yesbot.Constants.optionalString;

public class MessageScheduler {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private static final List<ScheduledFuture<?>> SCHEDULES = new ArrayList<>();

    public static void refresh(JDA jda) {
        for (ScheduledFuture<?> schedule : SCHEDULES) {
            schedule.cancel(true);
        }
        SCHEDULES.clear();

        initialize(jda);
    }

    static void initialize(JDA jda) {
        SCHEDULES.add(SCHEDULER.scheduleWithFixedDelay(() -> {
            var guilds = jda.getGuilds();
            for (Guild guild : guilds) {
                if (Mongo.hasGuildDocument(guild.getId())) {
                    long channelId = Mongo.getPromotionsChannel(guild.getId());
                    if (channelId == 0)
                        continue;

                    List<EpicStorePromotion> promos = Mongo.getLastPromotions(guild.getId());

                    TextChannel channel = jda.getTextChannelById(channelId);
                    JsonObject json = getJson("https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions");

                    JsonArray arr = json.getAsJsonObject("data").getAsJsonObject("Catalog").getAsJsonObject("searchStore").getAsJsonArray("elements");

                    List<EpicStorePromotion> proms = new ArrayList<>();
                    for (JsonElement jsonElement : arr) {
                        EpicStorePromotion p = EpicStorePromotion.of(jsonElement);
                        if (p == null)
                            continue;
                        proms.add(p);
                    }

                    if (!proms.equals(promos)) {
                        Mongo.setLastPromotions(guild.getId(), proms);
                        proms.forEach(p -> {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setColor(Color.RED)
                                    .setAuthor(p.seller, p.url)
                                    .setTitle(p.title)
                                    .setDescription(p.desc + "\n" + p.url)
                                    .setImage(p.image)
                                    .addField("Until", p.endDate.toString(), true);
                            channel.sendMessageEmbeds(embed.build()).queue();
                        });
                    }
                }
            }
        }, 0, 30, TimeUnit.MINUTES));
    }

    public record EpicStorePromotion(String url, String title, String seller, String desc, String type, String image, Date endDate) {

        public static EpicStorePromotion of(Document doc) {
            return new EpicStorePromotion(
                    doc.getString("url"), doc.getString("title"), doc.getString("seller"),
                    doc.getString("desc"), doc.getString("type"), doc.getString("image"),
                    doc.getDate("endDate")
            );
        }

        public static EpicStorePromotion of(JsonElement jsonElement) {
            JsonObject o = jsonElement.getAsJsonObject();
            String seller = optionalString(o.getAsJsonObject("seller").get("name"));
            if (seller.equals("Epic Dev Test Account"))
                return null;

            int price = o.getAsJsonObject("price").getAsJsonObject("totalPrice").get("discountPrice").getAsInt();
            if (price != 0)
                return null;

            String url = "";
            for (var el : o.getAsJsonArray("offerMappings")) {
                url = "https://store.epicgames.com/en-US/p/" + el.getAsJsonObject().get("pageSlug").getAsString();
                break;
            }

            String title = optionalString(o.get("title"));
            String desc = optionalString(o.get("description"));
            String type = optionalString(o.get("offerType"));

            String image = "";
            for (var el : o.getAsJsonArray("keyImages")) {
                if (optionalString(el.getAsJsonObject().get("type")).equals("OfferImageWide")) {
                    image = el.getAsJsonObject().get("url").getAsString();
                    break;
                }
            }

            String endDate = "";
            for (var el1 : o.getAsJsonObject("promotions").getAsJsonArray("promotionalOffers")) {
                for (var el : el1.getAsJsonObject().getAsJsonArray("promotionalOffers")) {
                    endDate = el.getAsJsonObject().get("endDate").getAsString();
                    break;
                }
            }

            TemporalAccessor ta1 = DateTimeFormatter.ISO_INSTANT.parse(endDate);
            Instant i1 = Instant.from(ta1);
            Date d1 = Date.from(i1);

            return new EpicStorePromotion(url, title, seller, desc, type, image, d1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EpicStorePromotion that = (EpicStorePromotion) o;

            if (!url.equals(that.url)) return false;
            if (!title.equals(that.title)) return false;
            if (!seller.equals(that.seller)) return false;
            if (!desc.equals(that.desc)) return false;
            if (!type.equals(that.type)) return false;
            if (!image.equals(that.image)) return false;
            return endDate.equals(that.endDate);
        }

        @Override
        public int hashCode() {
            int result = url.hashCode();
            result = 31 * result + title.hashCode();
            result = 31 * result + seller.hashCode();
            result = 31 * result + desc.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + image.hashCode();
            result = 31 * result + endDate.hashCode();
            return result;
        }
    }

}
