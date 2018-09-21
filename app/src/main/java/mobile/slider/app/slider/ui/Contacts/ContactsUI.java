package mobile.slider.app.slider.ui.Contacts;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.RoundedImageView;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.ui.MainUI;
import mobile.slider.app.slider.ui.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class ContactsUI {
    public Context c;
    public SView mainLayout;

    public SView contactContainer, title, alphabetScroller;

    public int wUnit(int percent) {
        return (int)(UserInterface.UI.container.width() / 100f * percent);
    }
    public int hUnit(int percent) {
        return (int)(UserInterface.UI.container.height() / 100f * percent);
    }

    public ContactsUI(Context c) {
        this.c = c;
        mainLayout = UserInterface.UI.inner;
    }

    public void setup() {
        title = new SView(new ImageView(c), mainLayout.view);
        alphabetScroller = new SView(new ImageView(c), mainLayout.view);
        contactContainer = new SView(new UIView.MScrollView(c), mainLayout.view);
        int topM = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.contacts_title), wUnit(100) + hUnit(UserInterface.TITLE_TOP_MARGIN));

        title.plot();
        Util.generateViewId(title.view);
        ImageUtil.setImageDrawable(title.view, R.drawable.contacts_title);
        title.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.contacts_title), wUnit(100)))
                .setTopM(hUnit(UserInterface.TITLE_TOP_MARGIN))
                .save();

        alphabetScroller.plot();
        Util.generateViewId(alphabetScroller.view);
        alphabetScroller.openRLayout()
                .setWidth(ImageUtil.getRelativeWidth(ImageUtil.getDrawable(R.drawable.alphabet_scroller), hUnit(100) - topM))
                .setHeight(hUnit(100) - topM)
                .addRule(RelativeLayout.BELOW, title.view.getId())
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .save();
        ImageUtil.setBackground(alphabetScroller.view, R.drawable.alphabet_scroller);

        contactContainer.plot();
        contactContainer.view.setVerticalScrollBarEnabled(false);
        contactContainer.openRLayout()
                .setWidth(mainLayout.width() - alphabetScroller.width())
                .setHeight(alphabetScroller.height())
                .addRule(RelativeLayout.BELOW, title.view.getId())
                .addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                .save();
        new ContactScroller().setup();
    }

    public class ContactScroller {
        //            item.appIcon.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ArrayList<Contact> cList = new ArrayList<Contact>();
//                    Contact.retrieveContacts(cList);
//                    int sel = new Random().nextInt(cList.size());
//                    ((ImageView)item.appIcon.view).setImageBitmap(cList.get(sel).getPhoto());
//                }
//            });

        public void setup() {
            final SView container = new SView(new RelativeLayout(c), contactContainer.view);
            container.plot(wUnit(100), ScrollView.LayoutParams.WRAP_CONTENT);

            Item[] items = new Item[]{genItem(container),genItem(container),genItem(container),genItem(container),genItem(container),genItem(container)};
            for  (int i = 0; i < items.length; i++) {
                SView.RLayout edit = items[i].container.openRLayout();

                if (i != 0) {
                    edit.addRule(RelativeLayout.BELOW, items[i-1].container.view.getId());
                    edit.setTopM(wUnit(15));
                }
                edit.save();
            }
        }
        public int pWidth(float percent) {
            return (int)(contactContainer.width() / 100f * percent);
        }
        private Item genItem(SView parent) {
            Item item;

            SView container = new SView(new RelativeLayout(c), parent.view);
            Util.generateViewId(container.view);
            container.plot();
            container.openRLayout().setWidth(pWidth(85)).setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT).setLeftM(pWidth(7)).save();

            SView appIcon = new SView(new RoundedImageView(c), container.view);
            ImageUtil.setImageDrawable(appIcon.view, R.drawable.contact_icon);
            appIcon.plot(container.width(), container.width());

            SView name = new SView(new TextView(c), container.view);
            ((TextView)name.view).setGravity(TextView.TEXT_ALIGNMENT_TEXT_START);
            ((TextView)name.view).setText("Hello World ");
            name.plot();
            name.openRLayout()
                    .setWidth(container.width())
                    .setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT)
                    .setTopM(container.width())
                    .save();

            item = new Item(container, appIcon, name);
            return item;
        }
        public class Item {
            public SView container, appIcon, name;

            public Item(SView container, SView appIcon, SView name) {
                this.container = container;
                this.appIcon = appIcon;
                this.name = name;
            }
        }
    }
}
