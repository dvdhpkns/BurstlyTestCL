/**
 * 
 */
package com.burstly.cltestapp;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.burstly.conveniencelayer.Burstly;
import com.burstly.conveniencelayer.BurstlyBanner;
import com.burstly.conveniencelayer.BurstlyBaseAd;
import com.burstly.conveniencelayer.BurstlyListenerAdapter;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;

/**
 *
 */
public class BannerFragment extends Fragment {
    private static final String TAG = "BannerFragment";
    private static final int SWIPE_MIN_DISTANCE = 40;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int REFRESH_TIME = 20;

    @Override
    public void onResume() {
        Burstly.get().onResumeActivity(this.getActivity());

        super.onResume();

    }

    @Override
    public void onPause() {
        Burstly.get().onPauseActivity(this.getActivity());

        super.onPause();

    }

    @Override
    public void onDestroy() {
        Burstly.get().onDestroyActivity(this.getActivity());

        super.onDestroy();

    }

    /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
      */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.banner, container, false);
        LinearLayout parentLayout = (LinearLayout) layout.findViewById(R.id.bannerParentLayout);


        /*
        final TextView textView = (TextView) layout.findViewById(R.id.textView);

        final GestureDetector myGestDetector = new GestureDetector(this.getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "e1: " + e1.getX() + " e2: " + e2.getX());
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    textView.setText("Right to left swipe");
                    Log.d(TAG, " right to left");
                    return false; // Right to left
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    textView.setText("Left to right swipe");
                    Log.d(TAG, "left to right");
                    return false; // Left to right
                }

                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    return false; // Bottom to top
                }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    return false; // Top to bottom
                }
                return false;
            }
        });

        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                myGestDetector.onTouchEvent(event);
                return true;
            }
        }); */

        for(AdNetworks ad : AdNetworks.values()){
            String zone = ad.getBannerZone();
            String adName = ad.getAdName();

            //inflate view from layout file
            View bannerView;
            LayoutInflater vi = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            bannerView = vi.inflate(R.layout.banner_inflater, null);


            //set adName in TextView
            TextView bannerAdName = (TextView)bannerView.findViewById(R.id.bannerAdName);
            bannerAdName.setText(adName);

            final TextView status = (TextView)bannerView.findViewById(R.id.adStatus);
            status.setText("Loading Banner...");

            LinearLayout progressBar = (LinearLayout)bannerView.findViewById(R.id.progressBar);
            ProgressBar pB = new ProgressBar(REFRESH_TIME*1000, 10, progressBar);

            LinearLayout bannerParent = (LinearLayout)bannerView.findViewById(R.id.bannerParent);

            //Add BurstlyBanner to layout
            final BurstlyBanner banner = new BurstlyBanner(this.getActivity(),
                    bannerParent,
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT),
                    ad.appId,
                    zone,
                    adName,
                    REFRESH_TIME);
            banner.addBurstlyListener(getBurstlyListener(status, pB));
            banner.showAd();



            ImageView refreshButton = (ImageView)bannerView.findViewById(R.id.refreshButton);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    banner.showAd();
                }
            });

            parentLayout.addView(bannerView);
        }
        return layout;
    }

    private BurstlyListenerAdapter getBurstlyListener(final TextView status, final ProgressBar pB) {
        BurstlyListenerAdapter listener = new BurstlyListenerAdapter() {
            @Override
            public void onShow(final BurstlyBaseAd ad, final AdShowEvent event) {
                status.setText("Ad Shown");
                pB.start();
                Log.d(TAG, "Ad has been shown: " + ad.getName());
            }

            @Override
            public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event) {
                //should not be seen
                status.setText("Ad Cached");
            }

            @Override
            public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {

                if(event.wasRequestThrottled())
                    status.setText("Throttled");
                else
                    status.setText("Failed");
                    Log.d(TAG, "onFail: " + event.toString());
            }
        };

        return listener;
    }

    class ProgressBar extends CountDownTimer {
        private LinearLayout progressBar;
        private View timeDown;
        private View timeRemaining;
        private long millisInFuture;

        public ProgressBar(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public ProgressBar(long millisInFuture, long countDownInterval, LinearLayout progressBar) {
            super(millisInFuture, countDownInterval);
            this.millisInFuture= millisInFuture;
            this.progressBar = progressBar;
            timeDown = (View)progressBar.findViewById(R.id.timeDown);
            timeRemaining = (View)progressBar.findViewById(R.id.timeRemaining);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            timeDown.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (millisInFuture-millisUntilFinished)));
            timeRemaining.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (millisUntilFinished)));
        }

        @Override
        public void onFinish() {
            timeDown.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            timeRemaining.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }
    }
}
