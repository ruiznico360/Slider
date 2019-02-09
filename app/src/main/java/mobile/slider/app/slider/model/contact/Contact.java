package mobile.slider.app.slider.model.contact;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.ImageUtil;
import mobile.slider.app.slider.util.Util;

public class Contact {
    public static ArrayList<Contact> contacts = new ArrayList<Contact>();
    public static final String ID_TAG = "CONTACT_ID#";
    public static boolean loadedContactIds = false;
    public static boolean loadedContactInfo = false;

    public String id, displayName,photoURI, firstName, lastName;
    public ArrayList<String> numbers = new ArrayList<>();
    public Bitmap photo;

    public Contact(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;

        String[] arr = displayName.split(" ");

        if (arr.length < 2) {
            firstName = arr[0];
        } else {
            firstName = arr[0];
            lastName = arr[arr.length - 1];
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

        for (int i = 0; i < unicodes.size(); i++) {
            for (int p = unicodes.get(i).length() - ID_TAG.length(); p >= 0; p--) {
                if (unicodes.get(i).substring(p, p + ID_TAG.length()).equals(ID_TAG)) {
                    Contact c = new Contact(unicodes.get(i).substring(0, p), unicodes.get(i).substring(p + ID_TAG.length(), unicodes.get(i).length()));
                    for (int x = 0; x < 1; x++) {
                        contacts.add(c);
                    }
                }
            }
        }
        for (int i = 0; i < letterNames.size(); i++) {
            for (int p = letterNames.get(i).length() - ID_TAG.length(); p >= 0; p--) {
                if (letterNames.get(i).substring(p, p + ID_TAG.length()).equals(ID_TAG)) {
                    Contact c = new Contact(letterNames.get(i).substring(0, p), letterNames.get(i).substring(p + ID_TAG.length(), letterNames.get(i).length()));
                    for (int x = 0; x < 1; x++) {
                        contacts.add(c);
                    }
                }
            }
        }
        for (int i = 0; i < numNames.size(); i++) {
            for (int p = numNames.get(i).length() - ID_TAG.length(); p >= 0; p--) {
                if (numNames.get(i).substring(p, p + ID_TAG.length()).equals(ID_TAG)) {
                    Contact c = new Contact(numNames.get(i).substring(0, p), numNames.get(i).substring(p + ID_TAG.length(), numNames.get(i).length()));
                    for (int x = 0; x < 1; x++) {
                        contacts.add(c);
                    }
                }
            }
        }
    }
    public static void retrieveContactInfo() {
        loadedContactInfo = false;
        for (int i = 0; i < contacts.size(); i++) {
            Cursor pCur = SystemOverlay.service.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?", new String[]{contacts.get(i).displayName}, null);
            while (pCur.moveToNext()) {
                contacts.get(i).numbers.add(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                contacts.get(i).photoURI = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            }
            pCur.close();
            contacts.get(i).loadPhoto();
        }
        loadedContactInfo = true;
    }
    public void loadPhoto() {
        try {
            photo = MediaStore.Images.Media.getBitmap(SystemOverlay.service.getContentResolver(), Uri.parse(photoURI));
        }catch (Exception e) {
            Bitmap b = BitmapFactory.decodeResource(SystemOverlay.service.getResources(), R.drawable.contact_icon_background);
            String initials = firstName.charAt(0) + (lastName != null ? lastName.charAt(0) + "" : "");
            photo = ImageUtil.drawChar(50,50,initials.toUpperCase(),b);
        }
    }
}
