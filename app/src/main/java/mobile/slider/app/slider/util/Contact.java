package mobile.slider.app.slider.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.services.SystemOverlay;

import static android.provider.VoicemailContract.Voicemails.NUMBER;

public class Contact {
    String name;
    String number;

    public Contact(String name) {
        this.name = name;
    }
    public static void retrieveContacts(ArrayList<Contact> contactList) {
        ContentResolver cr = SystemOverlay.service.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] {ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
        if (cursor != null) {
            try {
                final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int phone = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                long contactId;
                String displayName, address, number;
                while (cursor.moveToNext()) {
                    displayName = cursor.getString(displayNameIndex);
                    number = cursor.getString(phone);
//                    for (int i = 0; i < contactList.size(); i++) {
//                        if contactList.get(i.n)
//                    }
                    Contact c = new Contact(displayName);
                    c.number = number;
                    contactList.add(c);
                }
            } finally {
                cursor.close();
            }
            for (int i = 0; i < contactList.size(); i++) {
                Util.log(contactList.get(i).name + " " + contactList.get(i).number + " " + i) ;
            }
            // ListView has to be updated using a ui thread

            // Dismiss the progressbar after 500 millisecondds
        }
    }
}
