package com.burstly.conveniencelayer;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdHideEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;
import com.burstly.lib.ui.BurstlyView;

/**
 * AnimatedBanner is used to handle the use case of a banner which is not always visible.  This type of banner can be
 * hidden and shown.  When hideAd and showAd are called they will use the animations provided in setAnims.  If no anims
 * are ever set then the banner will pop on and off when hidden and shown.
 */
public class BurstlyAnimatedBanner extends BurstlyBaseAd implements ICacheable {
    /**
     * The state of the ad
     */
    public enum State {
        Offscreen,
        ShowTriggered,
        IntroAnim,
        OnScreen,
        OutroAnim
    }

    /**
     * Listener for animation events
     */
    public interface IAnimationListener {
        /**
         * Called when the AnimatedBanner which was shown finishes it's intro animation
         * @param banner The {@link BurstlyAnimatedBanner} which was shown
         */
        void onIntroAnimEnd(final BurstlyAnimatedBanner banner);

        /**
         * Called when the AnimatedBanner which was hidden finishes it's outro animation
         * @param banner The {@link BurstlyAnimatedBanner} which was hidden
         */
        void onOutroAnimEnd(final BurstlyAnimatedBanner banner);
    }

    /**
     * AnimationListener used to listen for intro and outro animations to end
     */
    protected class BannerAnimationListener implements Animation.AnimationListener {
        public void onAnimationStart(final Animation animation) {}
        public void onAnimationRepeat(final Animation animation) {}

        /**
         * callback received when animation ends
         * @param animation The {@link Animation} which ended
         */
        public void onAnimationEnd(final Animation animation)  {
            if(animation == mInAnim) {
                if(mState != State.IntroAnim)
                    Log.w(Burstly.TAG, "Intro anim finished but no longer in intro anim state");

                mState = State.OnScreen;

                if(mAnimationCallbacks != null)
                    mAnimationCallbacks.onIntroAnimEnd(BurstlyAnimatedBanner.this);
            }
            else {
                if(mState != State.OutroAnim)
                    Log.w(Burstly.TAG, "Outro anim finished but no longer in outro anim state");

                mState = State.Offscreen;
                getBurstlyView().setVisibility(View.GONE);

                if(mAnimationCallbacks != null)
                    mAnimationCallbacks.onOutroAnimEnd(BurstlyAnimatedBanner.this);

                BurstlyAnimatedBanner.super.onHide(new AdHideEvent(false, mLastShow));
            }
        }
    }

    /**
     * Instance of the AnimationListener which will be set on each anim
     */
    private BannerAnimationListener mAnimationListener = new BannerAnimationListener();

    /**
     * The state of the current animated banner
     */
    private State mState;

    /**
     * The animation used to transition the banner onto the screen
     */
    private Animation mInAnim;

    /**
     * The animation used to transition the banner off of the screen
     */
    private Animation mOutAnim;

    /**
     * The listener that receives animation complete callbacks
     */
    private IAnimationListener mAnimationCallbacks;

    /**
     * The refresh rate set on the banner initially
     */
    private int mRefreshRate;

    /**
     * When an ad is throttled, the minimum time until the next request can be made
     */
    private int mThrottleTime;

    /**
     * Constructor for an AnimatedBanner built from a {@link BurstlyView} that has been attached to a layout file
     * @param activity The {@link Activity} associated with the banner
     * @param id The id for the BurstlyView in the layout
     */
    public BurstlyAnimatedBanner(final Activity activity, int id) {
        super(activity);

        final BurstlyView burstlyView = (BurstlyView)activity.findViewById(id);
        setBurstlyView(burstlyView);
        init();
    }

    /**
     * Constructs an AnimatedBanner in code and attaches it to a ViewGroup
     * @param activity The {@link Activity} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param appId The appId for this application
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     */
    public BurstlyAnimatedBanner(final Activity activity, final ViewGroup group, final ViewGroup.LayoutParams params, final String appId, final String zoneId, final String viewName) {
        super(activity);

        final BurstlyView burstlyView  = new BurstlyView(activity);
        burstlyView.setPublisherId(appId);
        burstlyView.setZoneId(zoneId);
        burstlyView.setBurstlyViewId(viewName);

        setBurstlyView(burstlyView);
        init();

        if(params != null)
            group.addView(burstlyView, params);
        else
            group.addView(burstlyView);
    }

    /**
     * Initialize internal variables
     */
    private void init() {
        mState = State.Offscreen;
        mThrottleTime = -1;
        mRefreshRate = getBurstlyView().getDefaultSessionLife();
    }

    /**
     * Ad caching callback
     * @param event {@link AdCacheEvent} containing info on the successfully cached ad
     */
    @Override
    protected void onCache(final AdCacheEvent event) {
        super.onCache(event);

        if(mState == State.ShowTriggered) {
            super.showAd();
        }
        else if(mState != State.Offscreen) {
            Log.e(Burstly.TAG, "Unexpected state. Banner in state " + mState.toString() + " when precache finished.");
        }
    }

    /**
     * An ad was loaded and will display
     * @param event {@link AdShowEvent} containing info on the successfully shown ad
     */
    @Override
    protected void onShow(final AdShowEvent event) {
        if(event.isActivityInterstitial())
            throw new RuntimeException("Trying tor run an interstitial zone Id using BurstlyBanner");

        if(mState == State.ShowTriggered) {
            super.onShow(event);

            getBurstlyView().setVisibility(View.VISIBLE);

            if(mInAnim != null) {
                mState = State.IntroAnim;
                getBurstlyView().startAnimation(mInAnim);
            }
            else {
                mState = State.OnScreen;

                if(mAnimationCallbacks != null)
                    mAnimationCallbacks.onIntroAnimEnd(this);
            }
        }
        else if(mState == State.OnScreen) { //this is a refresh
            super.onShow(event);
        }
        else {
            Log.w(Burstly.TAG, "Received onLoad callback when banner was in state " + mState.toString());
        }
    }

    /**
     * Override onFail in order to catch failures due to request throttling
     * @param event {@link AdFailEvent} contains data related to the failure including the minimum amount of time
     *                                  until the next request can be made
     */
    @Override
    protected void onFail(final AdFailEvent event) {
        if(event.wasRequestThrottled())
            mThrottleTime = event.getMinTimeUntilNextRequest();
        else
            mThrottleTime = 0;

        super.onFail(event);
    }

    /**
     * Set the listener that will receive callbacks when the intro and outro animations end
     * @param listener The {@link IAnimationListener} that will receive callbacks
     */
    public void setAnimatedBannerListener(final IAnimationListener listener) {
        mAnimationCallbacks = listener;
    }

    /**
     * Called by the convenience layer when the activity associated with this ad is paused.  If you are not using the
     * convenience layer then this needs to be called in the onResume of the Activity associated with this ad.
     * @param activity {@link Activity} that was resumed
     */
    @Override
    public void activityResumed(final Activity activity) {
        if(!isVisible())
            getBurstlyView().resetDefaultSessionLife();

        super.activityResumed(activity);
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

    /**
     * Set the animations used to transition the banner on and off screen.  If null is used the banner will pop on 
     * and off without an animation.
     * @param inAnim {@link Animation} triggered by showAd
     * @param outAnim {@link Animation} triggered by hideAd
     */
    public void setAnims(final Animation inAnim, final Animation outAnim) {
        if(mState == State.IntroAnim || mState == State.OutroAnim)
            throw new RuntimeException("Attempting to change anims while currently animating");

        if(mInAnim != null)
            mInAnim.setAnimationListener(null);

        if(mOutAnim != null)
            mOutAnim.setAnimationListener(null);

        mInAnim = inAnim;
        mOutAnim = outAnim;

        if(mInAnim != null)
            mInAnim.setAnimationListener(mAnimationListener);

        if(mOutAnim != null)
            mOutAnim.setAnimationListener(mAnimationListener);
    }

    /**
     * Shows an ad.  If an ad is already precached it will begin the intro animation immediately.  If an ad has not been
     * precached it will wait for the ad to finish loading and then begin the intro animation.
     */
    @Override
    public void showAd() {
        throwIfNotOnMainThread();

        if(mState.ordinal() >= State.IntroAnim.ordinal()) {
            super.showAd();
        }
        else if(mState == State.ShowTriggered) {
            Log.w(Burstly.TAG, "Trying to show an ad when show has already been called");
        }
        else {
            if(mCachingState != CachingState.Idle) {
                Burstly.logW("calling");
                boolean cached = hasCachedAd();
                Burstly.logW("completed");
                if(cached) {
                    mState = State.ShowTriggered;

                    if(mRefreshRate > 0)
                        getBurstlyView().setDefaultSessionLife(mRefreshRate);

                    super.showAd();
                }
                else if(mCachingState == CachingState.Retrieving || mCachingState == CachingState.CacheRequestThrottled) {
                    mState = State.ShowTriggered;
                    Log.w(Burstly.TAG, "Attempting to show banner before it finished precaching");
                }
                else {
                    Log.e(Burstly.TAG, "Attempting to show precached banner while in idle state");
                }
            }
            else {
                mThrottleTime = 0;

                if(mRefreshRate > 0)
                    getBurstlyView().setDefaultSessionLife(mRefreshRate);

                super.showAd();

                if(mThrottleTime == 0)
                    mState = State.ShowTriggered;
            }
        }
    }

    /**
     * Returns true if the banner is animating onto, displaying on, or animating off of the screen
     * @return whether the ad is visible or not
     */
    public boolean isVisible() {
        return (mState.ordinal() >= State.IntroAnim.ordinal());
    }

    /**
     * Get the state of the animated banner
     * @return The {@link BurstlyAnimatedBanner.State} of the animated banner
     */
    public State getState() {
        return mState;
    }

    /**
     * Hides the animated banner ad
     */
    public void hideAd() {
        throwIfNotOnMainThread();

        if(mState.ordinal() < State.ShowTriggered.ordinal()) {
            Log.w(Burstly.TAG, "Calling hide without calling show.");
        }
        else if(mState == State.ShowTriggered) {
            Log.e(Burstly.TAG, "Hiding an ad immediately after trying to show it before it made it to the screen.  Impression will be tracked but not shown.");
            mState = State.Offscreen;
        }
        else if(mState == State.OutroAnim) {
            Log.w(Burstly.TAG, "Calling hide multiple times");
        }
        else {
            if(mState == State.IntroAnim)
                Log.w(Burstly.TAG, "Calling hide before intro transition was finished");

            getBurstlyView().resetDefaultSessionLife();

            if(mOutAnim == null) {
                mState = State.Offscreen;
                getBurstlyView().setVisibility(View.GONE);
                mAnimationCallbacks.onOutroAnimEnd(this);

                super.onHide(new AdHideEvent(false, mLastShow));
            }
            else {
                mState = State.OutroAnim;
                getBurstlyView().startAnimation(mOutAnim);
            }
        }
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
