package com.burstly.cltestapp;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.burstly.conveniencelayer.Burstly;
import com.burstly.conveniencelayer.BurstlyBanner;
import com.burstly.conveniencelayer.DefaultDecorator;

import java.util.ArrayList;


/**
 * Created By: David Hopkins
 * Email: dhopkins@Burstly.com
 * Date: 8/9/12
 */
public class BannerListActivity extends ListActivity {

    private static final String TAG = "BannerListActivity";

    // Enum holding all add data
    public enum Ads {
        HOUSE("0959195979157244033", "House Ad", null),
        MILLENIAL("0952195079157254033", "Millenial", null),
        ADMOB("0655195179157254033", "AdMob", null),
        GREYSTRIPE("0955195179157254033", "Greystripe", null),
        INMOBI("0755195079157254033", "InMobi", null),
        HOUSE2("0959195979157244033", "House Ad", null),
        MILLENIAL2("0952195079157254033", "Millenial", null),
        ADMOB2("0655195179157254033", "AdMob", null),
        GREYSTRIPE2("0955195179157254033", "Greystripe", null),
        INMOBI2("0755195079157254033", "InMobi", null),
        HOUSE3("0959195979157244033", "House Ad", null);/*
        MILLENIAL3("0952195079157254033", "Millenial", null),
        ADMOB3("0655195179157254033", "AdMob", null),
        GREYSTRIPE3("0955195179157254033", "Greystripe", null),
        INMOBI3("0755195079157254033", "InMobi", null),
        HOUSE23("0959195979157244033", "House Ad", null),
        MILLENIAL23("0952195079157254033", "Millenial", null),
        ADMOB23("0655195179157254033", "AdMob", null),
        GREYSTRIPE23("0955195179157254033", "Greystripe", null),
        INMOBI23("0755195079157254033", "InMobi", null); */
        
        private String zone;
        private String adName;
        private LinearLayout bannerLayout;
        // This assumes that all zones are from the same pub
        public final String appId = "Js_mugok3kCBg8ABoJj_Cg";

        Ads(String zone, String adName, LinearLayout bannerLayout) {
            this.zone = zone;
            this.adName = adName;
        }

        public String getZone() {
            return zone;
        }
        public String getAdName() {
            return adName;
        }
        public LinearLayout getBannerLayout() {
            return bannerLayout;
        }
    }

    // Member variables
    private ArrayList<Ads> mAdsArrayList;
    private BannerAdsAdapter mBannerAdsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Burstly.init(this, new DefaultDecorator(this));

        setContentView(R.layout.banner);

        // Add Ads Enum instances to Ads ArrayList
        mAdsArrayList = new ArrayList<Ads>();
        for(Ads ad : Ads.values()){
            if(ad.bannerLayout == null) { //sanity check
                LinearLayout bannerLayout = new LinearLayout(this);
                final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                bannerLayout.setLayoutParams(layoutParams);

                final BurstlyBanner banner = new BurstlyBanner((Activity) this,
                        bannerLayout,
                        layoutParams,
                        ad.appId,
                        ad.getZone(),
                        ad.getAdName(),
                        20);
                banner.showAd();

                ad.bannerLayout = bannerLayout;
            }
            mAdsArrayList.add(ad);
        }
        // Create/set the ListAdapter
        mBannerAdsAdapter = new BannerAdsAdapter(this, R.layout.banner_heading, mAdsArrayList);
        setListAdapter(mBannerAdsAdapter);

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
