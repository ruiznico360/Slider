package mobile.slider.app.slider.content.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import mobile.slider.app.slider.R;

public class MainUIAdapter extends PagerAdapter {
    public Context mContext;
    public ArrayList<View> pages;

    public MainUIAdapter(Context context, ArrayList<View> pages) {
        this.pages = pages;
        mContext = context;
    }
    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View page = pages.get(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.ui_fragment, collection, false);

        if (page.getParent() != null) {
            ((RelativeLayout)page.getParent()).removeView(page);
        }

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