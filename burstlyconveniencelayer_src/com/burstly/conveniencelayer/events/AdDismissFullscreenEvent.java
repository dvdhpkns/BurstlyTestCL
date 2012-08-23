package com.burstly.conveniencelayer.events;

/**
 * Event containing data on the ad that was dismissed
 */
public class AdDismissFullscreenEvent extends AdEvent {
    /**
     * The {@link AdShowEvent} event that caused an a fullscreen ad to be presented fullscreen, and then dismissed
     */
    private final AdShowEvent mMatchingShowEvent;

    /**
     * Construct a new AdDismissFullscreenEvent
     * @param showEvent the {@link AdShowEvent} that triggered the ad being presented fullscreen, and subsequently dismissed
     */
    public AdDismissFullscreenEvent(final AdShowEvent showEvent) {
        mMatchingShowEvent = showEvent;
    }

    /**
     * Get the {@link AdShowEvent} that triggered the ad being presented fullscreen, and subsequently dismissed
     * @return The {@link AdShowEvent} that triggered the ad being presented fullscreen, and subsequently dismissed
     */
    public AdShowEvent getMatchingShowEvent() {
        return mMatchingShowEvent;
    }
}
