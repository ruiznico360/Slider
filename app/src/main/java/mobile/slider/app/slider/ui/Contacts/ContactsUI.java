package mobile.slider.app.slider.ui.Contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.RoundedImageView;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.contact.Contact;
import mobile.slider.app.slider.ui.UIClass;
import mobile.slider.app.slider.model.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class ContactsUI extends UIClass {
    public Context c;
    public int titleMargin;
    public SView mainLayout;
    public SView title, contactContainer;

    public String getID() {
        return UserInterface.CONTACTS_WINDOW;
    }

    public ContactsUI(Context c) {
        this.c = c;
    }

    public void setup() {
        UserInterface.UI.resize(Util.displayWidth() / 4);
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        title = new SView(new ImageView(c), mainLayout.view);
        contactContainer = new SView(new RelativeLayout(c),mainLayout.view);


        titleMargin = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.contacts_title), wUnit(100)) + hUnit(UserInterface.TITLE_TOP_MARGIN);

        title.plot();
        Util.generateViewId(title.view);
        ImageUtil.setImageDrawable(title.view, R.drawable.contacts_title);
        title.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.contacts_title), wUnit(100)))
                .setTopM(hUnit(UserInterface.TITLE_TOP_MARGIN))
                .save();

        contactContainer.plot();
        contactContainer.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                .addRule(RelativeLayout.BELOW, title.view.getId())
                .save();

        new ContactList(this, contactContainer);

    }
    public void backPressed() {
        UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
    }
    public void remove() {

    }
}
