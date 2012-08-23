package com.burstly.conveniencelayer.events;

import com.burstly.lib.ui.BurstlyView;

import java.util.ArrayList;

/**
 * Event containing details of a failure in loading an ad
 */
public class AdFailEvent extends AdEvent {
    /**
     * The list of networks which Burstly attempted to retrieve an ad from but did not return a valid
     */
    private final ArrayList<String> mFailedCreativeNetworks;

    /**
     * The minimum amount of time in ms that must be waited until the next request is made
     */
    private int mMinTimeUntilNextRequest;

    /**
     * Flag telling if a failure occured because the request was throttled
     */
    private boolean mRequestThrottled;

    /**
     * Construct a new AdThrottleEvent
     * @param minTimeUntilNextRequest the minimum amount of time in ms that must be waited until the next request is made
     */
    public AdFailEvent(int minTimeUntilNextRequest) {
        mFailedCreativeNetworks = null;
        mRequestThrottled = true;
        mMinTimeUntilNextRequest = minTimeUntilNextRequest;
    }

    /**
     * Constructs a new AdFailEvent
     * @param failedCreativeNetworks The list of networks which Burstly attempted to retrieve but did not provide a valid ad
     */
    public AdFailEvent(final ArrayList<String> failedCreativeNetworks, final BurstlyView burstlyView) {
        mFailedCreativeNetworks = failedCreativeNetworks;
        mRequestThrottled = false;
        mMinTimeUntilNextRequest = burstlyView.getMinTimeUntilNextRequest(); 
    }

    /**
     * Gets the list of networks which Burstly attempted to retrieve an ad from but did not provide a valid ad
     * @return The list of networks which Burstly attempted to retrieve an ad from but did not provide a valid ad
     */
    public ArrayList<String> getFailedCreativesNetworks() {
        return mFailedCreativeNetworks;
    }

    /**
     * Gets whether this failure was a result of the request being throttled
     * @return true if the failure resulted from the request being throttled, false if it failed for some other reason.
     */
    public boolean wasRequestThrottled() {
        return mRequestThrottled;
    }

    /**
     * Gets the minimum amount of time in ms that must be waited until the next request is made
     * @return the minimum amount of time in ms that must be waited until the next request is made
     */
    public int getMinTimeUntilNextRequest() {
        return mMinTimeUntilNextRequest;
    }
}
