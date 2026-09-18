package me.braydon.profanity.notification.impl;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.braydon.profanity.common.DiscordWebhook;
import me.braydon.profanity.model.FilterContext;
import me.braydon.profanity.model.response.ContentProcessResponse;
import me.braydon.profanity.notification.INotificationSource;
import me.braydon.profanity.notification.NotificationContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A notification source that
 * sends alerts to a Discord webhook.
 *
 * @author Braydon
 */
@Component
public final class DiscordSource implements INotificationSource {
    @Value("${notifications.sources.Discord.url}")
    private String webhookUrl;

    @Value("${notifications.sources.Discord.username}")
    private String username;

    @Value("${notifications.sources.Discord.avatar}")
    private String avatarUrl;

    /**
     * The instance of the webhook to use.
     */
    private DiscordWebhook webhook;

    @PostConstruct
    public void onInitialize() {
        webhook = new DiscordWebhook(webhookUrl = webhookUrl.trim());
        webhook.setUsername(username);
        webhook.setAvatarUrl(avatarUrl);
    }

    /**
     * Check if this source is enabled.
     *
     * @return whether this source is enabled
     */
    @Override
    public boolean isEnabled() {
        return !webhookUrl.isEmpty();
    }

    /**
     * Send an alert to this source.
     *
     * @param response the content response to alert for
     * @param content  the content to send in the alert
     */
    @Override @SneakyThrows
    public void alert(@NonNull ContentProcessResponse response, @NonNull NotificationContent content) {
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                .setColor(Color.RED)
                .setTitle("Content Filtered");
        if (content.isDisplayContent()) {
            embed.addField("Content", "`" + response.getContent() + "`", false);
        }
        if (content.isDisplayMatched()) {
            embed.addField("Matched", response.getMatched().stream().map(matched -> "`" + matched + "`").collect(Collectors.joining(", ")), false);
        }
        if (content.isDisplayTags()) {
            embed.addField("Tags", response.getTags().stream().map(tag -> "`" + tag.name() + "`").collect(Collectors.joining(", ")), false);
        }
        if (content.isDisplayScore()) {
            embed.addField("Score", "`" + response.getScore() + "`", false);
        }
        FilterContext context = response.getContext();
        if (content.isDisplayContext() && context != null && !context.isEmpty()) {
            for (Map.Entry<String, String> entry : context.getValues().entrySet()) {
                embed.addField(formatContextFieldName(entry.getKey()), "`" + entry.getValue() + "`", false);
            }
        }
        webhook.addEmbed(embed);
        webhook.execute();
    }

    /**
     * Format a context key for display in an embed field title.
     */
    @NonNull
    private static String formatContextFieldName(@NonNull String key) {
        String[] parts = key.replace('-', ' ').replace('_', ' ').split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.isEmpty() ? key : builder.toString();
    }
}