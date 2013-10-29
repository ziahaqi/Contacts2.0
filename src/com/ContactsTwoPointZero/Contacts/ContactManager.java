package com.ContactsTwoPointZero.Contacts;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: cramascanu
 * Date: 10/11/13
 * Time: 3:04 PM
 * To change this template use File | Settings | File Templates.
 */

public class ContactManager{
    private HashMap<Integer,Contact> listOfContacts;
    private Activity activity;
    private Handler handler;
    public ContactManager(Activity act){
        listOfContacts = new HashMap<Integer,Contact>();
        activity = act;
    }
    public ContactManager(Activity act, Handler handler) {
        listOfContacts = new HashMap<Integer,Contact>();
        this.handler = handler;
        activity = act;
    }

    public void readContacts() throws InterruptedException {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                readContactsFromPhone();
                Contact contact;
                int nrOfContacts = 0;
                contact = new Contact("Gheorghe Ion ");
                contact.addPhoneNumber("0735425123");
                contact.addPhoneNumber("0215425123");
                contact.addPhoneNumber("075552584");
                contact.setGoogleAccount("jdoe4033");
                listOfContacts.put(nrOfContacts++, contact);

                contact = new Contact("George Popescu");
                contact.addPhoneNumber("0761235123");
                contact.setFacebookAccount("test");
                contact.setYahooAccount("catalin.ramascanu@yahoo.com");
                contact.setGoogleAccount("test");
                listOfContacts.put(nrOfContacts++, contact);

                contact = new Contact("Alexandra Poenaru");
                contact.addPhoneNumber("023183283");
                contact.setGoogleAccount("test");
                listOfContacts.put(nrOfContacts++, contact);

                contact = new Contact("Irina Tomescu");
                contact.addPhoneNumber("023183283");
                contact.addPhoneNumber("0735213882");
                contact.setYahooAccount("test");
                listOfContacts.put(nrOfContacts++, contact);

                Message msg = new Message();
                msg.what = 0;
//                handler.sendMessage(msg);
//            }
//        }).start();


    }

    private void readContactsFromPhone(){
        int contactsSize = 0;
        ContentResolver cr = activity.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        HashMap<Integer,Contact> contacts = new HashMap<Integer,Contact>();
        Contact contact;
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // Create new Contact and add Phone Numbers
                    contact = new Contact(name);
                    System.out.print(name + ": ");
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNumber = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contact.addPhoneNumber(phoneNumber);
                        System.out.print(phoneNumber+" ");
                    }
                    //close phone Cursor.
                    pCur.close();

                    Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (emailCur.moveToNext()) {
                        // This would allow you get several email addresses
                        // if the email addresses were stored in an array
                        String email = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        String emailType = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                        int type = emailCur.getInt(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                        String customLabel = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
                        CharSequence CustomemailType = ContactsContract.CommonDataKinds.Email.getTypeLabel(activity.getResources(), type, customLabel);

                        contact.addEmail(email);
                    }
                    emailCur.close();

                    Bitmap photo = null;

                    try {
                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(cr,
                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(id)));

                        if (inputStream != null) {
                            photo = BitmapFactory.decodeStream(inputStream);
                        }

                        if (inputStream != null) inputStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (photo != null){
                        contact.addProfilePicture(photo);
                    }

                    //Add to contactList
                    contacts.put(contactsSize++,contact);
                }


            }
        }

        listOfContacts = contacts;
    }

    public HashMap<Integer,Contact> getListOfContacts(){
        return listOfContacts;
    }

    public ArrayList<String> getNamesOfContacts(){
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < listOfContacts.size(); i++){
            list.add(listOfContacts.get(i).getName());
        }
        return  list;
    }

    private void addContactToPhone(Contact contact){
        String contactName = contact.getName();
        SparseArray<String> phoneNumbers = new SparseArray<String>();
        String emailID = "email@nomail.com";
        String company = "bad";
        String jobTitle = "abcd";

        ArrayList <ContentProviderOperation> ops = new ArrayList < ContentProviderOperation > ();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            contactName).build());

        //------------------------------------------------------ Mobile Number
        int key = 0;
        for(int i = 0; i < phoneNumbers.size(); i++) {
            key = phoneNumbers.keyAt(i);
            // get the object by the key.
            String phoneNumber = phoneNumbers.get(key);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        //------------------------------------------------------ Email
        if (emailID != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        //------------------------------------------------------ Organization
        if (!company.equals("") && !jobTitle.equals("")) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .build());
        }

        // Asking the Contact provider to create a new contact
        try {
            activity.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity.getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateContact(Contact contact,int position){
        listOfContacts.remove(position);
        listOfContacts.put(position,contact);
    }
}