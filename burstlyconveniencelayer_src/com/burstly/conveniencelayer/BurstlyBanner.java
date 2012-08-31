package com.burstly.conveniencelayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import com.burstly.lib.ui.BurstlyView;

/**
 * A default static banner ad placement which is attached to an activity and left visible and active during the life
 * of the Activity
 */
public class BurstlyBanner extends BurstlyBaseAd {
    /**
     * Constructor for an BurstlyBanner built from a {@link BurstlyView} that has been attached to a layout file
     * @param activity The {@link Activity} associated with the banner
     * @param id The id for the BurstlyView in the layout
     */
    public BurstlyBanner(final Activity activity, int id) {
        super(activity);

        final BurstlyView burstlyView = (BurstlyView)activity.findViewById(id);
        setBurstlyView(burstlyView);
    }

    /**
     * Constructs an BurstlyBanner in code and attaches it to a ViewGroup
     * @param activity The {@link Activity} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param appId The appId for this application
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param refreshRate The number of seconds between banner refreshes (Minimum 10 seconds)
     */
    public BurstlyBanner(final Activity activity, final ViewGroup group, final ViewGroup.LayoutParams params, final String appId, final String zoneId, final String viewName, int refreshRate) {
        super(activity);

        final BurstlyView burstlyView  = new BurstlyView(activity);
        burstlyView.setPublisherId(appId);
        burstlyView.setZoneId(zoneId);
        burstlyView.setBurstlyViewId(viewName);
        burstlyView.setDefaultSessionLife(refreshRate);

        setBurstlyView(burstlyView);

        if(params != null)
            group.addView(burstlyView, params);
        else
            group.addView(burstlyView);
    }

    /**
     * Set integration mode for an instance of burstly banner
     *
     * @param adNetwork the ad network specified from the enum {@link BurstlyIntegrationModeAdNetworks}
     * @param testDeviceIds {@link String} device Ids that will run in Integration Mode
     * @param context {@link android.content.Context} current context
     */
    public void setIntegrationMode(BurstlyIntegrationModeAdNetworks adNetwork, String[] testDeviceIds, Context context){
        super.setIntegrationMode(adNetwork, testDeviceIds, context, true);
    }

    /**
     * {@link BurstlyView} attached to a parent
     */
    protected void burstlyViewAttachedToWindow() {
        Log.i(Burstly.TAG, getName() + " attached to parent.");
    }

    /**
     * {@link BurstlyView} removed from it's parent
     */
    protected void burstlyViewDetachedFromWindow() {
        Log.e(Burstly.TAG, getName() + " is being removed from it's parent. Use an AnimatedBanner if you wish to be able to hide and show an ad");
    }
}
