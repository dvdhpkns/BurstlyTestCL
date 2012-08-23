package com.burstly.conveniencelayer;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import com.burstly.conveniencelayer.events.*;

/**
 * The AutomaticCacheManager manages the caching of ads for a BurstlyBaseAd which supports precaching (such as for
 * {@link BurstlyAnimatedBanner} and {@link BurstlyInterstitial}).  The manager will try to have a cached ad ready to play
 * as soon as it is initialized and after every time the ad is shown.
 */
public class AutomaticCacheManager extends BurstlyListenerAdapter implements IActivityListener {
    /**
     * The {@link BurstlyBaseAd} which is being managed
     */
    private final BurstlyBaseAd mAd;

    /**
     *
     */
    private final ICacheable mCacheable;

    /**
     * Tracks whether show has been triggered while an ad is still being precached
     */
    private boolean mShowTriggered;

    /**
     * Constructs a cache manager to manage the cache of the specified ad
     * @param ad The {@link BurstlyBaseAd} whose cache is being managed
     * @param activity Activity associated with this ad
     */
    public AutomaticCacheManager(final BurstlyBaseAd ad, final Activity activity) {
        final Class[] interfaces = ad.getClass().getInterfaces();
        boolean implementsCachable = false;

        for(Class current : interfaces) {
            if(current == ICacheable.class) {
                implementsCachable = true;
                break;
            }
        }

        if(!implementsCachable)
            throw new RuntimeException("");

        mAd = ad;
        mCacheable = (ICacheable)ad;

        Burstly.get().addActivityListener(activity, this);
        ad.addBurstlyListener(this);
    }

    /**
     * Called by the {@link Burstly} convenience layer when the associated activity is resumed
     * @param activity {@link Activity} that was resumed
     */
    public void activityResumed(final Activity activity) {
        mShowTriggered = false;
        
        if(!mCacheable.hasCachedAd())
            mCacheable.cacheAd();
    }

    /**
     * unused callback from conveniencelayer when the associated activity is paused
     * @param activity {@link Activity} that was paused
     */
    public void activityPaused(final Activity activity) {}

    /**
     * unused callback from conveniencelayer when the associated activity is destroyed
     * @param activity {@link Activity} that was destroyed
     */
    public void activityDestroyed(final Activity activity) {}

    /**
     * Does the AutomaticCacheManager have an ad cached
     * @return true if an ad is cached, false otherwise
     */
    public boolean hasCachedAd() {
        return mCacheable.hasCachedAd();
    }

    /**
     * Trigger the cached ad to show. Any {@link BurstlyBaseAd} associated with an AutomaticCacheManager needs to use
     * the showAd method of the manager and not that of the ad.
     */
    public void showAd() {
        mShowTriggered = true;
        
        if(mCacheable.hasCachedAd())
            mAd.showAd();
    }

    /**
     * Called when an ad is removed or, on a refresh, before the ad is changed
     * @param ad The ad which is having a creative hidden
     * @param event hide event data
     */
    @Override
    public void onHide(final BurstlyBaseAd ad, final AdHideEvent event) {
        if(!event.isARefresh()) {
            if(!event.getMatchingShowEvent().isActivityInterstitial()) {
                mCacheable.cacheAd();
            }
        }
    }

    /**
     * Called when an ad is shown, or on a refresh, when the creative changes
     * @param ad The ad which is showing a creative
     * @param event show event data
     */
    @Override
    public void onShow(final BurstlyBaseAd ad, final AdShowEvent event) {
        if(!mShowTriggered)
            throw new RuntimeException("showAd called on BurstlyBaseAd being managed by a AutomaticCacheManager. "
            + "You must call showAd on the AutomaticCacheManager which has been attached to this BurstlyView");
        
        mShowTriggered = false;
    }

    /**
     * Called when an ad fails to load when an attempt to precache or display is made
     * @param ad The ad which failed to display a creative when an attempt to show or precache an ad was made
     * @param event fail event data
     */
    @Override
    public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                if(!mCacheable.hasCachedAd())
                    mCacheable.cacheAd();
            }
        }, mAd.getBurstlyView().getMinTimeUntilNextRequest());
    }

    /**
     * Called when a creative is cached
     * @param ad The ad which cached a creative
     * @param event cache event data
     */
    @Override
    public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event) {
        if(mShowTriggered)
            showAd();
    }
}
