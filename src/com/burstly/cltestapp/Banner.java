/**
 * 
 */
package com.burstly.cltestapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.burstly.conveniencelayer.Burstly;

/**
 *
 */
public class Banner extends Fragment {
   /* @Override
    public void onCreate(Bundle savedInstanceState) {

    }  */

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

        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.banner, container, false);
         /*
        Button btn = (Button) layout.findViewById(R.id.btn);
        btn.setText("test");
        */
        return layout;
    }
}
