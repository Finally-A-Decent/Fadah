package info.preva1l.fadah.hooks.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Text;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class DiscordHook {
    private Config.Hooks.Discord conf = Config.i().getHooks().getDiscord();
    private DecimalFormat df = Config.i().getFormatting().numbers();

    public boolean onStart() {
        conf = Config.i().getHooks().getDiscord();
        df = Config.i().getFormatting().numbers();
        return true;
    }

    public void send(Listing listing) {
        CompletableFuture.runAsync(() -> {
            switch (conf.getMessageMode()) {
                case EMBED -> sendEmbed(listing);
                case PLAIN_TEXT -> sendPlain(listing);
            }
        }, DataService.instance.getThreadPool());
    }

    private void sendEmbed(Listing listing) {
        Config.Hooks.Discord.Embed embedConf = conf.getEmbed();
        final DiscordWebhook.EmbedObject.EmbedObjectBuilder embed = DiscordWebhook.EmbedObject.builder()
                .title(formatString(embedConf.getTitle(), listing))
                .description(formatString(embedConf.getContent(), listing));

        switch (embedConf.getImageLocation()) {
            case SIDE -> embed.thumbnail(new DiscordWebhook.EmbedObject.Thumbnail(getImageUrlForItem(listing.getItemStack().getType())));
            case BOTTOM -> embed.image(new DiscordWebhook.EmbedObject.Image(getImageUrlForItem(listing.getItemStack().getType())));
        }

        if (!embedConf.getFooter().isEmpty()) {
                embed.footer(new DiscordWebhook.EmbedObject.Footer(embedConf.getFooter(), ""));
        }

        final DiscordWebhook webhook = new DiscordWebhook(conf.getWebhookUrl());
        webhook.addEmbed(embed.build());
        try {
            webhook.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPlain(Listing listing) {
        final DiscordWebhook webhook = new DiscordWebhook(conf.getWebhookUrl());
        webhook.setContent(formatString(conf.getPlainText(), listing));
        try {
            webhook.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatString(String str, Listing listing) {
        return str
                .replace("%player%", listing.getOwnerName())
                .replace("%item%", Text.extractItemNameToString(listing.getItemStack()))
                .replace("%price%", df.format(listing.getPrice()));
    }

    private String getImageUrlForItem(Material material) {
        return "https://minecraft-api.vercel.app/images/items/%s.png".formatted(material.getKey().value());
    }

    public enum Mode {
        EMBED,
        PLAIN_TEXT
    }

    public enum ImageLocation {
        SIDE,
        BOTTOM
    }

    public static class DiscordWebhook {

        private final String url;
        @Setter
        private String content;
        @Setter
        private String username;
        @Setter
        private String avatarUrl;
        @Setter
        private boolean tts;
        private final List<EmbedObject> embeds = new ArrayList<>();

        /**
         * Constructs a new DiscordWebhook instance
         *
         * @param url The webhook URL obtained in Discord
         */
        public DiscordWebhook(String url) {
            this.url = url;
        }

        public void addEmbed(EmbedObject embed) {
            this.embeds.add(embed);
        }

        public void execute() throws IOException {
            if (this.content == null && this.embeds.isEmpty()) {
                throw new IllegalArgumentException("Set content or add at least one EmbedObject");
            }

            JsonObject json = new JsonObject();

            json.addProperty("content", this.content);
            json.addProperty("username", this.username);
            json.addProperty("avatar_url", this.avatarUrl);
            json.addProperty("tts", this.tts);

            if (!this.embeds.isEmpty()) {
                JsonArray embedObjects = new JsonArray();

                for (EmbedObject embed : this.embeds) {
                    JsonObject jsonEmbed = new JsonObject();

                    jsonEmbed.addProperty("title", embed.getTitle());
                    jsonEmbed.addProperty("description", embed.getDescription().replace("\\n", "\n"));
                    jsonEmbed.addProperty("url", embed.getUrl());

                    if (embed.getColor() != null) {
                        Color color = embed.getColor();
                        int rgb = color.getRed();
                        rgb = (rgb << 8) + color.getGreen();
                        rgb = (rgb << 8) + color.getBlue();

                        jsonEmbed.addProperty("color", rgb);
                    }

                    EmbedObject.Footer footer = embed.getFooter();
                    EmbedObject.Image image = embed.getImage();
                    EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
                    EmbedObject.Author author = embed.getAuthor();
                    List<EmbedObject.Field> fields = embed.getFields();

                    if (footer != null) {
                        JsonObject jsonFooter = new JsonObject();

                        jsonFooter.addProperty("text", footer.getText());
                        jsonFooter.addProperty("icon_url", footer.getIconUrl());
                        jsonEmbed.add("footer", jsonFooter);
                    }

                    if (image != null) {
                        JsonObject jsonImage = new JsonObject();

                        jsonImage.addProperty("url", image.getUrl());
                        jsonEmbed.add("image", jsonImage);
                    }

                    if (thumbnail != null) {
                        JsonObject jsonThumbnail = new JsonObject();

                        jsonThumbnail.addProperty("url", thumbnail.getUrl());
                        jsonEmbed.add("thumbnail", jsonThumbnail);
                    }

                    if (author != null) {
                        JsonObject jsonAuthor = new JsonObject();

                        jsonAuthor.addProperty("name", author.getName());
                        jsonAuthor.addProperty("url", author.getUrl());
                        jsonAuthor.addProperty("icon_url", author.getIconUrl());
                        jsonEmbed.add("author", jsonAuthor);
                    }

                    JsonArray jsonFields = new JsonArray();
                    for (EmbedObject.Field field : fields) {
                        JsonObject jsonField = new JsonObject();

                        jsonField.addProperty("name", field.getName());
                        jsonField.addProperty("value", field.getValue());
                        jsonField.addProperty("inline", field.isInline());

                        jsonFields.add(jsonField);
                    }

                    jsonEmbed.add("fields", jsonFields);
                    embedObjects.add(jsonEmbed);
                }

                json.add("embeds", embedObjects);
            }

            URL url = URI.create(this.url).toURL();
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Fadah-Discord-Webhook");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStream stream = connection.getOutputStream();
            stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();

            connection.getInputStream().close();
            connection.disconnect();
        }

        @Getter
        @Setter
        @Builder
        public static class EmbedObject {
            private String title;
            private String description;
            private String url;
            private Color color;

            private Footer footer;
            private Thumbnail thumbnail;
            private Image image;
            private Author author;
            private final List<Field> fields = new ArrayList<>();

            @Getter
            private static class Footer {
                private final String text;
                private final String iconUrl;

                private Footer(String text, String iconUrl) {
                    this.text = text;
                    this.iconUrl = iconUrl;
                }
            }

            @Getter
            private static class Thumbnail {
                private final String url;

                private Thumbnail(String url) {
                    this.url = url;
                }
            }

            @Getter
            private static class Image {
                private final String url;

                private Image(String url) {
                    this.url = url;
                }
            }

            @Getter
            private static class Author {
                private final String name;
                private final String url;
                private final String iconUrl;

                private Author(String name, String url, String iconUrl) {
                    this.name = name;
                    this.url = url;
                    this.iconUrl = iconUrl;
                }
            }

            @Getter
            private static class Field {
                private final String name;
                private final String value;
                private final boolean inline;

                private Field(String name, String value, boolean inline) {
                    this.name = name;
                    this.value = value;
                    this.inline = inline;
                }
            }
        }
    }
}
