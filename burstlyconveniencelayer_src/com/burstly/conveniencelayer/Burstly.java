package com.burstly.conveniencelayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.burstly.lib.BurstlySdk;
import com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity;
import com.burstly.lib.util.LoggerExt;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Burstly is a singleton which takes care of initializing and shutting down the SDK as well as keeping a list
 * of BurstlyViews associated with each activity so that they can be notified of activity pause and resume events.
 */
public class Burstly {
    /**
     * Log tag
     */
    public static final String TAG = "Burstly Convenience Layer";

    /**
     * Static singleton instance
     */
    protected static Burstly sInstance = null;

    /**
     * Gets the Burstly singleton
     * @return reference to static instance of the Burstly singleton
     */
    public static Burstly get() {
        if(sInstance == null) {
            throw new RuntimeException( "Burstly Convenience Layer never initialized.  " +
                "Burstly.init should be called from your default activity's onCreate method");
        }

        return sInstance;
    }

    /**
     * Initialize the BurstlySdk and the Burstly convenience layer
     * 
     * @param context Application {@link Context} or default {@link Activity} used to initialize the BurstlySdk
     * @param decorator The {@link com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity.IDecorator} that will be used to decorate your image interstitials
     * @return reference to the static instance
     */
    public static synchronized Burstly init(final Context context, final BurstlyFullscreenActivity.IDecorator decorator) {
        if(sInstance == null) {
            final Burstly burstly = new Burstly();
            burstly.initBurstly(context, decorator);
    
            return burstly;
        }
        else {
            logW("Burstly already initialized");
            return sInstance;
        }
    }

    /**
     * Static deinitializer cleans up the SDK and convenience layer
     */
    public static void deinit() {
        sInstance.deinitBurstly();
        sInstance = null;
    }

    /**
     * Context used to initialize the SDK and convenience layer
     */
    private Context mContext;

    /**
     * Map of all ads associated with an Activity
     */
    private final HashMap<Activity, ArrayList<IActivityListener>> mActivityListeners = new HashMap<Activity, ArrayList<IActivityListener>>();

    /**
     * Map of BurstlyView names to the pubs and zones they are associated with to make sure a BurstlyView name isn't used
     * with 2 different pub/zone combinations
     */
    private final HashMap<String, String> mViewMap = new HashMap<String, String>();

    /**
     * Flag keeping track of whether logging is enabled or disabled
     */
    private boolean mLoggingEnabled;

    /**
     * protected constructor assures only a single instance
     */
    protected Burstly() {
        if(sInstance != null) {
            throw new RuntimeException("Attempting to create multiple instances of singleton Burstly");
        }

        sInstance = this;
    }

    /**
     * Initializer makes calls to initialize the BurstlySdk, and initializes data structures for the convenience
     * layer to manage the BurstlyView's associated with each page
     * @param context {@link Context} or default {@link Activity} used to initialize the Burstly SDK
     * @param decorator {@link com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity.IDecorator} used to initialize the SDK
     */
    protected void initBurstly(final Context context, final BurstlyFullscreenActivity.IDecorator decorator) {
        mContext = context;
        mLoggingEnabled = true;
        BurstlySdk.init(context);

        mActivityListeners.clear();

        if(decorator != null)
            BurstlyFullscreenActivity.addDecorator("burstlyImage", decorator);
        else
            logW("No decorator spcified. Interstitials will not have a close button.  Pass an instance of com.burstly.convenience.DefaultDecorator into Convenience.init to add the default close button.");
    }

    /**
     * Shuts down the burstly SDK and clears all remaining data.  Logs when deinitializing before destroying everything
     */
    protected void deinitBurstly() {
        BurstlyFullscreenActivity.removeDecorator("burstlyImage");

        if(mActivityListeners.size() != 0) {
            logE("Deinitializing Burstly conveniencelayer system before everything is destroyed");
        }
        
        BurstlySdk.shutdown(mContext);

        mContext = null;
    }

    /**
     * Passes activity onPause events on to necessary BurstlyView objects
     * @param activity The {@link Activity} being paused
     */
    public void onPauseActivity(final Activity activity) {
        if(mActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = mActivityListeners.get(activity);

            for(IActivityListener listener : listeners) {
                listener.activityPaused(activity);
            }
        }
    }

    /**
     * Passes activity onResume events on to the necessary BurstlyView objects
     * @param activity The {@link Activity} being resumed
     */
    public void onResumeActivity(final Activity activity) {
        if(mActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = mActivityListeners.get(activity);

            for(IActivityListener listener : listeners) {
                listener.activityResumed(activity);
            }
        }
    }

    /**
     * Passes activity onDestroy events on to necessary BurstlyView objects
     * @param activity The {@link Activity} being destroyed
     */
    public void onDestroyActivity(final Activity activity) {
        if(mActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = mActivityListeners.get(activity);

            for(int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).activityDestroyed(activity);
                listeners.remove(i);
            }

            mActivityListeners.remove(activity);
        }
    }

    /**
     * Add an {@link IActivityListener} to receive callbacks when the activity is paused, resumed, or destroyed
     * @param activity The {@link Activity} associated with the ad
     * @param activityListener The new {@link IActivityListener} being added
     */
    public void addActivityListener(final Activity activity, final IActivityListener activityListener) {
        if(activityListener instanceof BurstlyBaseAd) {       
            final BurstlyBaseAd burstlyAd = (BurstlyBaseAd)activityListener;
            final String name = burstlyAd.getName();
            final String appZone = burstlyAd.getAppId() + ":" + burstlyAd.getZoneId();
    
            if(mViewMap.containsKey(name)) {
                if(!mViewMap.get(name).equals(appZone)) {
                    throw new RuntimeException("Attempting to reuse the view Id " + name + " with a different app/zone combination.  Use a new view Id for each pub and zone");
                }
            }
            else {
                mViewMap.put(name, appZone);
            }
        }

        if(mActivityListeners.containsKey(activity)) {
            mActivityListeners.get(activity).add(activityListener);
        }
        else {
            final ArrayList<IActivityListener> listeners = new ArrayList<IActivityListener>();
            listeners.add(activityListener);

            mActivityListeners.put(activity, listeners);
        }
    }

    /**
     * Removes an {@link IActivityListener} from the list of listeners receiving pause, resume, and destroy
     * callbacks for the specified activity
     * @param activity {@link Activity} having the listener removed
     * @param activityListener {@link IActivityListener} being removed from the list of listeners
     */
    public void removeActivityListener(final Activity activity, final IActivityListener activityListener) {
        if(mActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = mActivityListeners.get(activity);

            if(listeners.contains(activityListener))
                listeners.remove(activityListener);
        }
    }

    /**
     * Removes {@link IActivityListener} from all lists of listeners receiving pause, resumee, and destroyed callbacks
     * @param activityListener {@link IActivityListener} being removed
     */
    public void removeActivityListener(final IActivityListener activityListener) {
        final Object[] listenerLists = mActivityListeners.values().toArray();
        
        for(Object listenersObj : listenerLists) {
            ArrayList<IActivityListener> listeners = (ArrayList<IActivityListener>)listenersObj;
            
            if(listeners.contains(activityListener))
                listeners.remove(activityListener);
        }
    }

    /**
     * Enables and disables logging.
     * @param enabled true if enabling, false if disabling
     */
    public void setLoggingEnabled(boolean enabled) {
        if(enabled)
            LoggerExt.setLogLevel(LoggerExt.DEBUG_LEVEL);
        else
            LoggerExt.setLogLevel(LoggerExt.NONE_LEVEL);

        mLoggingEnabled = enabled;
    }

    /**
     * Gets the state of logging
     * @return true if enabled, false if disabled
     */
    public boolean isLoggingEnabled() {
        return mLoggingEnabled;
    }

    /**
     * Print a debug level line to the log
     * @param s string to print to log
     */
    static void logD(final String s) {
        if(get().isLoggingEnabled())
            Log.d(TAG, s);
    }

    /**
     * Print a debug level line to the log
     * @param s string to print to log
     */
    static void logI(final String s) {
        if(get().isLoggingEnabled())
            Log.i(TAG, s);
    }

    /**
     * Print a debug level line to the log
     * @param s string to print to log
     */
    static void logW(final String s) {
        if(get().isLoggingEnabled())
            Log.w(TAG, s);
    }

    /**
     * Print a error level line to the log
     * @param s string to print to log
     */
    static void logE(final String s) {
        //always print error level log entries to the log
        Log.e(TAG, s);
    }

}
