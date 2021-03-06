package mobile.slider.app.slider.ui.Contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
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
    public SView alphabetScroller, alphabetScrollerLetter, loading, contactContainer;

    public ContactList(UIClass ui, SView contactContainer) {
        this.contactContainer = contactContainer;
        this.ui = ui;
        this.c = UserInterface.UI.c;
        this.aScrollerHeight = ui.wUnit(50);

        alphabetScroller = new SView(new RelativeLayout(c), contactContainer.view);
        alphabetScrollerLetter = new SView(new ImageView(c), contactContainer.view);
        loading = new SView(new ProgressBar(c), contactContainer.view);

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

        if (!Contact.loadedContactIds) {
            loading.view.setVisibility(View.VISIBLE);
            ui.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Contact.loadedContactIds) {
                        loading.view.setVisibility(View.INVISIBLE);
                        new ContactScroller().setup();
                    }else {
                        ui.postDelayed(this, 1);
                    }
                }
            },1);
        }else{
            new ContactScroller().setup();
        }
    }
    public class ContactScroller {
        public Bitmap alphabetScrollerBuffer;
        public float scrollY = 0;
        public boolean cancelAutoScroll = false;

        public final float rWidth = ui.wUnit(80);
        public final float rHeight = rWidth * 1.75f;
        public final float textHeight = rHeight / 7;
        public final ArrayList<ContactRenderable> contacts = new ArrayList<ContactRenderable>();

        public SView container;
        public boolean touchEnabled = false, scrollerInUse = false, alphabetScrollerInUse = false;

        public void setup() {
            setupContactRenderables();
            setupScroller();

            container = new SView(new ContactDrawer(), contactContainer.view);
            container.plot(ui.wUnit(100),RelativeLayout.LayoutParams.MATCH_PARENT, 0);


            contactContainer.post(new Runnable() {
                @Override
                public void run() {
                    touchEnabled = true;
                }
            });
        }
        public void setupScroller() {
            alphabetScroller.view.setOnTouchListener(new View.OnTouchListener() {
                public int yOffset;
                public int currentIndicator = -1;
                public Buffer buffer;
                public boolean end;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ContactDrawer cr = ((ContactDrawer)container.view);
                    if (cr.totalHeight <= container.height()) return true;

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        end = false;
                        yOffset = (int) event.getRawY() - alphabetScroller.y();
                        alphabetScrollerInUse = true;
                        touchEnabled = false;
                        if (alphabetScroller.currentAnim != null) {
                            alphabetScroller.currentAnim.cancel();
                        }
                        alphabetScroller.view.setVisibility(View.VISIBLE);
                        cancelAutoScroll();

                    }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float perc = (event.getRawY() - yOffset - contactContainer.y()) / (float) (contactContainer.height() - ((alphabetScroller.height() * 2)));

                        if (perc < 0) perc = 0;
                        if (perc > 1) perc = 1;

                        alphabetScroller.view.setVisibility(View.INVISIBLE);
                        alphabetScrollerLetter.view.setVisibility(View.VISIBLE);
                        alphabetScroller.openRLayout().setTopM((int) ((contactContainer.height() - (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();

                        scrollY = (int)(perc * (container.height() - cr.totalHeight));

                        alphabetScrollerLetter.openRLayout().setTopM(alphabetScroller.openRLayout().topM).save();

                        int newIndicator = checkIndicator();
                        if (newIndicator != currentIndicator) {
                            currentIndicator = newIndicator;
                            Bitmap b = ImageUtil.mutableBitmap(R.drawable.scroller_letter_icon);
                            b = ImageUtil.drawChar(50,40, contacts.get(currentIndicator).start + "", b);

                            ((ImageView)alphabetScrollerLetter.view).setImageBitmap(b);
                        }

                        ui.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (end) {
                                    setAlphabetScrollerBuffer(null);
                                    touchEnabled = true;
                                    alphabetScrollerInUse = false;
                                    cr.invalidate();
                                    return;
                                }
                                if (!alphabetScrollerInUse) {
                                    return;
                                }

                                cr.invalidate();
                                if (buffer == null || buffer.done) {
                                    buffer = new Buffer() {
                                        @Override
                                        public void run() {
                                            Bitmap b = Bitmap.createBitmap(cr.getWidth(), cr.getHeight(), Bitmap.Config.ARGB_8888);
                                            cr.drawTo(new Canvas(b));
                                            setAlphabetScrollerBuffer(b);
                                            done = true;
                                        }
                                    };
                                    buffer.start();
                                }
                                ui.postDelayed(this, 1);

                            }
                        },1);

                    }else if (event.getAction() == MotionEvent.ACTION_UP) {
                        end = true;
                        alphabetScroller.view.setVisibility(View.VISIBLE);
                        alphabetScrollerLetter.view.setVisibility(View.INVISIBLE);

                        if (buffer.done) {
                            setAlphabetScrollerBuffer(null);
                            touchEnabled = true;
                            alphabetScrollerInUse = false;
                            cr.invalidate();
                        }

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
                    ContactDrawer d = ((ContactDrawer)container.view);
                    if (scrollY == 0) return 0;

                    int indicator = currentIndicator;
                    for (int i = contacts.size() - 1; i >= 0; i--) {
                        ContactRenderable cr = contacts.get(i);

                        if (cr.contact == null) {
                            int yPos = (int)(i * rHeight) + (int)(scrollY + d.indicatorPicSize + d.indicatorOffset) + container.y();

                            if (yPos <= container.y() + container.height()) {
                                indicator = i;
                                break;
                            }
                        }
                    }
                    return indicator;
                }
            });
            contactContainer.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (oldBottom != bottom) {
                        ContactDrawer cr = ((ContactDrawer)container.view);
                        if (cr.totalHeight <= container.height()) return;

                        updateAlphabetScroller(MotionEvent.ACTION_MOVE);
                    }
                }
            });
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

                    contacts.add(cr);
                }
                ContactRenderable cr = new ContactRenderable();
                cr.contact = c;
                contacts.add(cr);
            }
        }

        public void cancelAutoScroll() {
            cancelAutoScroll = true;
        }

        public void setAlphabetScrollerBuffer(Bitmap b) {
            alphabetScrollerBuffer = b;
        }
        public Bitmap getAlphabetScrollerBuffer() {
            return alphabetScrollerBuffer;
        }

        public void updateAlphabetScroller(int action) {
            ContactDrawer cr = ((ContactDrawer)container.view);
            if (action == MotionEvent.ACTION_DOWN) {
                scrollerInUse = true;
                if (alphabetScroller.currentAnim != null) {
                    alphabetScroller.currentAnim.cancel();
                }
                alphabetScroller.view.setVisibility(View.VISIBLE);
            }else if (action == MotionEvent.ACTION_MOVE) {
                float perc = scrollY / (float) (container.height() - cr.totalHeight);
                alphabetScroller.openRLayout().setTopM((int) ((contactContainer.height() - (alphabetScroller.height() * 2)) * perc)).setHeight(aScrollerHeight).save();
            }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                scrollerInUse = false;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public int prevScrollY = (int)scrollY;

                    @Override
                    public void run() {
                        if (UserInterface.running() && !scrollerInUse && !alphabetScrollerInUse) {
                            if ((int)scrollY == prevScrollY) {
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
                            prevScrollY = (int)scrollY;
                        }
                    }
                },1);
            }
        }

        public class ContactDrawer extends View {
            public Paint textPaint, bmapPaint;
            public float textSize = textHeight * .75f;
            public float contactPicSize = rWidth;
            public float indicatorPicSize = rWidth / 2;
            public int prevRangeT = 0, prevRangeB = 0;

            public final float contactOffset = (int)(rHeight - (contactPicSize + textHeight * 2)) / 2;
            public final float indicatorOffset = contactOffset + contactPicSize / 4;
            public final float totalHeight = rHeight * contacts.size();

            public ContactDrawer() {
                super(c);
                textPaint = new Paint();
                bmapPaint = new Paint();

                textPaint.setColor(Color.rgb(30,30,30));
                textPaint.setFakeBoldText(true);
                textPaint.setTextSize(textSize);

                setOnTouchListener(new OnTouchListener() {
                    public float prevScrollY = 0;
                    public float prevMovement = 0;
                    public long prevMovementTime = 0;
                    public float prevVel = 0;
                    public float velScroll = 0;
                    public final float MAX_VEL = 60;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (totalHeight <= contactContainer.height() || !touchEnabled) return true;

                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            velScroll = 0;
                            prevScrollY = event.getRawY();
                            prevMovementTime = event.getEventTime();

                            updateAlphabetScroller(event.getAction());
                        }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            float movement = event.getRawY() - prevScrollY;
                            if (nextScroll(movement) != movement) {
                                scrollY = nextScroll(movement);
                            }else {
                                scrollY += nextScroll(movement);
                            }

                            prevVel = ((event.getRawY() - prevScrollY) / (float)(event.getEventTime() - prevMovementTime));
                            prevScrollY = event.getRawY();
                            prevMovement = movement;
                            prevMovementTime = event.getEventTime();
                            invalidate();

                            updateAlphabetScroller(event.getAction());
                        }else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            velScroll =  Math.abs(prevVel) > MAX_VEL ? (prevVel < 0 ? -MAX_VEL : MAX_VEL) : prevVel * 40f;

                            ui.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (cancelAutoScroll) {
                                        cancelAutoScroll = false;
                                        return;
                                    }
                                    if (velScroll != 0) {
                                        float nextScroll = nextScroll(velScroll);

                                        if (nextScroll != velScroll) {
                                            velScroll = 0;
                                            scrollY = nextScroll;
                                        }else {
                                            if (Math.abs(velScroll) < 2) {
                                                velScroll = 0;
                                            }else {
                                                velScroll /= 1.1f;
                                            }
                                            scrollY += nextScroll;
                                        }

                                        updateAlphabetScroller(MotionEvent.ACTION_MOVE);
                                        invalidate();
                                        ui.postDelayed(this, 1);
                                    }
                                }
                            },1);
                            updateAlphabetScroller(event.getAction());

                            return false;
                        }
                        return true;
                    }
                });

            }

            public float nextScroll(float attempt) {
                if (scrollY + attempt > 0) return 0f;
                else if (scrollY + attempt < -totalHeight + getHeight()) return -totalHeight + getHeight();
                else{
                    return attempt;
                }
            }

            public void drawTo(Canvas cn) {
                final float scrollY = ContactScroller.this.scrollY;
                int newRangeT = (int)((-scrollY - rHeight) / rHeight), newRangeB = (int)((getHeight() + -scrollY + (rHeight)) / rHeight);

                if (newRangeT < 0) newRangeT = 0;
                if (newRangeB > contacts.size()) newRangeB = contacts.size();

                boolean finished = true;

                for (int i = newRangeT; i < newRangeB ; i++) {
                    ContactRenderable cr = contacts.get(i);
                    Bitmap toDraw;

                    if (cr.contact == null) {
                        toDraw = getIndicatorPhoto(cr.start + "");

                        int height = (int)(i * rHeight) + (int)scrollY;
                        Rect src = new Rect(0, 0, toDraw.getWidth(),  toDraw.getHeight());
                        Rect dst = new Rect((int)(cn.getWidth() - indicatorPicSize) / 2, (int)(height + indicatorOffset), (int)(cn.getWidth() + indicatorPicSize) / 2, (int)(height + indicatorOffset + indicatorPicSize));
                        cn.drawBitmap(toDraw, src,dst,bmapPaint);
                    }else{
                        if (!cr.contact.loadingPhoto) {
                            cr.contact.loadPhoto();
                        }
                        toDraw = cr.contact.photo;

                        int height = (int)(i * rHeight + scrollY);
                        Rect dst = new Rect((int)(cn.getWidth() - contactPicSize) / 2, height, (int)(cn.getWidth() + contactPicSize) / 2, height + (int)contactPicSize);

                        if (toDraw == null) {
                            Paint p = new Paint();
                            p.setColor(Color.BLACK);
                            cn.drawCircle((dst.left + dst.right) / 2,(dst.top + dst.bottom) / 2, dst.width() / 2, p);
                            finished = false;
                        }else{
                            Rect src = new Rect(0, 0, toDraw.getWidth(), toDraw.getHeight());
                            cn.drawBitmap(toDraw, src, dst, bmapPaint);
                        }

                        drawText(cn,cr.contact.firstName, (int)(height + contactOffset + contactPicSize));

                        if (cr.contact.lastName != null) {
                            drawText(cn,cr.contact.lastName, (int)(height + contactOffset + contactPicSize + textHeight));
                        }
                    }
                }

                for (int i = prevRangeT; i < prevRangeB; i++) {
                    if (i < newRangeT || i > newRangeB) {
                        ContactRenderable cr = contacts.get(i);
                        if (cr.contact != null) {
                            cr.contact.unloadPhoto();
                        }
                    }
                }

                if (!finished) {
                    ui.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    },1);
                }
                prevRangeB = newRangeB;
                prevRangeT = newRangeT;
            }
            @Override
            public void onDraw(Canvas cn) {
                if (alphabetScrollerInUse) {
                    Bitmap toDraw = getAlphabetScrollerBuffer();
                    if (toDraw != null) {
                        Rect src = new Rect(0, 0, toDraw.getWidth(), toDraw.getHeight());
                        Rect dst = new Rect(0, 0, getWidth(), getHeight());
                        cn.drawBitmap(toDraw, src, dst, bmapPaint);
                    }
                    return;
                }
                drawTo(cn);
            }

            public Bitmap getIndicatorPhoto(String text) {
                Bitmap b = Bitmap.createBitmap((int)rWidth, (int)rWidth, Bitmap.Config.ARGB_8888);
                Paint p = new Paint();
                p.setColor(Color.RED);
                new Canvas(b).drawCircle(b.getWidth() / 2,b.getHeight() / 2,b.getWidth() / 2,p);

                return ImageUtil.drawChar(80,50,text,b);
            }
            public void drawText(Canvas c, String text, int height) {
                text = text.trim().replaceAll(" +", " ");

                final Rect tBounds = new Rect();
                final float maxSize = c.getWidth() * .9f;

                String newText = text;
                textPaint.getTextBounds(newText,0,newText.length(), tBounds);

                while (tBounds.width() > maxSize) {
                    newText = newText.substring(0, newText.length() - 1);
                    textPaint.getTextBounds(newText + "...",0,(newText + "...").length(), tBounds);
                }
                c.drawText(newText.equals(text) ? text : newText + "...", (c.getWidth() - tBounds.width())  / 2, height + textSize, textPaint);
            }
        }
    }
    public class Buffer extends Thread {
        boolean done = false;
    }
    public class ContactRenderable {
        public Contact contact;
        public char start;
    }
}
