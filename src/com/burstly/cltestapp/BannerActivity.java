package com.burstly.cltestapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.burstly.conveniencelayer.*;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;


/**
 * Created By: David Hopkins
 * Email: dhopkins@Burstly.com
 * Date: 8/9/12
 */
public class BannerActivity extends Activity {

    private static final String TAG = "BannerActivity";

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Burstly.init(this, new DefaultDecorator(this));

        setContentView(R.layout.banner);

        //get reference to the linear layout inside the scroll view
        LinearLayout parentLayout = (LinearLayout) findViewById(R.id.bannerParentLayout);

        for(Ads ad : Ads.values()){
            String zone = ad.getZone();
            String adName = ad.getAdName();

            //inflate view from layout file
            View bannerView;
            LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            bannerView = vi.inflate(R.layout.banner_inflater, null);

            //set adName in TextView
            TextView bannerAdName = (TextView)bannerView.findViewById(R.id.bannerAdName);
            bannerAdName.setText(adName);

            TextView status = (TextView)bannerView.findViewById(R.id.adStatus);
            status.setText("Loading Banner...");

            //Add BurstlyBanner to layout
            BurstlyBanner banner = new BurstlyBanner(this,
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

    }

    private BurstlyListenerAdapter getBurstlyListener(final TextView status) {
        BurstlyListenerAdapter listener = new BurstlyListenerAdapter() {
            @Override
            public void onShow(final BurstlyBaseAd ad, final AdShowEvent event) {
                status.setText("Add Shown");
                Log.d(TAG, "Ad has been shown: " + ad.getName());
            }

            @Override
            public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event) {
                status.setText("Add Cached");
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

    @Override
    public void onResume() {
        Burstly.get().onResumeActivity(this);

        super.onResume();

    }

    @Override
    public void onPause() {
        Burstly.get().onPauseActivity(this);

        super.onPause();

    }

    @Override
    public void onDestroy() {
        Burstly.get().onDestroyActivity(this);

        super.onDestroy();

    }

}
