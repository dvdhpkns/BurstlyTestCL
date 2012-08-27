/**
 * 
 */
package com.burstly.cltestapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.burstly.conveniencelayer.BurstlyBaseAd;
import com.burstly.conveniencelayer.BurstlyInterstitial;
import com.burstly.conveniencelayer.BurstlyListenerAdapter;
import com.burstly.conveniencelayer.events.AdFailEvent;

/**
 * @author mwho
 *
 */
public class Interstitial extends Fragment {
    private static final String RETRIEVING = "Retrieving Ad";
    private static final String SHOW = "Show Ad";
    private static final String THROTTLED = "Throttled. Retry in ";
    private static final String FAILED = "Request Failed";

    private BurstlyInterstitial mInterstitial;
    private Button mButton;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
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
        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.interstitial, container, false);

        mInterstitial = new BurstlyInterstitial(this.getActivity(),
                "TeW3mgnGxkSQXNERLognRQ",
                "0159914779078214412",
                "interstitial");

        mInterstitial.addBurstlyListener(mListener);

        mButton = (Button)layout.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButton.setText(RETRIEVING);
                mButton.setEnabled(false);

                mInterstitial.showAd();
            }
        });
		return layout;
	}

    @Override
    public void onResume() {
        super.onResume();

        mButton.setText(SHOW);
        mButton.setEnabled(true);
    }

    private BurstlyListenerAdapter mListener = new BurstlyListenerAdapter() {
        public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {
            if(event.wasRequestThrottled())
                mButton.setText(THROTTLED + event.getMinTimeUntilNextRequest() + " ms");
            else
                mButton.setText(FAILED);

            mButton.setEnabled(true);
        }
    };

}
