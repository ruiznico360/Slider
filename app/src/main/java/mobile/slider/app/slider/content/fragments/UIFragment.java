package mobile.slider.app.slider.content.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Random;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.util.Util;

public class UIFragment extends Fragment {
    public View view;

    public static UIFragment newInstance(View v) {
        UIFragment f = new UIFragment();
        f.view = v;
        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            Util.log("null view in UIFragment");
        }
        return view;
    }
    public static class Adapter extends PagerAdapter {
        public Context mContext;
        public ArrayList<View> pages;

        public Adapter(Context context, ArrayList<View> pages) {
            this.pages = pages;
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View page = pages.get(position);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.ui_fragment, collection, false);

            RelativeLayout uiFragment = layout.findViewById(R.id.ui_fragment);
            uiFragment.addView(page);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) page.getLayoutParams();
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            uiFragment.updateViewLayout(page, params);

            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
    public enum ModelObject {

        RED(R.layout.ui_fragment),
        BLUE(R.layout.ui_fragment),
        GREEN( R.layout.ui_fragment);

        private int mLayoutResId;

        ModelObject(int layoutResId) {
            mLayoutResId = layoutResId;
        }
        public int getLayoutResId() {
            return mLayoutResId;
        }
    }
}