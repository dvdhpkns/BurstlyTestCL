package com.burstly.conveniencelayer;

import android.app.Activity;
import android.content.Context;
import com.burstly.conveniencelayer.events.AdShowEvent;
import com.burstly.lib.ui.BurstlyView;

/**
 * BurstlyInterstitial should be used with zones marked as interstitial zones in the burstly.com UI and will be launched
 * in their own activity.  The {@link Activity} launched will depend on who the ad provider is and the type of ad. For
 * Burstly house and direct ads, as well as server side feeds the BurstlyFullscreenActivity.  For other providers
 * whose SDKs are integrated into the Burstly SDK there own Activities will be used.  See the documentation for the
 * necessary manifest entries
 */
public class BurstlyInterstitial extends BurstlyBaseAd implements ICacheable {
    /**
     * Constructs a BurstlyInterstitial used for retrieving and triggering an interstitial shown in a new {@link Activity}
     * @param activity The {@link Activity} where the interstitials are requested from and will be launched from
     * @param appId The appId for this application
     * @param zoneId The zoneId for these interstitials
     * @param viewName The name of this view which will be used to identify it in teh logs
     */
    public BurstlyInterstitial(final Activity activity, final String appId, final String zoneId, final String viewName) {
        super(activity);

        final BurstlyView burstlyView= new BurstlyView(activity);
        burstlyView.setPublisherId(appId);
        burstlyView.setZoneId(zoneId);
        burstlyView.setBurstlyViewId(viewName);

        setBurstlyView(burstlyView);
    }

    /**
     * Callback received if the associated BurstlyView is attached to a parent.  This is not allowed for interstitials
     * and will throw an exception.
     */
    protected void burstlyViewAttachedToWindow() {
        throw new RuntimeException("Interstitial BurstlyView should not be attached to the window.");
    }

    /**
     * Callback received if the associated BurstlyView is detached from its parent.  This should never happen
     */
    protected void burstlyViewDetachedFromWindow() {}

    /**
     * Set integration mode for an instance of burstly banner
     *
     * @param adNetwork the ad network specified from the enum {@link BurstlyIntegrationModeAdNetworks}
     * @param testDeviceIds {@link String[]} device Ids that will run in Integration Mode
     * @param context {@link android.content.Context} current context
     */
    @Override
    public void setIntegrationMode(BurstlyIntegrationModeAdNetworks adNetwork, String[] testDeviceIds, Context context) {
        super.setIntegrationMode(adNetwork, testDeviceIds, context, false);
    }

    /**
     * An ad was loaded and will display
     * @param event {@link AdShowEvent} containing data on the ad shown
     */
    @Override
    protected void onShow(final AdShowEvent event) {
        if(!event.isActivityInterstitial())
            throw new RuntimeException("BurstlyInterstitial being used with a non interstitial zone");
        
        super.onShow(event);
    }

    /**
     * caches an ad to be shown later
     */
    public void cacheAd() {
        super.baseCacheAd();
    }

    /**
     * Gets whether there is a cached ad ready to be shown
     * @return true if a cached ad is available to be shown. False otherwise.
     */
    public boolean hasCachedAd() {
        return super.baseHasCachedAd();
    }

    /**
     * Gets whether an ad is being retrieved and cached currently
     * @return true if currently retrieving an ad to cache. False otherwise.
     */
    public boolean isCachingAd() {
        return super.baseIsCachingAd();
    }
}
