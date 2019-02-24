package mobile.slider.app.slider.ui.Contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.model.Anim;
import mobile.slider.app.slider.model.RoundedImageView;
import mobile.slider.app.slider.model.SView.SView;
import mobile.slider.app.slider.model.SView.SWindowLayout;
import mobile.slider.app.slider.model.UIView;
import mobile.slider.app.slider.model.contact.Contact;
import mobile.slider.app.slider.ui.UIClass;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class ContactList {
    public Context c;
    public int aScrollerHeight;
    public UIClass ui;
    public SView contactScroller, alphabetScroller, alphabetScrollerLetter, loading, contactContainer;

    public ContactList(UIClass ui, SView contactContainer) {
        this.contactContainer = contactContainer;
        this.ui = ui;
        this.c = UserInterface.UI.c;
        this.aScrollerHeight = ui.wUnit(50);

        alphabetScroller = new SView(new RelativeLayout(c), contactContainer.view);
        alphabetScrollerLetter = new SView(new ImageView(c), contactContainer.view);
        contactScroller = new SView(new UIView.MScrollView(c), contactContainer.view);
        loading = new SView(new ProgressBar(c), contactContainer.view);

        contactScroller.plot();
        contactScroller.view.setVerticalScrollBarEnabled(false);
        contactScroller.openRLayout()
                .setWidth(RelativeLayout.LayoutParams.MATCH_PARENT)
                .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT)
                .save();

        alphabetScroller.plot();
        alphabetScroller.view.setVisibility(View.INVISIBLE);
        alphabetScroller.openRLayout()
                .setWidth(ui.wUnit(33))
                .setHeight(aScrollerHeight)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                .save();

        SView alphabetScrollerBG = new SView(new ImageView(c),alphabetScroller.view);
        alphabetScrollerBG.plot();
        ImageUtil.setImageDrawable(alphabetScrollerBG.view,R.drawable.scroller_icon);
        alphabetScrollerBG.openRLayout().setWidth(ui.wUnit(25)).setHeight(RelativeLayout.LayoutParams.MATCH_PARENT).addRule(RelativeLayout.ALIGN_PARENT_RIGHT).save();

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
                .setWidth(ui.wUnit(100))
                .setHeight(ui.wUnit(100))
                .addRule(RelativeLayout.CENTER_VERTICAL)
                .save();

        new ContactScroller().setup();
    }
    public class ContactScroller {
        public final float rWidth = ui.wUnit(80);
        public final float rHeight = rWidth * 1.75f;
        public final float textHeight = rHeight / 7;
        public IconLoader iconLoader;
        public final ArrayList<ContactRenderable> contacts = new ArrayList<ContactRenderable>();
        public final ArrayList<ContactRenderable> indicators = new ArrayList<ContactRenderable>();


        public SView container;
        public boolean touchEnabled = false, scrollerInUse = false, alphabetScrollerInUse = false;

        public void setup() {
            container = new SView(new RelativeLayout(c), contactScroller.view);

            setupContactRenderables();
            setupScroller();

            container.plot(ui.wUnit(100), rHeight * contacts.size());

            container.post(new Runnable() {
                @Override
                public void run() {
                    iconLoader = new IconLoader();
                    buffer();
                }
            });

            touchEnabled = true;
        }
        public void setupScroller() {
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
                        }else if (event.getAction() == MotionEvent.ACTION_UP) {
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
                            },1);
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
//                        alphabetScroller.openRLayout().setTopM((int) ((contactContainer.height() - (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();

                        ((UIView.MScrollView) contactScroller.view).smoothScrollTo(0, (int)(perc * (container.height() - contactContainer.height())));
                        alphabetScrollerLetter.openRLayout().setTopM(alphabetScroller.openRLayout().topM).save();

                        int newIndicator = checkIndicator();
                        if (newIndicator != currentIndicator) {
                            currentIndicator = newIndicator;
                            Bitmap b = ImageUtil.mutableBitmap(R.drawable.scroller_letter_icon);
                            b = ImageUtil.drawChar(50,40, indicators.get(currentIndicator).start + "", b);

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
                    for (int i = indicators.size() - 1; i >= 0; i--) {
                        int yPos = indicators.get(i).appIcon.y() + indicators.get(i).appIcon.height();

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
                    buffer();
                    if (container.height() <= contactContainer.height()) return;

                    float perc = contactScroller.view.getScrollY() / (float) (container.height() - contactScroller.height());
                    alphabetScroller.openRLayout().setTopM((int) ((contactContainer.height() - (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();
                }
            });


//            contactContainer.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    if (oldBottom != bottom) {
//                        if (container.height() <= contactContainer.height()) return;
//
//                        float perc = contactScroller.view.getScrollY() / (float)(container.height() - contactScroller.height());
//                        alphabetScroller.openRLayout().setTopM((int)((contactContainer.height() -  (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();
//                    }
//                }
//            });
        }
        public void buffer() {
            ArrayList<ContactRenderable> toRender = new ArrayList<>();
            for (int i = (int) ((contactScroller.view.getScrollY() - rHeight * 4) / rHeight); i <= (contactScroller.height() + contactScroller.view.getScrollY() + (rHeight * 5)) / rHeight; i++) {
                if (i < contacts.size() && i >= 0) {
                    ContactRenderable cr = contacts.get(i);
                    if (!cr.initialized) {
                        toRender.add(cr);
                    }

                }
            }
            iconLoader.renderables = toRender;
        }
        public class IconLoader {
            public ArrayList<ContactRenderable> renderables;

            public IconLoader() {
                renderables = new ArrayList<>();
                ui.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (UserInterface.shouldMove() && !renderables.isEmpty()) {
                            int total = 5;

                            while (total > 0) {
                                if (renderables.isEmpty()) break;

                                ContactRenderable cr = renderables.get(0);
                                renderables.remove(0);

                                if (!cr.initialized) {
                                    load(cr);
                                    total--;
                                }

                            }
                        }
                        ui.postDelayed(this,100);
                    }
                }, 100);
            }
            public void load(ContactRenderable cr) {
                if (cr.contact != null) {
                    cr.appIcon = new SView(new RoundedImageView(c,true), container.view);
                    cr.firstName = new SView(new TextView(c), container.view);
                    cr.lastName = new SView(new TextView(c), container.view);

                    ((TextView) cr.firstName.view).setMaxLines(1);
                    ((TextView) cr.firstName.view).setEllipsize(TextUtils.TruncateAt.END);
                    ((TextView) cr.firstName.view).setGravity(Gravity.CENTER);
                    ((TextView) cr.firstName.view).setText(cr.contact.firstName);
                    ((TextView) cr.firstName.view).setTextSize(TypedValue.COMPLEX_UNIT_PX, textHeight * .75f);


                    ((TextView) cr.lastName.view).setMaxLines(1);
                    ((TextView) cr.lastName.view).setEllipsize(TextUtils.TruncateAt.END);
                    ((TextView) cr.lastName.view).setGravity(Gravity.CENTER);
                    ((TextView) cr.lastName.view).setText(cr.contact.lastName);
                    ((TextView) cr.lastName.view).setTextSize(TypedValue.COMPLEX_UNIT_PX, textHeight * .75f);

                    cr.contact.loadPhoto();
                    ((RoundedImageView) cr.appIcon.view).setImageBitmap(cr.contact.photo);
                    cr.firstName.plot(rWidth, textHeight);
                    cr.lastName.plot(rWidth, textHeight);

                    cr.appIcon.plot(rWidth, rWidth);
                    cr.appIcon.openRLayout()
                            .setTopM((rHeight * cr.index + (rHeight - (cr.appIcon.height() + textHeight * 2)) / 2))
                            .setLeftM((ui.wUnit(100) - cr.appIcon.width()) / 2)
                            .save();

                    cr.firstName.openRLayout()
                            .setTopM(cr.appIcon.openRLayout().getTopM() + cr.appIcon.height())
                            .setLeftM((ui.wUnit(100) - cr.appIcon.width()) / 2)
                            .save();

                    cr.lastName.openRLayout()
                            .setTopM(cr.appIcon.openRLayout().getTopM() + cr.appIcon.height() + cr.firstName.height())
                            .setLeftM((ui.wUnit(100) - cr.appIcon.width()) / 2)
                            .save();
                }
                cr.initialized = true;
            }

        }
        public void setupContactRenderables() {
            char currentIndicator = ' ';
            for (int i = 0; i < Contact.contacts.size(); i++) {
                Contact c = Contact.contacts.get(i);
                char start = c.displayName.charAt(0);
                char addIndicator = ' ';
                if (Character.isLetter(start)) {
                    if (currentIndicator != start) {
                        currentIndicator = start;
                        addIndicator = currentIndicator;
                    }
                }else {
                    char indicator;
                    if (Character.isDigit(start)) {
                        indicator = '#';
                    }else{
                        indicator = '&';
                    }
                    if (currentIndicator != indicator) {
                        currentIndicator = indicator;
                        addIndicator = currentIndicator;
                    }
                }

                if (addIndicator != ' ') {
                    ContactRenderable cr = new ContactRenderable();
                    cr.start = addIndicator;

                    cr.appIcon = new SView(new RoundedImageView(ContactList.this.c,true), container.view);

                    Bitmap b = Bitmap.createBitmap((int)rWidth, (int)rWidth, Bitmap.Config.ARGB_8888);
                    Paint p = new Paint();
                    p.setColor(Color.RED);
                    new Canvas(b).drawRect(0,0,b.getWidth(),b.getHeight(),p);
                    ((ImageView)cr.appIcon.view).setImageBitmap(ImageUtil.drawChar(80,50,cr.start + "",b));


                    cr.appIcon.plot(rWidth / 2, rWidth / 2);
                    cr.appIcon.openRLayout()
                            .setTopM((rHeight * contacts.size() + (rHeight - cr.appIcon.height()) / 2))
                            .setLeftM((ui.wUnit(100) - cr.appIcon.width()) / 2)
                            .save();

                    contacts.add(cr);
                    indicators.add(cr);
                    cr.index = contacts.size() - 1;
                }
                ContactRenderable cr = new ContactRenderable();
                cr.contact = c;
                contacts.add(cr);
                cr.index = contacts.size() - 1;
            }
        }

        public class ContactRenderable {
            public Contact contact;
            public char start;
            public SView appIcon, firstName, lastName;
            public int index = -1;

            public boolean initialized = false;
        }
    }
}
