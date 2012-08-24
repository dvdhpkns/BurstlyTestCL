/**
 * 
 */
package com.burstly.cltestapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class Banner extends Fragment {
    private static final String TAG = "Banner";

    // Enum holding all add data
    public enum Ads {
        HOUSE("0959195979157244033", "House Ad"),
        MILLENIAL("0952195079157254033", "Millenial"),
        ADMOB("0655195179157254033", "AdMob"),
        GREYSTRIPE("0955195179157254033", "Greystripe"),
        INMOBI("0755195079157254033", "InMobi");

        private String zone;
        private String adName;
        // This assumes that all zones are from the same pub
        public final String appId = "Js_mugok3kCBg8ABoJj_Cg";

        Ads(String zone, String adName) {
            this.zone = zone;
            this.adName = adName;
        }

        public String getZone() {
            return zone;
        }
        public String getAdName() {
            return adName;
        }
    }

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
        Button btn = (Button) layout.findViewById(R.id.btn);
        btn.setText("test");

        BurstlyBanner banner = new BurstlyBanner(this.getActivity(),
                parentLayout,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT),
                BannerActivity.Ads.HOUSE.appId,
                BannerActivity.Ads.HOUSE.getZone(),
                BannerActivity.Ads.HOUSE.getAdName(),
                20);
        banner.showAd();
        */
        for(Ads ad : Ads.values()){
            String zone = ad.getZone();
            String adName = ad.getAdName();

            //inflate view from layout file
            View bannerView;
            LayoutInflater vi = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            bannerView = vi.inflate(R.layout.banner_inflater, null);

            //set adName in TextView
            TextView bannerAdName = (TextView)bannerView.findViewById(R.id.bannerAdName);
            bannerAdName.setText(adName);

            TextView status = (TextView)bannerView.findViewById(R.id.adStatus);
            status.setText("Loading Banner...");

            //Add BurstlyBanner to layout
            BurstlyBanner banner = new BurstlyBanner(this.getActivity(),
                    (LinearLayout)bannerView.findViewById(R.id.bannerParent),
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT),
                    ad.appId,
                    zone,
                    adName,
                    20);
            banner.addBurstlyListener(getBurstlyListener(status));
            banner.showAd();

            parentLayout.addView(bannerView);
        }
        return layout;
    }

    private BurstlyListenerAdapter getBurstlyListener(final TextView status) {
        BurstlyListenerAdapter listener = new BurstlyListenerAdapter() {
            @Override
            public void onShow(final BurstlyBaseAd ad, final AdShowEvent event) {
                status.setText("Ad Shown");
                Log.d(TAG, "Ad has been shown: " + ad.getName());
            }

            @Override
            public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event) {
                status.setText("Ad Cached");
            }

            @Override
            public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {

                if(event.wasRequestThrottled())
                    status.setText("Throttled");
                else
                    status.setText("onFail: " + event.toString());
            }
        };

        return listener;
    }
}
