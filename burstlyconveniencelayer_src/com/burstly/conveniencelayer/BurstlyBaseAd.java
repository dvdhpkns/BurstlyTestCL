package com.burstly.conveniencelayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import com.burstly.conveniencelayer.events.*;
import com.burstly.lib.ui.AdSize;
import com.burstly.lib.ui.BurstlyView;
import com.burstly.lib.ui.IBurstlyAdListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The BurstlyBaseAd is the base convenience layer wrapper class for all ads.
 */
public abstract class BurstlyBaseAd implements IActivityListener {
    /**
     * The state of having a ad cached
     */
    protected enum CachingState {
        Idle,
        CacheRequestThrottled,
        Retrieving,
        Retrieved
    }

    /**
     * Throw an exception if the current thread is not the UI thread
     */
    protected static void throwIfNotOnMainThread() {
        if(Thread.currentThread() != Looper.getMainLooper().getThread())
            throw new RuntimeException("com.burstly.convenience.Banner must be called from the ui thread");
    }

    /**
     * The {@link BurstlyView} associated with this ad
     */
    private BurstlyView mBurstlyView;

    /**
     * The {@link Activity} this ad is associated with
     */
    protected final Activity mActivity;

    /**
     * The {@link IBurstlyListener} which will receive callbacks when ad events occur
     */
    protected List<IBurstlyListener> mListeners;

    /**
     * The list of failed creatives that is built every time a request is made
     */
    protected ArrayList<String> mFailedCreativesList;

    /**
     * The current creative being shown.
     */
    protected String mCurrentCreative;

    /**
     * The state of the current ads cache
     */
    protected CachingState mCachingState;

    /**
     * Event data for the last cached ad
     */
    protected AdCacheEvent mLastCache;

    /**
     * Event data for the last shown ad
     */
    protected AdShowEvent mLastShow;


    /**
     * Raw device id.
     */
    private static String sDeviceId;

    /**
     * Flag used to determine whether the test mode alert has been shown
     */
    private static boolean testModeAlertShown = false;

    /**
     * Listener receiving callbacks from the {@link BurstlyView}
     */
    private IBurstlyAdListener mBurstlyAdListener = new IBurstlyAdListener() {
        /**
         * Called when a single network fails to load.
         * @param network {@link String} loaded network name
         */
        public void failedToLoad(String network) {
            singleCreativeFailed(network);
        }

        /**
         * An ad was loaded and will display
         * @param network {@link String} loaded network name
         * @param isInterstitial {@code boolean} defines whether the ad will be loaded in a new Activity
         */
        public void didLoad(final String network, boolean isInterstitial) {
            boolean isRefresh = (mLastShow != null);
            if(isRefresh)
                BurstlyBaseAd.this.onHide(new AdHideEvent(true, mLastShow));

            mLastShow = new AdShowEvent(isInterstitial, network, mFailedCreativesList, isRefresh);
            BurstlyBaseAd.this.onShow(mLastShow);
        }

        /**
         * An ad was cached
         * @param network {@link String} loaded network name
         */
        public void didPrecacheAd(String network) {
            mLastCache = new AdCacheEvent(network, mFailedCreativesList);
            onCache(mLastCache);
        }

        /**
         * Beginning a request to the server
         */
        public void startRequestToServer() {
            requestStarted();
        }

        /**
         * Attempting to load a creative that was returned by the server
         * @param network {@link String} network which we are trying to load
         */
        public void attemptingToLoad(String network) {
            tryToLoadCreative(network);
        }

        /**
         * Ad was clicked on
         * @param network {@link String} network which was clicked
         */
        public void adNetworkWasClicked(String network) {
            BurstlyBaseAd.this.onClick(new AdClickEvent(network));
        }

        /**
         * Request throttled
         * @param timeInMsec {@code int} minimum amount of time until a new request can be made
         */
        public void requestThrottled(int timeInMsec) {
            BurstlyBaseAd.this.onFail(new AdFailEvent(timeInMsec));
        }

        /**
         * Failed to load any of the creatives in the assigned zones
         */
        public void failedToDisplayAds() {
            BurstlyBaseAd.this.onFail(new AdFailEvent(mFailedCreativesList, mBurstlyView));
        }

        /**
         * A fullscreen activity interstitial was dismissed
         * @param network {@link String} name of network which was dismissed
         */
        public void adNetworkDismissFullScreen(String network) {
            BurstlyBaseAd.this.onDismissFullscreen(new AdDismissFullscreenEvent(mLastShow));
            mLastShow = null;
        }

        /**
         * A fullscreen activity interstitial was dismissed
         * @param network {@link String} name of network which was dismissed
         */
        public void adNetworkPresentFullScreen(String network) {
            BurstlyBaseAd.this.onPresentFullscreen(new AdPresentFullscreenEvent());
        }

        public void finishRequestToServer() {}
        public void viewDidChangeSize(AdSize newSize, AdSize oldSize) {}
        public void onHide() {}
        public void onShow() {}
        public void onExpand(boolean isFullscreen) {}
        public void onCollapse() {}
    };

    /**
     * private listener monitors whether the {@link BurstlyView} has been attached to a ViewGroup
     */
    private View.OnAttachStateChangeListener mAttachListener = new View.OnAttachStateChangeListener() {
        public void onViewAttachedToWindow(View view) {
            burstlyViewAttachedToWindow();
        }

        public void onViewDetachedFromWindow(View view) {
            burstlyViewDetachedFromWindow();
        }
    };

    /**
     * protected constructor for abstract class
     * @param activity {@link Activity} associated with this ad
     */
    protected BurstlyBaseAd(final Activity activity) {
        mActivity = activity;
        mCachingState = CachingState.Idle;
        mListeners = new ArrayList<IBurstlyListener>();
    }

    /**
     * sets the {@link BurstlyView} used by this ad
     * @param burstlyView The {@link BurstlyView} instance used to access the BurstlySDK and show content
     */
    protected void setBurstlyView(final BurstlyView burstlyView) {
        throwIfNotOnMainThread();

        if(mBurstlyView != null)
            throw new RuntimeException("BurstlyView cannot be changed.");

        mBurstlyView = burstlyView;
        mBurstlyView.addOnAttachStateChangeListener(mAttachListener);
        mBurstlyView.setBurstlyAdListener(mBurstlyAdListener);
        
        Burstly.get().addActivityListener(mActivity, this);
    }

    /**
     * retrieve the {@link BurstlyView} used by this ad     *
     * @return The {@link BurstlyView} used by this ad
     */
    protected BurstlyView getBurstlyView() {
        return mBurstlyView;
    }

    /**
     * Called when an ad is hidden which can occur by itself or just before a show event in the case of a refresh
     * @param event {@link AdHideEvent} containing data on the ad that was hidden
     */
    protected void onHide(final AdHideEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onHide(this, event);
        }

        mLastShow = null;
    }

    /**
     * Called when an ad will be shown
     * @param event {@link AdShowEvent} containing data on the ad shown
     */
    protected void onShow(final AdShowEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onShow(this, event);
        }
    }

    /**
     * Called when an ad is cached
     * @param event {@link AdCacheEvent} containing data on the cached ad
     */
    protected void onCache(final AdCacheEvent event) {
        mCachingState =  CachingState.Retrieved;

        for(final IBurstlyListener listener:mListeners) {
            listener.onCache(this, event);
        }
    }

    /**
     * Called when an ad is clicked
     * @param event {@link AdClickEvent} containing data on the ad that was clicked
     */
    protected void onClick(final AdClickEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onClick(this, event);
        }
    }

    /**
     * Called when a request for an ad fails to fill
     * @param event {@link AdFailEvent} containing data on the failure
     */
    protected void onFail(final AdFailEvent event) {
        mCachingState = CachingState.Idle;

        for(final IBurstlyListener listener:mListeners) {
            listener.onFail(this, event);
        }
    }

    /**
     * Called when an interstitial, which was launched in it's own activity, was dismissed
     * @param event {@link AdDismissFullscreenEvent} containing info on the dismissed ad
     */
    protected void onDismissFullscreen(final AdDismissFullscreenEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onDismissFullscreen(this, event);
        }
    }

    /**
     * Called when a new {@link Activity} will launch to display an interstitial
     * @param event {@link AdPresentFullscreenEvent} containing info on the ad which will display
     */
    protected void onPresentFullscreen(final AdPresentFullscreenEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onPresentFullscreen(this, event);
        }
    }

    /**
     * Base functionality for a new request clears the failed creatives list
     */
    protected void requestStarted() {
        mFailedCreativesList = new ArrayList<String>();
    }

    /**
     * Base functionality for a failure to load a creative
     * @param network The network which failed to load
     */
    protected void singleCreativeFailed(final String network) {
        mFailedCreativesList.add(network);
    }

    /**
     * Base functionality for an attempt to load a creative
     * @param network The network being attempted
     */
    protected void tryToLoadCreative(final String network) {
        mCurrentCreative = network;
    }

    /**
     * abstract method must be overridder by subclasses to monitor ViewGroup attachment state
     */
    protected abstract void burstlyViewAttachedToWindow();

    /**
     * abstract method must be overridder by subclasses to monitor ViewGroup attachment state
     */
    protected abstract void burstlyViewDetachedFromWindow();

    /**
     * show an ad
     */
    public void showAd() {
        throwIfNotOnMainThread();

        mCachingState = CachingState.Idle;
        mBurstlyView.sendRequestForAd();
    }

    /**
     * caches an ad to be shown later 
     */
    protected void baseCacheAd() {
        Burstly.logW("cacheAd start");
        if(baseHasCachedAd()) {
            Log.w(Burstly.TAG, getName() + ": Ad already cached.");
            onCache(mLastCache);
        }
        else {
            throwIfNotOnMainThread();
            mCachingState = CachingState.Retrieving;
            mBurstlyView.precacheAd();
        }
        Burstly.logW("cacheAd end");
    }

    /**
     * Gets whether there is a cached ad ready to be shown
     * @return true if a cached ad is available to be shown. False otherwise.
     */
    protected boolean baseHasCachedAd() {
        return (mCachingState == CachingState.Retrieved && !mBurstlyView.isCachedAdExpired());
    }

    /**
     * Gets whether an ad is being retrieved and cached currently
     * @return true if currently retrieving an ad to cache. False otherwise.
     */
    protected boolean baseIsCachingAd() {
        return (mCachingState == CachingState.Retrieving);
    }

    /**
     * Add a {@link IBurstlyListener} which will receive callbacks when ad events occur
     * @param listener A {@link IBurstlyListener} which will receive callbacks when ad events occur
     */
    public synchronized void addBurstlyListener(final IBurstlyListener listener) {
        mListeners.add(listener);
    }

    /**
     * Removes a {@link IBurstlyListener} from the list of liseteners
     * @param listener {@link IBurstlyListener} to be removed 
     */
    public synchronized void removeBurstlyListener(final IBurstlyListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Get the name associated with this ad.  This is the ad which is used to identify this ad in the logs.
     * @return The name of this ad space
     */
    public String getName() {
        return mBurstlyView.getBurstlyViewId();
    }

    /**
     * Get the App Id used by this ad
     * @return The App Id used in requests by this ad
     */
    public String getAppId() {
        return mBurstlyView.getPublisherId();
    }

    /**
     * Get the Zone Id used by this ad
     * @return The Zone Id used in requests by this ad
     */
    public String getZoneId() {
        return mBurstlyView.getZoneId();
    }

    /**
     * Set the App Id used by this ad
//     */
//    protected void setAppId(String appId) {
//        mBurstlyView.setPublisherId(appId);
//    }
//
//    /**
//     * Set the Zone Id used by this ad
//     */
//    private void setZoneId(String zoneId) {
//        mBurstlyView.setZoneId(zoneId);
//    }

    /**
     * Called by the convenience layer when the activity associated with this ad is paused.  If you are not using the
     * convenience layer then this needs to be called in the onPause of the {@link Activity} associated with this ad.
     * @param activity {@link Activity} that was paused
     */
    public void activityPaused(final Activity activity) {
        throwIfNotOnMainThread();

        mBurstlyView.onHideActivity();

        if(mCachingState == CachingState.Retrieving || mCachingState == CachingState.CacheRequestThrottled)
            mCachingState = CachingState.Idle;
    }

    /**
     * Called by the convenience layer when the activity associated with this ad is resumed.  If you are not using the
     * convenience layer then this needs to be called in the onResume of the {@link Activity} associated with this ad.
     * @param activity {@link Activity} that was resumed
     */
    public void activityResumed(final Activity activity) {
        throwIfNotOnMainThread();

        mBurstlyView.onShowActivity();
    }

    /**
     * Called by the convenience layer when the activity associated with this ad is destroyed.  If you are not using the
     * convenience layer then this needs to be called in the onDestroy of the {@link Activity} associated with this ad.
     * @param activity {@link Activity} that was destroyed
     */
    public void activityDestroyed(final Activity activity) {
        throwIfNotOnMainThread();

        mBurstlyView.destroy();
    }

    /**
     * Set a comma delimited list of key value pairs in the format key=value (i.e. key1=value1,key2=value2,...,keyN=valueN)
     * These parameters can be used to setup custom targeting in the Burstly.com UI
     * @param targetingParams String containing comma delimited list of custom targeting key value pairs
     */
    public void setTargetingParameters(final String targetingParams) {
        mBurstlyView.setPubTargetingParams(targetingParams);
    }

    /**
     * Get the comma delimited list of custom targeting key value pairs.
     * @return String containing comma delimited list of custom targeting key value pairs
     */
    public String getTargetingParameters() {
        return "";//mBurstlyView.getPubTargetingParams();
    }

    /**
     * A comma delimited list of key value pairs which are passed as parameters into ads.
     * @param adParameters String containing comma delimited list of key value pairs which are passed as parameters into ads.
     */
    public void setAdParameters(final String adParameters) {
        mBurstlyView.setCrParms(adParameters);
    }

    /**
     * Get the comma delimited list of key value pairs which are being passed as parameters into ads.
     * @return String containing comma delimited list of key value pairs which are passed as parameters into ads
     */
    public String getAdParameters() {
        return "";//mBurstlyView.getCrParms();
    }

    /**
     * Get device id. This is the same waterfall process used in the Burstly SDK.
     *
     * @param context {@link android.content.Context} current context
     * @return {@link String} raw device id
     */
    protected static synchronized String getDeviceId(final Context context) {
        if (sDeviceId == null) {
            boolean invalidDeviceId = false;
            final TelephonyManager tManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            sDeviceId = tManager.getDeviceId();
            Log.d(Burstly.TAG, "TelephonyManager: deviceID - {0}" + sDeviceId);
            invalidDeviceId = !isDeviceIdValid(sDeviceId);
            // Is there no IMEI or MEID?
            // Is this at least Android 2.3+?
            // Then let's get the serial.
            if (invalidDeviceId && Build.VERSION.SDK_INT >= 9) {
                Log.d(Burstly.TAG, "Trying to get serial of 2.3+ device...");
                // THIS CLASS IS ONLY LOADED FOR ANDROID 2.3+
                sDeviceId = Build.SERIAL;
                Log.d(Burstly.TAG, "SERIAL: deviceID - {0}" + sDeviceId);
                invalidDeviceId = !isDeviceIdValid(sDeviceId);
            }
            //In the sdk if there is no deviceID at this point one is created.
        }
        return sDeviceId;
    }

    /**
     * Check device id for validity.
     *
     * @param deviceId {@link String} current device id
     * @return {@code boolean} true if device id is valid
     */
    private static boolean isDeviceIdValid(final String deviceId) {
        // Is the device ID null or empty?
        if (deviceId == null) {
            Log.e(Burstly.TAG, "Device id is null.");
            return false;
        }
        // Is this an emulator device ID?
        final boolean validDeviceId = (deviceId.length() != 0 && !deviceId.equals("000000000000000")
                && !deviceId.equals("0") && !deviceId.equals("unknown"));
        if (!validDeviceId) {
            Log.e(Burstly.TAG, "Device id is empty or an emulator.");
        }
        return validDeviceId;
    }

    /**
     * Show alert in application if device is using a test pub and zone
     *
     * @param context
     */
    protected static synchronized void showTestModeAlert(Context context) {
        if(testModeAlertShown != true){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set dialog content
            alertDialogBuilder
                    .setTitle("Burstly Integration Mode")
                    .setMessage("You will only see sample ads from specified networks on this device. Disable Integration Mode to see live ads.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            testModeAlertShown = true;
        }
    }

    /**
     * Set integration mode for an instance of burstly banner
     *
     * @param adNetwork the ad network specified from the enum {@link BurstlyIntegrationModeAdNetworks}
     * @param testDeviceIds {@link String} device Ids that will run in Integration Mode
     * @param context {@link android.content.Context} current context
     * @param isBanner {@link Boolean} True if burstly view is a banner false if interstitial
     */
    protected void setIntegrationMode(BurstlyIntegrationModeAdNetworks adNetwork, String[] testDeviceIds, Context context, boolean isBanner){
        String deviceID = BurstlyBaseAd.getDeviceId(context);
        Log.d(Burstly.TAG, "Checking device ID against list of test IDs. Device ID: " + deviceID);
        if(Arrays.asList(testDeviceIds).contains(deviceID) && deviceID != null){
            BurstlyBaseAd.showTestModeAlert(context);
            Log.d(Burstly.TAG, "Device is Test Device. Ad " + this.getName() + " will display sample ads from specified ad network.");
            final BurstlyView bv  = getBurstlyView();
            bv.setPublisherId(adNetwork.appId);
            if(isBanner) bv.setZoneId(adNetwork.getBannerZone());
            else bv.setZoneId(adNetwork.getInterstitialZone());
        } else {
            Log.d(Burstly.TAG, "Device is not a test device. Using default pub and zone.");
        }
    }

    protected abstract void setIntegrationMode(BurstlyIntegrationModeAdNetworks adNetwork, String[] testDeviceIds, Context context);

    final public void setIntegrationMode(BurstlyIntegrationModeAdNetworks adNetwork, String testDeviceIds, Context context){
        setIntegrationMode(adNetwork, new String[]{testDeviceIds}, context);
    }
}
