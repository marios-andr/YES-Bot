package io.github.marios_andr.yesbot.command.announcements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.github.marios_andr.yesbot.Constants.*;

public record EpicStoreAnnouncement(String url, String title, String seller, String desc, String type, String image,
                                    Date endDate) implements Announcement {

    @Override
    public MessageEmbed buildEmbed() {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(seller, url)
                .setTitle(title)
                .setDescription(desc + "\n" + url)
                .setImage(image.isEmpty() ? "attachment://missing.png" : image)
                .addField("Until", endDate.toString(), true)
                .build();
    }

    public static List<Announcement> parse(String type) {
        JsonObject json = getJson("https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions");
        JsonArray arr = json.getAsJsonObject("data").getAsJsonObject("Catalog").getAsJsonObject("searchStore").getAsJsonArray("elements");

        List<Announcement> anns = new ArrayList<>();
        for (JsonElement jsonElement : arr) {
            EpicStoreAnnouncement p = null;
            try {
                p = EpicStoreAnnouncement.of(jsonElement);
            } catch (Exception e) {
                LOG.error("An error occurred while parsing epic store promotion", e);
            }
            if (p == null) {
                continue;
            }
            anns.add(p);
        }

        return anns;
    }

    public static EpicStoreAnnouncement of(JsonElement jsonElement) {
        JsonObject o = jsonElement.getAsJsonObject();
        String title = getStringOrNull(o.get("title"));
        String desc = getStringOrNull(o.get("description"));
        String type = getStringOrNull(o.get("offerType"));

        String seller = getStringOrNull(o.getAsJsonObject("seller").get("name"));
        if (title.equals("Mystery Game"))
            return null;

        int price = o.getAsJsonObject("price").getAsJsonObject("totalPrice").get("discountPrice").getAsInt();
        if (price != 0)
            return null;

        String url = "";
        urlAssign:
        {
            for (var el : o.getAsJsonArray("offerMappings")) {
                url = "https://store.epicgames.com/en-US/p/" + el.getAsJsonObject().get("pageSlug").getAsString();
                break urlAssign;
            }
            url = "https://store.epicgames.com/en-US/p/" + o.get("productSlug").getAsString();
        }

        String image = "";
        for (var el : o.getAsJsonArray("keyImages")) {
            if (getStringOrNull(el.getAsJsonObject().get("type")).equals("OfferImageWide")) {
                image = el.getAsJsonObject().get("url").getAsString();
                break;
            } else if (getStringOrNull(el.getAsJsonObject().get("type")).equals("DieselStoreFrontWide")) {
                image = el.getAsJsonObject().get("url").getAsString();
                break;
            } else if (getStringOrNull(el.getAsJsonObject().get("type")).equals("DieselGameBoxWide")) {
                image = el.getAsJsonObject().get("url").getAsString();
                break;
            }
        }

        String endDate = "";
        if (o.get("promotions").isJsonNull())
            return null;

        for (var el1 : o.getAsJsonObject("promotions").getAsJsonArray("promotionalOffers")) {
            for (var el : el1.getAsJsonObject().getAsJsonArray("promotionalOffers")) {
                var discount = el.getAsJsonObject().get("discountSetting").getAsJsonObject().get("discountPercentage").getAsJsonPrimitive();
                if (!discount.isNumber() || !Objects.equals(discount.getAsNumber().intValue(), 0)) {
                    continue;
                }
                endDate = el.getAsJsonObject().get("endDate").getAsString();
                break;
            }
        }

        if (endDate.isEmpty())
            return null;


        TemporalAccessor ta1 = DateTimeFormatter.ISO_INSTANT.parse(endDate);
        Instant i1 = Instant.from(ta1);
        Date d1 = Date.from(i1);

        return new EpicStoreAnnouncement(url, title, seller, desc, type, image, d1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpicStoreAnnouncement that = (EpicStoreAnnouncement) o;

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
