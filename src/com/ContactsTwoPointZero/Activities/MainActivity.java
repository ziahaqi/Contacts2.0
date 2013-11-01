package com.ContactsTwoPointZero.Activities;

/**
 * Created with IntelliJ IDEA.
 * User: cramascanu
 * Date: 9/26/13
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.*;
import com.ContactsTwoPointZero.Contacts.Contact;
import com.ContactsTwoPointZero.Contacts.ContactListAdapter;
import com.ContactsTwoPointZero.Contacts.ContactManager;
import com.ContactsTwoPointZero.Connections.Facebook.MemorizingTrustManager;
import com.example.ExpandableList.R;
import org.jivesoftware.smack.*;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.util.Collection;

public class MainActivity extends Activity {
    // More efficient than HashMap for mapping integers to objects
    private SparseArray<Contact> contactList;
    private ContactListAdapter adapter;
    private ActivityProfile activityProfile;
    private ContactManager contactManager;
    private Activity thisActivity;
    private ExpandableListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity = this;
        contactList = new SparseArray<Contact>();
        contactManager = new ContactManager(this);
        activityProfile = new ActivityProfile();
        activityProfile.setGoogleAccount("bot.smack21@gmail.com");
        activityProfile.setGooglePassword("Linux1234");
        activityProfile.setYahooAccount("y_smack_test@yahoo.com");
        activityProfile.setYahooPassword("Linux1234");
        activityProfile.setFacebookAccount("100006895481717");
        activityProfile.setFacebookPassword("Linux1234");
        startNormalActivity();

    }

    private void startActivitySetup(){
        setContentView(R.layout.setup_app);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Before you can use Contacts 2.0, you need to setup your social accounts.")
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
               });
        Dialog infoDialog = builder.create();
        infoDialog.show();
        Button saveButton = (Button) findViewById(R.id.save_setup);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityProfile = new ActivityProfile();

                activityProfile.setGoogleAccount(((EditText) findViewById(R.id.g_acc_input)).getText().toString());
                activityProfile.setGooglePassword(((EditText) findViewById(R.id.g_pass_input)).getText().toString());

                activityProfile.setFacebookAccount(((EditText) findViewById(R.id.f_acc_input)).getText().toString());
                activityProfile.setFacebookPassword(((EditText) findViewById(R.id.f_pass_input)).getText().toString());

                activityProfile.setYahooAccount(((EditText) findViewById(R.id.y_acc_input)).getText().toString());
                activityProfile.setYahooPassword(((EditText) findViewById(R.id.y_pass_input)).getText().toString());

//                final ProgressDialog mDialog = new ProgressDialog(thisActivity);
//                mDialog.setMessage("Loading Contacts...");
//                mDialog.setCancelable(false);
//                mDialog.show();

                Intent editContactsActivity = new Intent(thisActivity, EditContactsActivity.class);
                editContactsActivity.putExtra("contactList", contactManager.getListOfContacts());
                editContactsActivity.putExtra("contactNames", contactManager.getNamesOfContacts());
                startActivity(editContactsActivity);
            }
        });
    }

    private void startNormalActivity(){
        createData();
//        ContactManager manager = new ContactManager(this);
//        manager.readContacts();
        listView = (ExpandableListView) findViewById(R.id.listView);
        adapter = new ContactListAdapter(this,listView, contactList);
        listView.setAdapter(adapter);
        initListeners();
    }

    private void initListeners(){

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editContactIntent = new Intent(thisActivity, CreateContactActivity.class);
                editContactIntent.putExtra("givenContact", adapter.getCurrentSelectedContact(position));
                startActivityForResult(editContactIntent,position);
                return true;
            }
        });

        EditText inputSearch = (EditText) findViewById(R.id.searchBox);
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                adapter.filterData(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });


        ImageButton createContact = (ImageButton) findViewById(R.id.addContact);
        createContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, CreateContactActivity.class);
                MainActivity.this.startActivityForResult(myIntent,2);
            }
        });


    }
    public void createData() {
        int nrOfContacts = 0;
        Contact contact;

        contact = new Contact("Gheorghe Ion ");
        contact.addPhoneNumber("0735425123");
        contact.addPhoneNumber("0215425123");
        contact.addPhoneNumber("075552584");
        contact.setGoogleAccount("jdoe4033@gmail.com");
        contactList.append(nrOfContacts++, contact);

        contact = new Contact("George Popescu");
        contact.addPhoneNumber("0761235123");
        contact.setFacebookAccount("catalin.ramascanu");
        contact.setYahooAccount("catalin.ramascanu@yahoo.com");
        contact.setGoogleAccount("bot.smack21@gmail.com");
        contactList.append(nrOfContacts++, contact);

        contact = new Contact("Alexandra Poenaru");
        contact.addPhoneNumber("023183283");
        contact.setGoogleAccount("test");
        contactList.append(nrOfContacts++, contact);

        contact = new Contact("Irina Tomescu");
        contact.addPhoneNumber("023183283");
        contact.addPhoneNumber("0735213882");
        contact.setGoogleAccount("catalin.rmc@gmail.com");
        contact.setYahooAccount("catalin.ramascanu@yahoo.ro");
        contactList.append(nrOfContacts++, contact);

    }

    private void openEditContact(Contact contact){
        setContentView(R.layout.create_contact);

    }

    public ActivityProfile getActivityProfile(){
        return activityProfile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (data.getExtras().containsKey("savedExistedContact") && data != null){
            Contact savedContact = (Contact) data.getExtras().getSerializable("savedExistedContact");
            adapter.updateContact(savedContact, requestCode);
        }
        if (data.getExtras().containsKey("savedNewContact") && data != null){
            Contact savedContact = (Contact) data.getExtras().getSerializable("savedNewContact");
            System.out.println("adding " + savedContact);
            adapter.addContact(savedContact);
        }
    }
}
