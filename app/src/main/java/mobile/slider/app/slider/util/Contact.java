package mobile.slider.app.slider.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;

import static android.provider.VoicemailContract.Voicemails.NUMBER;

public class Contact {
    public String id;
    public String name;
    public String number;
    public String photoURI;

    public Contact(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public static void retrieveContacts(ArrayList<Contact> contactList) {
        ContentResolver cr = SystemOverlay.service.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    Contact c = new Contact(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)), cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)));
                    c.number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    c.photoURI = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    contactList.add(c);
                }
            } finally {
                cursor.close();
            }
            for (int i = 0; i < contactList.size(); i++) {
                Util.log(contactList.get(i).id + " " + contactList.get(i).name + " " + contactList.get(i).number + " " + i);
            }
        }
    }
    public Bitmap getPhoto() {
        Bitmap b = BitmapFactory.decodeResource(SystemOverlay.service.getResources(), R.drawable.contact_icon);
        try {
            b = MediaStore.Images.Media.getBitmap(SystemOverlay.service.getContentResolver(), Uri.parse(photoURI));
        }catch (Exception e) { }
        return b;

    }
}
