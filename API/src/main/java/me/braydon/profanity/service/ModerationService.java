package me.braydon.profanity.service;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import me.braydon.profanity.model.response.ContentProcessResponse;
import me.braydon.profanity.notification.INotificationSource;
import me.braydon.profanity.notification.NotificationContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service is responsible for moderating text
 * content and reporting it to the appropriate parties.
 *
 * @author Braydon
 */
@Service @Log4j2(topic = "Moderation")
public class ModerationService {
    @Value("${notifications.enabled}")
    private boolean enabled;

    @Value("${notifications.content.content}")
    private boolean displayContent;

    @Value("${notifications.content.matched}")
    private boolean displayMatched;

    @Value("${notifications.content.tags}")
    private boolean displayTags;

    @Value("${notifications.content.score}")
    private boolean displayScore;

    /**
     * The content to display within notifications.
     */
    @NonNull private final NotificationContent notificationContent;

    /**
     * The registered notification sources to alert.
     */
    @NonNull private final List<INotificationSource> notificationSources;

    @Autowired
    public ModerationService(@NonNull List<INotificationSource> notificationSources) {
        notificationContent = new NotificationContent(displayContent, displayMatched, displayTags, displayScore);
        this.notificationSources = notificationSources;
    }

    /**
     * Handle alerts for the given response.
     * <p>
     * If the content in the given response
     * contains profanity, notification sources
     * will be notified.
     * </p>
     *
     * @param response the response to handle
     */
    @Async
    public void handleAlerts(@NonNull ContentProcessResponse response) {
        // Disabled or likely safe content, no need to alert anyone
        if (!enabled || response.getScore() < 0.6D) {
            return;
        }
        // Notify sources
        int notified = 0;
        for (INotificationSource source : notificationSources) {
            if (!source.isEnabled()) {
                continue;
            }
            notified++;
            source.alert(response, notificationContent);
        }
        if (notified > 0) {
            log.info("Notified {} sources of filtered content", notified);
        }
    }
}