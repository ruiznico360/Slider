package mobile.slider.app.slider.content.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.util.Util;

public class MainUIListAdapter extends ArrayAdapter<View> {
    public int lastPosition = -1;
    public ArrayList<View> dataSet;
    public Context c;
    public int size;

    public MainUIListAdapter(ArrayList<View> data, Context context, int size) {
        super(context, R.layout.ui_fragment, data);
        this.dataSet = data;
        this.c = context;
        this.size = size;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        View data = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final View result;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.ui_fragment, parent, false);

            ViewGroup.LayoutParams params = convertView.getLayoutParams();
            params.height = size;
            convertView.setLayoutParams(params);

            convertView.setBackgroundColor(Color.BLACK);
            result = convertView;
        } else {
            result = convertView;
        }
        // Return the completed view to render on screen
        return result;
    }
}
