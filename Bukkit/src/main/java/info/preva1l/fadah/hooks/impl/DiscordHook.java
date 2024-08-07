package info.preva1l.fadah.hooks.impl;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.Hook;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class DiscordHook implements Hook {
    private boolean enabled = false;

    public void send(Listing listing) {
        TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
            switch (Config.HOOK_DISCORD_MODE.toModeEnum()) {
                case EMBED -> sendEmbed(listing);
                case PLAIN -> sendPlain(listing);
            }
        });
    }

    private void sendEmbed(Listing listing) {
        final DiscordWebhook.EmbedObject.EmbedObjectBuilder embed = DiscordWebhook.EmbedObject.builder()
                .title(StringUtils.colorize(Config.HOOK_DISCORD_EMBED_TITLE.toString()
                        .replace("%player%", listing.getOwnerName())
                        .replace("%item%", StringUtils.extractItemName(listing.getItemStack()))
                        .replace("%price%", new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.getPrice()))))
                .description(StringUtils.colorize(Config.HOOK_DISCORD_EMBED_CONTENT.toString()
                                .replace("%player%", listing.getOwnerName())
                                .replace("%item%", StringUtils.extractItemName(listing.getItemStack()))
                                .replace("%price%", new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.getPrice()))));

        switch (Config.HOOK_DISCORD_EMBED_IMAGE.toImageLocationEnum()) {
            case SIDE -> embed.thumbnail(new DiscordWebhook.EmbedObject.Thumbnail(getImageUrlForItem(listing.getItemStack().getType())));
            case BOTTOM -> embed.image(new DiscordWebhook.EmbedObject.Image(getImageUrlForItem(listing.getItemStack().getType())));
        }

        if (!Config.HOOK_DISCORD_EMBED_FOOTER.toString().isEmpty()) {
                embed.footer(new DiscordWebhook.EmbedObject.Footer(Config.HOOK_DISCORD_EMBED_FOOTER.toString(), ""));
        }

        final DiscordWebhook webhook = new DiscordWebhook(Config.HOOK_DISCORD_URL.toString());
        webhook.addEmbed(embed.build());
        try {
            webhook.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPlain(Listing listing) {
        final DiscordWebhook webhook = new DiscordWebhook(Config.HOOK_DISCORD_URL.toString());
        webhook.setContent(StringUtils.colorize(Config.HOOK_DISCORD_PLAIN.toString()
                .replace("%player%", listing.getOwnerName())
                .replace("%item%", StringUtils.extractItemName(listing.getItemStack()))
                .replace("%price%", new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.getPrice()))));
        try {
            webhook.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getImageUrlForItem(Material material) {
        return "https://mcapi.marveldc.me/item/%s?version=1.20&width=200&height=200".formatted(material.name());
    }

    public enum Mode {
        EMBED,
        PLAIN
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

            JSONObject json = new JSONObject();

            json.put("content", this.content);
            json.put("username", this.username);
            json.put("avatar_url", this.avatarUrl);
            json.put("tts", this.tts);

            if (!this.embeds.isEmpty()) {
                List<JSONObject> embedObjects = new ArrayList<>();

                for (EmbedObject embed : this.embeds) {
                    JSONObject jsonEmbed = new JSONObject();

                    jsonEmbed.put("title", embed.getTitle());
                    jsonEmbed.put("description", embed.getDescription());
                    jsonEmbed.put("url", embed.getUrl());

                    if (embed.getColor() != null) {
                        Color color = embed.getColor();
                        int rgb = color.getRed();
                        rgb = (rgb << 8) + color.getGreen();
                        rgb = (rgb << 8) + color.getBlue();

                        jsonEmbed.put("color", rgb);
                    }

                    EmbedObject.Footer footer = embed.getFooter();
                    EmbedObject.Image image = embed.getImage();
                    EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
                    EmbedObject.Author author = embed.getAuthor();
                    List<EmbedObject.Field> fields = embed.getFields();

                    if (footer != null) {
                        JSONObject jsonFooter = new JSONObject();

                        jsonFooter.put("text", footer.getText());
                        jsonFooter.put("icon_url", footer.getIconUrl());
                        jsonEmbed.put("footer", jsonFooter);
                    }

                    if (image != null) {
                        JSONObject jsonImage = new JSONObject();

                        jsonImage.put("url", image.getUrl());
                        jsonEmbed.put("image", jsonImage);
                    }

                    if (thumbnail != null) {
                        JSONObject jsonThumbnail = new JSONObject();

                        jsonThumbnail.put("url", thumbnail.getUrl());
                        jsonEmbed.put("thumbnail", jsonThumbnail);
                    }

                    if (author != null) {
                        JSONObject jsonAuthor = new JSONObject();

                        jsonAuthor.put("name", author.getName());
                        jsonAuthor.put("url", author.getUrl());
                        jsonAuthor.put("icon_url", author.getIconUrl());
                        jsonEmbed.put("author", jsonAuthor);
                    }

                    List<JSONObject> jsonFields = new ArrayList<>();
                    for (EmbedObject.Field field : fields) {
                        JSONObject jsonField = new JSONObject();

                        jsonField.put("name", field.getName());
                        jsonField.put("value", field.getValue());
                        jsonField.put("inline", field.isInline());

                        jsonFields.add(jsonField);
                    }

                    jsonEmbed.put("fields", jsonFields.toArray());
                    embedObjects.add(jsonEmbed);
                }

                json.put("embeds", embedObjects.toArray());
            }

            URL url = new URL(this.url);
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
