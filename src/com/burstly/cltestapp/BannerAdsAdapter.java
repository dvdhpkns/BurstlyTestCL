package com.burstly.cltestapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.burstly.conveniencelayer.BurstlyBanner;
import com.burstly.conveniencelayer.BurstlyBaseAd;
import com.burstly.conveniencelayer.BurstlyListenerAdapter;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;

import java.util.ArrayList;

/**
 * Created By: David Hopkins
 * Email: dhopkins@Burstly.com
 * Date: 8/16/12
 */
public class BannerAdsAdapter extends ArrayAdapter<BannerListActivity.Ads> {
    private final ArrayList<BannerListActivity.Ads> adsArrayList;
    private final Context context;
    private static final String TAG = "Burstly BannerAdsAdapter";

    public BannerAdsAdapter(Context context, int textViewResourceId, ArrayList<BannerListActivity.Ads> adsArrayList) {
        super(context, textViewResourceId, adsArrayList);
        this.context = context;
        this.adsArrayList = adsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.banner_heading, null);
        }
        BannerListActivity.Ads ad = adsArrayList.get(position);
        String zone = ad.getZone();
        String adName = ad.getAdName();
        String appId = ad.appId;
        TextView bannerAdName;
        TextView status = null;

        if (ad != null) {
            bannerAdName = (TextView) v.findViewById(R.id.bannerAdName);
            status = (TextView) v.findViewById(R.id.adStatus);
            if (bannerAdName != null) {
                bannerAdName.setText(adName);
            }
            if(status != null){
                status.setText("Status: ");
            }
        }

        /*BurstlyBanner banner = ad.getBanner();
        if(banner == null) { */
         /*   BurstlyBanner banner = addBanner(v, zone, adName, appId);
            BurstlyListenerAdapter burstlyListenerAdapter = getBurstlyListener(status);
            banner.addBurstlyListener(burstlyListenerAdapter);
            banner.showAd();  */
      //  }

        final LinearLayout bannerViewGroup = (LinearLayout)v.findViewById(R.id.bannerParent);
        ViewGroup bannerParentOld = (ViewGroup) ad.getBannerLayout().getParent();
        if(bannerParentOld != null){
            bannerParentOld.removeView(ad.getBannerLayout());
        }
        bannerViewGroup.addView(ad.getBannerLayout());

        //TODO add burstlylisteneradapter
        return v;
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

    /**
     * Add a banner to the list view using the convenience layer
     * @param v
     * @param zone
     * @param adName
     * @param appId
     */
    private BurstlyBanner addBanner(View v, String zone, String adName, String appId) {

        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final LinearLayout bannerViewGroup = (LinearLayout)v.findViewById(R.id.bannerParent);

        final BurstlyBanner banner = new BurstlyBanner((Activity) context,
                bannerViewGroup,
                layoutParams,
                appId,
                zone,
                adName,
                30);

        return banner;
    }

}
