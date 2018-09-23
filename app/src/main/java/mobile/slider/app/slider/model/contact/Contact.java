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

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;
import mobile.slider.app.slider.ui.UserInterface;
import mobile.slider.app.slider.util.Util;

public class Contact {
    public static ArrayList<Contact> contacts = new ArrayList<>();
    public static boolean loadedContactIds = false;
    public static boolean loadedContactInfo = false;

    public String id;
    public String name;
    public ArrayList<String> numbers = new ArrayList<>();
    public String photoURI;
    public Bitmap photo;

    public Contact(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public static ArrayList<Contact> retrieveContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        loadedContactIds = false;

        ContentResolver cr = SystemOverlay.service.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    Contact c = new Contact(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)), cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                    contacts.add(c);
                }
            } finally {
                cursor.close();
            }
            loadedContactIds = true;
        }
        return contacts;
    }

    public static void retrieveContactInfo() {
        loadedContactInfo = false;
        for (int i = 0; i < contacts.size(); i++) {
            Cursor pCur = SystemOverlay.service.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?", new String[]{contacts.get(i).name}, null);
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
        Bitmap b = BitmapFactory.decodeResource(SystemOverlay.service.getResources(), R.drawable.contact_icon);
        try {
            b = MediaStore.Images.Media.getBitmap(SystemOverlay.service.getContentResolver(), Uri.parse(photoURI));
        }catch (Exception e) { }
        photo = b;

    }
}
