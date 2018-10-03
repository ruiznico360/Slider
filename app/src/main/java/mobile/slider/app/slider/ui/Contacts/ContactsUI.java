package mobile.slider.app.slider.ui.Contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
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
import mobile.slider.app.slider.ui.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class ContactsUI extends UIClass {
    public Context c;
    public int titleMargin, aScrollerHeight;
    public SView mainLayout;
    public SView contactScroller, title, alphabetScroller, alphabetScrollerLetter, loading, contactContainer;

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
        alphabetScroller = new SView(new RelativeLayout(c), contactContainer.view);
        alphabetScrollerLetter = new SView(new ImageView(c), contactContainer.view);
        contactScroller = new SView(new UIView.MScrollView(c), contactContainer.view);
        loading = new SView(new ProgressBar(c), mainLayout.view);

        titleMargin = ImageUtil.getRelativeHeight(ImageUtil.getDrawable(R.drawable.contacts_title), wUnit(100)) + hUnit(UserInterface.TITLE_TOP_MARGIN);
        aScrollerHeight = wUnit(50);

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

        contactScroller.plot();
        contactScroller.view.setVerticalScrollBarEnabled(false);
        contactScroller.openRLayout()
                .setWidth(RelativeLayout.LayoutParams.MATCH_PARENT)
                .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT)
                .save();

        alphabetScroller.plot();
        alphabetScroller.view.setVisibility(View.INVISIBLE);
        alphabetScroller.openRLayout()
                .setWidth(wUnit(33))
                .setHeight(aScrollerHeight)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .save();

        SView alphabetScrollerBG = new SView(new ImageView(c),alphabetScroller.view);
        alphabetScrollerBG.plot();
        ImageUtil.setImageDrawable(alphabetScrollerBG.view,R.drawable.scroller_icon);
        alphabetScrollerBG.openRLayout().setWidth(wUnit(25)).setHeight(RelativeLayout.LayoutParams.MATCH_PARENT).addRule(RelativeLayout.ALIGN_PARENT_RIGHT).save();

        alphabetScrollerLetter.plot();
        ImageUtil.setImageDrawable(alphabetScrollerLetter.view,R.drawable.scroller_letter_icon);
        alphabetScrollerLetter.view.setVisibility(View.INVISIBLE);
        alphabetScrollerLetter.openRLayout()
                .setWidth((int)(aScrollerHeight * 1.5))
                .setHeight((int)(aScrollerHeight * 1.5))
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .save();

        loading.plot();
        loading.view.setVisibility(View.INVISIBLE);
        loading.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(wUnit(100))
                .addRule(RelativeLayout.CENTER_VERTICAL)
                .save();



        new ContactScroller().setup();
    }

    public void backPressed() {
        UserInterface.UI.launchNewWindow(UserInterface.UI_WINDOW);
    }
    public void remove() {

    }
    public class ContactScroller {
        public boolean scrollerInUse = false, alphabetScrollerInUse = false, touchEnabled = false;

        public void setup() {
            final SView container = new SView(new RelativeLayout(c), contactScroller.view);
            final ArrayList<Item> items = new ArrayList<>();
            final ArrayList<Item> indicatorItems = new ArrayList<>();
            final Runnable setUpContactList = new Runnable() {
                @Override
                public void run() {
                    Iterator<Contact> contactIterator = Contact.contacts.iterator();

                    int cAdded = 0;
                    int index = 0;
                    Character prevCharacter = null;
                    while (cAdded < Contact.contacts.size()) {
                        char start = Contact.contacts.get(cAdded).displayName.charAt(0);

                        if (Character.isLetter(start)) {
                            if (prevCharacter == null || start != prevCharacter) {
                                Item item = genItem(container);
                                item.appIcon.openRLayout().setHeight(item.appIcon.height() *.6f).setWidth(item.appIcon.width() * .6f).addRule(RelativeLayout.CENTER_VERTICAL).save();
                                item.contactItem = false;
                                items.add(item);
                                indicatorItems.add(item);
                                item.indicatorTag = (start + "").toUpperCase();

                                Bitmap b = Bitmap.createBitmap(item.appIcon.width(), item.appIcon.height(), Bitmap.Config.ARGB_8888);
                                Paint p = new Paint();
                                p.setColor(Color.RED);
                                new Canvas(b).drawRect(0,0,b.getWidth(),b.getHeight(),p);
                                ((ImageView)item.appIcon.view).setImageBitmap(ImageUtil.drawChar(80,50,item.indicatorTag,b));

                                if (index != 0) {
                                    SView.RLayout edit = items.get(index).container.openRLayout();
                                    edit.addRule(RelativeLayout.BELOW, items.get(index - 1).container.view.getId());
                                    edit.setTopM(wUnit(15));
                                    edit.save();
                                }
                                index++;
                            }
                        }else {
                            if (Character.isDigit(start)) {
                                if (prevCharacter == null || !Character.isDigit(prevCharacter)) {
                                    Item item = genItem(container);
                                    item.appIcon.openRLayout().setHeight(item.appIcon.height() *.6f).setWidth(item.appIcon.width() * .6f).addRule(RelativeLayout.CENTER_VERTICAL).save();
                                    item.contactItem = false;
                                    items.add(item);
                                    indicatorItems.add(item);
                                    item.indicatorTag = "#";

                                    Bitmap b = Bitmap.createBitmap(item.appIcon.width(), item.appIcon.height(), Bitmap.Config.ARGB_8888);
                                    Paint p = new Paint();
                                    p.setColor(Color.RED);
                                    new Canvas(b).drawRect(0,0,b.getWidth(),b.getHeight(),p);
                                    ((ImageView)item.appIcon.view).setImageBitmap(ImageUtil.drawChar(80,50,"#",b));

                                    if (index != 0) {
                                        SView.RLayout edit = items.get(index).container.openRLayout();
                                        edit.addRule(RelativeLayout.BELOW, items.get(index - 1).container.view.getId());
                                        edit.setTopM(wUnit(15));
                                        edit.save();
                                    }
                                    index++;
                                }
                            } else {
                                if (prevCharacter == null || Character.isDigit(prevCharacter) || Character.isLetter(prevCharacter)) {
                                    Item item = genItem(container);
                                    item.appIcon.openRLayout().setHeight(item.appIcon.height() *.6f).setWidth(item.appIcon.width() * .6f).addRule(RelativeLayout.CENTER_VERTICAL).save();
                                    item.contactItem = false;
                                    items.add(item);
                                    indicatorItems.add(item);
                                    item.indicatorTag = "&";

                                    Bitmap b = Bitmap.createBitmap(item.appIcon.width(), item.appIcon.height(), Bitmap.Config.ARGB_8888);
                                    Paint p = new Paint();
                                    p.setColor(Color.RED);
                                    new Canvas(b).drawRect(0,0,b.getWidth(),b.getHeight(),p);
                                    ((ImageView)item.appIcon.view).setImageBitmap(ImageUtil.drawChar(80,50,"&",b));

                                    if (index != 0) {
                                        SView.RLayout edit = items.get(index).container.openRLayout();
                                        edit.addRule(RelativeLayout.BELOW, items.get(index - 1).container.view.getId());
                                        edit.setTopM(wUnit(15));
                                        edit.save();
                                    }
                                    index++;
                                }
                            }
                        }

                        items.add(genItem(container));
                        if (index != 0) {
                            SView.RLayout edit = items.get(index).container.openRLayout();
                            edit.addRule(RelativeLayout.BELOW, items.get(index - 1).container.view.getId());
                            edit.setTopM(wUnit(15));
                            edit.save();
                        }

                        prevCharacter = start;
                        index++;
                        cAdded++;
                    }
                    while (contactIterator.hasNext()) {
                        Contact current = contactIterator.next();
                        for (int i = 0; i < items.size(); i++) {
                            if (items.get(i).contactItem) {
                                if (current.lastName != null) {
                                    ((TextView) items.get(i).lastName.view).setText(current.lastName);
                                }else{
                                    items.get(i).lastName.remove();
                                }

                                ((TextView) items.get(i).firstName.view).setText(current.firstName);
                                ((ImageView) items.get(i).appIcon.view).setImageBitmap(current.photo);

                                if (i == items.size() - 1) {
                                    loading.view.setVisibility(View.INVISIBLE);
                                    contactScroller.view.setVisibility(View.VISIBLE);
                                    touchEnabled = true;
                                }else{
                                    current = contactIterator.next();
                                }
                            }
                        }
                    }
                }
            };

            container.plot(wUnit(100), ScrollView.LayoutParams.WRAP_CONTENT);

            if (Contact.loadedContactInfo) {
                setUpContactList.run();
            }else {
                new Runnable() {
                    @Override
                    public void run() {
                        if (UserInterface.running()) {
                            if (Contact.loadedContactInfo && UserInterface.shouldMove()) {
                                setUpContactList.run();
                            } else {
                                loading.view.setVisibility(View.VISIBLE);
                                contactScroller.view.setVisibility(View.INVISIBLE);
                                touchEnabled = false;
                                new Handler().postDelayed(this, 1);
                            }
                        }
                    }
                }.run();
            }

            contactScroller.view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (container.height() <= contactContainer.height()) return true;

                    if (touchEnabled) {
                        if (event.getAction() == MotionEvent.ACTION_MOVE) {
                           scrollerInUse = true;
                            if (alphabetScroller.currentAnim != null) {
                                alphabetScroller.currentAnim.cancel();
                            }
                            alphabetScroller.view.setVisibility(View.VISIBLE);
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            scrollerInUse = false;
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public int prevScrollY = contactScroller.view.getScrollY();

                                @Override
                                public void run() {
                                    if (UserInterface.running() && !scrollerInUse && !alphabetScrollerInUse) {
                                        if (contactScroller.view.getScrollY() == prevScrollY) {
                                            Anim anim = UserInterface.uiAnim(c, alphabetScroller, 100);
                                            anim.delay = 1000;
                                            anim.addAlpha(Anim.FADE_OUT);
                                            anim.setEnd(new Runnable() {
                                                @Override
                                                public void run() {
                                                    alphabetScroller.view.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                            anim.start();
                                        } else {
                                            handler.postDelayed(this, 1);
                                        }
                                        prevScrollY = contactScroller.view.getScrollY();
                                    }
                                }
                            }, 1);
                        }
                    }else{
                        return true;
                    }
                    return false;
                }
            });

            alphabetScroller.view.setOnTouchListener(new View.OnTouchListener() {
                public int yOffset;
                public int currentIndicator = -1;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (container.height() <= contactContainer.height()) return true;

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        yOffset = (int) event.getRawY() - alphabetScroller.y();
                        alphabetScrollerInUse = true;
                        touchEnabled = false;
                        if (alphabetScroller.currentAnim != null) {
                            alphabetScroller.currentAnim.cancel();
                        }
                        ((UIView.MScrollView) contactScroller.view).smoothScrollTo(0, ((UIView.MScrollView) contactScroller.view).prevScrollY);

                        alphabetScroller.view.setVisibility(View.VISIBLE);
                    }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float perc = (event.getRawY() - yOffset - contactContainer.y()) / (float) (contactContainer.height() - ((alphabetScroller.height() * 2)));

                        if (perc < 0) perc = 0;
                        if (perc > 1) perc = 1;

                        alphabetScroller.view.setVisibility(View.INVISIBLE);
                        alphabetScrollerLetter.view.setVisibility(View.VISIBLE);
                        alphabetScroller.openRLayout().setTopM((int) ((contactContainer.height() - (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();

                        ((UIView.MScrollView) contactScroller.view).smoothScrollTo(0, (int)(perc * (container.height() - contactContainer.height())));
                        alphabetScrollerLetter.openRLayout().setTopM(alphabetScroller.openRLayout().topM).save();

                        int newIndicator = checkIndicator();
                        if (newIndicator != currentIndicator) {
                            currentIndicator = newIndicator;
                            Bitmap b = BitmapFactory.decodeResource(c.getResources(), R.drawable.scroller_letter_icon);
                            b = ImageUtil.drawChar(50,40, indicatorItems.get(currentIndicator).indicatorTag, b);

                            ((ImageView)alphabetScrollerLetter.view).setImageBitmap(b);
                        }

                    }else if (event.getAction() == MotionEvent.ACTION_UP) {
                        alphabetScroller.view.setVisibility(View.VISIBLE);
                        alphabetScrollerLetter.view.setVisibility(View.INVISIBLE);
                        touchEnabled = true;
                        alphabetScrollerInUse = false;

                        Anim anim = UserInterface.uiAnim(c, alphabetScroller, 100);
                        anim.delay = 1000;
                        anim.addAlpha(Anim.FADE_OUT);
                        anim.setEnd(new Runnable() {
                            @Override
                            public void run() {
                                alphabetScroller.view.setVisibility(View.INVISIBLE);
                            }
                        });
                        anim.start();
                    }
                    return true;
                }
                public int checkIndicator() {
                    int indicator = currentIndicator;
                    for (int i = indicatorItems.size() - 1; i >= 0; i--) {
                        int yPos = indicatorItems.get(i).appIcon.y() + indicatorItems.get(i).appIcon.height();

                        if (yPos <= contactScroller.height() + contactScroller.y()) {
                            indicator = i;
                            break;
                        }
                    }
                    return contactScroller.view.getScrollY() == 0 ? 0 : indicator;
                }
            });
            ((UIView.MScrollView) contactScroller.view).setScrollEvent(new Runnable() {
                @Override
                public void run() {
                    if (container.height() <= contactContainer.height()) return;

                    float perc = contactScroller.view.getScrollY() / (float) (container.height() - contactScroller.height());
                    alphabetScroller.openRLayout().setTopM((int) ((contactContainer.height() - (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();
                }
            });


            mainLayout.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (oldBottom != bottom) {
                        if (container.height() <= contactContainer.height()) return;

                        float perc = contactScroller.view.getScrollY() / (float)(container.height() - contactScroller.height());
                        alphabetScroller.openRLayout().setTopM((int)((contactContainer.height() -  (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();
                    }
                }
            });
        }

        public int pWidth(float percent) {
            return (int)(contactScroller.width() / 100f * percent);
        }
        private Item genItem(SView parent) {
            Item item;

            SView container = new SView(new RelativeLayout(c), parent.view);
            Util.generateViewId(container.view);
            container.plot();
            container.openRLayout().setWidth(pWidth(80)).setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT).setLeftM(pWidth(10)).save();

            final SView appIcon = new SView(new RoundedImageView(c), container.view);
            ImageUtil.setImageDrawable(appIcon.view, R.drawable.contact_icon);
            appIcon.plot();
            appIcon.openRLayout().setWidth(container.width()).setHeight(container.width()).addRule(RelativeLayout.CENTER_HORIZONTAL).save();

            SView firstName = new SView(new TextView(c), container.view);
            ((TextView) firstName.view).setMaxLines(1);
            ((TextView) firstName.view).setEllipsize(TextUtils.TruncateAt.END);
            ((TextView) firstName.view).setGravity(Gravity.CENTER_HORIZONTAL);
            firstName.plot();
            Util.generateViewId(firstName.view);
            firstName.openRLayout()
                    .setWidth(container.width())
                    .setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT)
                    .setTopM(container.width())
                    .save();

            SView lastName = new SView(new TextView(c), container.view);
            ((TextView) lastName.view).setMaxLines(1);
            ((TextView) lastName.view).setEllipsize(TextUtils.TruncateAt.END);
            ((TextView) lastName.view).setGravity(Gravity.CENTER_HORIZONTAL);
            lastName.plot();
            lastName.openRLayout()
                    .addRule(RelativeLayout.BELOW, firstName.view.getId())
                    .setWidth(container.width())
                    .setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT)
                    .save();

            item = new Item(container, appIcon, firstName, lastName);

            return item;
        }
        public class Item {
            public SView container, appIcon, firstName, lastName;
            public String indicatorTag;
            public boolean contactItem = true;

            public Item(SView container, SView appIcon, SView firstName, SView lastName) {
                this.container = container;
                this.appIcon = appIcon;
                this.firstName = firstName;
                this.lastName = lastName;
            }
        }
    }
}
