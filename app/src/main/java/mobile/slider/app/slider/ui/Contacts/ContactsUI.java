package mobile.slider.app.slider.ui.Contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.RoundedImageView;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.contact.Contact;
import mobile.slider.app.slider.ui.MainUI;
import mobile.slider.app.slider.ui.UIView;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class ContactsUI {
    public Context c;
    public int titleMargin, aScrollerHeight;
    public SView mainLayout;

    public SView contactContainer, title, alphabetScroller, alphabetScrollerContainer, loading;

    public int wUnit(int percent) {
        return (int)(UserInterface.UI.container.width() / 100f * percent);
    }
    public int hUnit(int percent) {
        return (int)(UserInterface.UI.container.height() / 100f * percent);
    }

    public ContactsUI(Context c) {
        this.c = c;
    }

    public void setup() {
        mainLayout = new SView(new RelativeLayout(c), UserInterface.UI.inner.view);
        mainLayout.plot(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        title = new SView(new ImageView(c), mainLayout.view);
        alphabetScrollerContainer = new SView(new RelativeLayout(c), mainLayout.view);
        alphabetScroller = new SView(new RelativeLayout(c), alphabetScrollerContainer.view);
        contactContainer = new SView(new UIView.MScrollView(c), mainLayout.view);
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
        contactContainer.view.setVerticalScrollBarEnabled(false);
        contactContainer.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                .addRule(RelativeLayout.BELOW, title.view.getId())
                .save();

        alphabetScrollerContainer.plot();
        alphabetScrollerContainer.view.setVisibility(View.INVISIBLE);
        alphabetScrollerContainer.openRLayout()
                .setWidth(wUnit(33))
                .setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .addRule(RelativeLayout.BELOW, title.view.getId())
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                .setBottomM(aScrollerHeight)
                .save();

        alphabetScroller.plot();
        alphabetScroller.openRLayout()
                .setWidth(RelativeLayout.LayoutParams.MATCH_PARENT)
                .setHeight(aScrollerHeight)
                .save();

        loading.plot();
        loading.view.setVisibility(View.INVISIBLE);
        loading.openRLayout()
                .setWidth(wUnit(100))
                .setHeight(wUnit(100))
                .addRule(RelativeLayout.CENTER_VERTICAL)
                .save();

        SView alphabetScrollerContainerBG = new SView(new RelativeLayout(c),alphabetScrollerContainer.view);
        alphabetScrollerContainerBG.plot();
        alphabetScrollerContainerBG.view.setBackgroundColor(Color.argb(90,0,0,255));
        alphabetScrollerContainerBG.openRLayout().setWidth(wUnit(25)).setHeight(RelativeLayout.LayoutParams.MATCH_PARENT).addRule(RelativeLayout.ALIGN_PARENT_RIGHT).save();

        SView alphabetScrollerBG = new SView(new RelativeLayout(c),alphabetScroller.view);
        alphabetScrollerBG.plot();
        alphabetScrollerBG.view.setBackgroundColor(Color.rgb(0,0,255));
        alphabetScrollerBG.openRLayout().setWidth(wUnit(25)).setHeight(RelativeLayout.LayoutParams.MATCH_PARENT).addRule(RelativeLayout.ALIGN_PARENT_RIGHT).save();
        new ContactScroller().setup();
    }

    public class ContactScroller {
        public int loadedItems;
        public boolean scrollerInUse = false, alphabetScrollerInUse = false, touchEnabled = false;

        public void setup() {
            final SView container = new SView(new RelativeLayout(c), contactContainer.view);
            final ArrayList<Item> items = new ArrayList<>();
            final Runnable setUpContactList = new Runnable() {
                @Override
                public void run() {
                    Iterator<Contact> contactIterator = Contact.contacts.iterator();

                    int cAdded = 0;
                    int index = 0;
                    Character prevCharacter = null;
                    while (cAdded < Contact.contacts.size()) {
                        char start = Contact.contacts.get(cAdded).name.charAt(0);

                        if (Character.isLetter(start)) {
                            if (prevCharacter == null || start != prevCharacter) {
                                Item item = genItem(container);
                                item.contactItem = false;
                                ((TextView)item.lastName.view).setText(start + "");
                                items.add(item);

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
                                    item.contactItem = false;
                                    ((TextView) item.lastName.view).setText("#");
                                    items.add(item);

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
                                    item.contactItem = false;
                                    ((TextView) item.lastName.view).setText("&");
                                    items.add(item);

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
                                String firstName = "", lastName = "";

                                String name = current.name;
                                String[] arr = name.split(" ");

                                if (arr.length < 2) {
                                    firstName = arr[0];
                                } else {
                                    firstName = arr[0];
                                    lastName = arr[arr.length - 1];
                                }

                                ((TextView) items.get(i).firstName.view).setText(firstName);
                                ((TextView) items.get(i).lastName.view).setText(lastName);
                                ((ImageView) items.get(i).appIcon.view).setImageBitmap(current.photo);

                                if (i == items.size() - 1) {
                                    loading.view.setVisibility(View.INVISIBLE);
                                    contactContainer.view.setVisibility(View.VISIBLE);
                                    touchEnabled = true;
                                }else{
                                    current = contactIterator.next();
                                }
                            }
//                        final int num = i;
//                        items.get(i).appIcon.view.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                String numbers = "";
//                                for (String s : Contact.contacts.get(num).numbers) {
//                                    numbers += "\n     " + s;
//                                }
//                                Util.log(numbers.length() == 0 ? "No numbers attached to " + Contact.contacts.get(num).name + " " + Contact.contacts.get(num).id : Contact.contacts.get(num).name + " " + Contact.contacts.get(num).id + "\'s numbers:" + numbers);
//                            }
//                        });
                        }
                    }
                }
            };
            loadedItems = 0;

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
                                contactContainer.view.setVisibility(View.INVISIBLE);
                                touchEnabled = false;
                                new Handler().postDelayed(this, 1);
                            }
                        }
                    }
                }.run();
            }

            contactContainer.view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (touchEnabled) {
                        if (event.getAction() == MotionEvent.ACTION_MOVE) {
                           scrollerInUse = true;
                            if (alphabetScrollerContainer.currentAnim != null) {
                                alphabetScrollerContainer.currentAnim.cancel();
                            }
                            alphabetScrollerContainer.view.setVisibility(View.VISIBLE);
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            scrollerInUse = false;
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public int prevScrollY = contactContainer.view.getScrollY();

                                @Override
                                public void run() {
                                    if (UserInterface.running() && !scrollerInUse && !alphabetScrollerInUse) {
                                        if (contactContainer.view.getScrollY() == prevScrollY) {
                                            Anim anim = UserInterface.uiAnim(c, alphabetScrollerContainer, 100);
                                            anim.delay = 1000;
                                            anim.addAlpha(Anim.FADE_OUT);
                                            anim.setEnd(new Runnable() {
                                                @Override
                                                public void run() {
                                                    alphabetScrollerContainer.view.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                            anim.start();
                                        } else {
                                            handler.postDelayed(this, 1);
                                        }
                                        prevScrollY = contactContainer.view.getScrollY();
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
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        yOffset = (int) event.getRawY() - alphabetScroller.y();
                        alphabetScrollerInUse = true;
                        touchEnabled = false;
                        if (alphabetScrollerContainer.currentAnim != null) {
                            alphabetScrollerContainer.currentAnim.cancel();
                        }
                        alphabetScrollerContainer.view.setVisibility(View.VISIBLE);

                        ((UIView.MScrollView)contactContainer.view).smoothScrollTo(0, ((UIView.MScrollView)contactContainer.view).prevScrollY);
                    }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float perc = (event.getRawY() - yOffset - alphabetScrollerContainer.y()) / (float) (alphabetScrollerContainer.height() - ((alphabetScroller.height())));

                        if (perc < 0) perc = 0;
                        if (perc > 1) perc = 1;

                        alphabetScroller.openRLayout().setTopM((int) ((alphabetScrollerContainer.height() - (alphabetScroller.height())) * perc)).setHeight(aScrollerHeight).save();
                        ((UIView.MScrollView)contactContainer.view).smoothScrollTo(0, (int)(perc * (container.height() - alphabetScrollerContainer.height())));

                    }else if (event.getAction() == MotionEvent.ACTION_UP) {
                        touchEnabled = true;
                        alphabetScrollerInUse = false;

                        Anim anim = UserInterface.uiAnim(c, alphabetScrollerContainer, 100);
                        anim.delay = 1000;
                        anim.addAlpha(Anim.FADE_OUT);
                        anim.setEnd(new Runnable() {
                            @Override
                            public void run() {
                                alphabetScrollerContainer.view.setVisibility(View.INVISIBLE);
                            }
                        });
                        anim.start();
                    }
                    return true;
                }
            });
            ((UIView.MScrollView) contactContainer.view).setScrollEvent(new Runnable() {
                @Override
                public void run() {
                    float perc = contactContainer.view.getScrollY() / (float) (container.height() - alphabetScrollerContainer.height());
                    alphabetScroller.openRLayout().setTopM((int) ((alphabetScrollerContainer.height() - (alphabetScroller.height())) * perc)).setHeight(aScrollerHeight).save();
                }
            });

            mainLayout.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (oldBottom != bottom) {
                        float perc = contactContainer.view.getScrollY() / (float)(container.height() - alphabetScrollerContainer.height());
                        alphabetScroller.openRLayout().setTopM((int)((alphabetScrollerContainer.height() -  (alphabetScroller.height())) * perc)).setHeight(aScrollerHeight).save();
                    }
                }
            });
        }

        public int pWidth(float percent) {
            return (int)(contactContainer.width() / 100f * percent);
        }
        private Item genItem(SView parent) {
            Item item;

            SView container = new SView(new RelativeLayout(c), parent.view);
            Util.generateViewId(container.view);
            container.plot();
            container.openRLayout().setWidth(pWidth(80)).setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT).setLeftM(pWidth(10)).save();

            final SView appIcon = new SView(new ImageView(c), container.view);
            ImageUtil.setImageDrawable(appIcon.view, R.drawable.contact_icon);
            appIcon.plot(container.width(), container.width());

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
