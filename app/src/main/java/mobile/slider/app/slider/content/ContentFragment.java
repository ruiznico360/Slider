package mobile.slider.app.slider.content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ContentFragment extends Fragment {

    public View layoutResourceView;
    public int layoutResource;
    public static ContentFragment newInstance(int layoutResource) {
        ContentFragment f = new ContentFragment();
        f.layoutResource = layoutResource;
        return f;
    }
    public static ContentFragment newInstance(View v) {
        ContentFragment f = new ContentFragment();
        f.layoutResourceView = v;
        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        if (layoutResourceView == null) {
            v = inflater.inflate(layoutResource, container, false);
        }else{
            v = layoutResourceView;
        }
        return v;
    }

    public static class ContentFragmentAdapter extends FragmentPagerAdapter {
        private List<ContentFragment> fragments;

        public ContentFragmentAdapter(FragmentManager fm, List<ContentFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }


        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }
}
