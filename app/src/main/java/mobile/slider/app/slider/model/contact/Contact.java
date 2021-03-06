package mobile.slider.app.slider.model.contact;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class Contact {
    public static ArrayList<Contact> contacts = new ArrayList<Contact>();
    public static final String ID_TAG = "CONTACT_ID#";
    public static boolean loadedContactIds = false;
    public static String INVALID = "INVALID";

    public String id, displayName,photoURI, firstName, lastName;
    public boolean loadingPhoto = false, unload = false;
    public ArrayList<String> numbers = new ArrayList<>();
    public Bitmap photo;

    public Contact(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
        this.photoURI = INVALID;

        String[] arr = displayName.split(" ");

        if (arr.length < 2) {
            firstName = arr[0];
        } else {
            firstName = arr[0] + " ";
            lastName = arr[arr.length - 1];
            for (int i = 1; i < arr.length - 1; i++) {
                firstName += arr[i] + " ";
            }
        }
    }

    public static ArrayList<Contact> retrieveContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        ArrayList<String> letterNames = new ArrayList<>(), numNames = new ArrayList<>(), unicodes = new ArrayList<>();

        loadedContactIds = false;

        ContentResolver cr = SystemOverlay.service.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + ID_TAG + cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (Character.isLetter(name.charAt(0))) {
                        letterNames.add(name);
                    }else if (Character.isDigit(name.charAt(0))) {
                        numNames.add(name);
                    }else{
                        unicodes.add(name);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        alphabetize(letterNames, numNames, unicodes, contacts);
        loadedContactIds = true;

        return contacts;

    }

    public static void alphabetize(ArrayList<String> letterNames, ArrayList<String> numNames, ArrayList<String> unicodes, ArrayList<Contact> contacts) {
        Collections.sort(letterNames);
        Collections.sort(numNames);
        Collections.sort(unicodes);

        int test = 1;
        for (int i = 0; i < unicodes.size(); i++) {
            for (int p = unicodes.get(i).length() - ID_TAG.length(); p >= 0; p--) {
                if (unicodes.get(i).substring(p, p + ID_TAG.length()).equals(ID_TAG)) {
                    for (int x = 0; x < test; x++) {
                        Contact c = new Contact(unicodes.get(i).substring(0, p), unicodes.get(i).substring(p + ID_TAG.length(), unicodes.get(i).length()));
                        contacts.add(c);
                    }
                }
            }
        }
        for (int i = 0; i < letterNames.size(); i++) {
            for (int p = letterNames.get(i).length() - ID_TAG.length(); p >= 0; p--) {
                if (letterNames.get(i).substring(p, p + ID_TAG.length()).equals(ID_TAG)) {
                    for (int x = 0; x < test; x++) {
                        Contact c = new Contact(letterNames.get(i).substring(0, p), letterNames.get(i).substring(p + ID_TAG.length(), letterNames.get(i).length()));
                        contacts.add(c);
                    }
                }
            }
        }
        for (int i = 0; i < numNames.size(); i++) {
            for (int p = numNames.get(i).length() - ID_TAG.length(); p >= 0; p--) {
                if (numNames.get(i).substring(p, p + ID_TAG.length()).equals(ID_TAG)) {
                    for (int x = 0; x < test; x++) {
                        Contact c = new Contact(numNames.get(i).substring(0, p), numNames.get(i).substring(p + ID_TAG.length(), numNames.get(i).length()));
                        contacts.add(c);
                    }
                }
            }
        }
    }
    public void retrieveContactInfo() {
        Cursor pCur = SystemOverlay.service.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?", new String[]{displayName}, null);
        while (pCur.moveToNext()) {
            numbers.add(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        }
        pCur.close();
    }

    public void loadPhoto() {
        if (photo != null) return;

        loadingPhoto = true;
        if (photoURI == null || photoURI.equals(INVALID)) {
            Cursor pCur = SystemOverlay.service.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?", new String[]{displayName}, null);
            while (pCur.moveToNext()) {
                photoURI = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            }
        }

        try {
            Bitmap b = MediaStore.Images.Media.getBitmap(SystemOverlay.service.getContentResolver(), Uri.parse(photoURI));
            Bitmap output = Bitmap.createBitmap(b.getWidth(), b.getHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(output);
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setFilterBitmap(true);
            p.setDither(true);
            p.setColor(Color.parseColor("#BAB399"));
            canvas.drawCircle(output.getWidth() / 2, output.getWidth() / 2, output.getWidth() / 2 - 0.3f, p);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            Rect rect = new Rect(0, 0, output.getWidth(), output.getHeight());
            p.setColor(Color.RED);
            canvas.drawBitmap(b, rect, rect,p);

            if (unload) {
                unloadBitmap();
                unload = false;
            }else{
                photo = output;
            }

        }catch (Exception e) {
            Bitmap b = ImageUtil.mutableBitmap(R.drawable.contact_icon_background);
            String initials = firstName.charAt(0) + (lastName != null ? lastName.charAt(0) + "" : "");
            b = ImageUtil.drawChar(50, 50, initials.toUpperCase(), b);

            if (unload) {
                unloadBitmap();
                unload = false;
            }else{
                photo = b;
            }
        }

        loadingPhoto = false;
    }
    public void unloadPhoto() {
        if (loadingPhoto) {
            unload = true;
        }else{
            unloadBitmap();
        }
    }
    private void unloadBitmap() {
//        photoURI = null;
        if (photo != null) {
            photo.recycle();
            photo = null;
        }
    }
}
