package mobile.slider.app.slider.model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Nicolas on 2016-04-16.
 */
public class SearchBar extends EditText {

    public SearchBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public SearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public SearchBar(Context context) {
        super(context);

    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dispatchKeyEvent(event);
            this.clearFocus();
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

}
