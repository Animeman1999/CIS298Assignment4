package edu.kvcc.cis298.cis298assignment4;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by David Barnes on 11/3/2015.
 */
public class BeverageFragment extends Fragment {

    //String key that will be used to send data between fragments
    private static final String ARG_BEVERAGE_ID = "crime_id";

    //private class level vars for the model properties
    private EditText mId;
    private EditText mName;
    private EditText mPack;
    private EditText mPrice;
    private CheckBox mActive;
    private Button mContactButton;
    private Button mSendDetailsButton;
    private static final int REQUEST_CONTACT = 1;  //constant request code needed when getting the result of the select contact intent.
    private String mContactName;
    private String mEmailString;
    ArrayList<String> emailNames = new ArrayList<String>();


    //Private var for storing the beverage that will be displayed with this fragment
    private Beverage mBeverage;

    //Public method to get a properly formatted version of this fragment
    public static BeverageFragment newInstance(String id) {
        //Make a bungle for fragment args
        Bundle args = new Bundle();
        //Put the args using the key defined above
        args.putString(ARG_BEVERAGE_ID, id);

        //Make the new fragment, attach the args, and return the fragment
        BeverageFragment fragment = new BeverageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //When created, get the beverage id from the fragment args.
        String beverageId = getArguments().getString(ARG_BEVERAGE_ID);
        //use the id to get the beverage from the singleton
        mBeverage = BeverageCollection.get(getActivity()).getBeverage(beverageId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Check to see if the result code is ok.
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        //Check to see that there is data
        if (requestCode == REQUEST_CONTACT && data !=null){

            //Set a URI, which is a pointer to the data that was returned.
            Uri contactUri = data.getData();

            //Create a new cursor.
            ContentResolver cr = getActivity().getContentResolver();
            Cursor c = cr.query(contactUri, null, null, null, null);
            try {//If the cursor is empty return
                if (c.getCount() == 0){
                    return;
                }
                c.moveToFirst(); // Make sure we are at the beginign

                //Get the name of the contact and place on the ContactButton.
                mContactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                mContactButton.setText(mContactName);

                //create a sting to hold the id.
                String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                //Create a cursor to get the email address and place in the string.
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext()) {
                    //pull the email address out of the string;
                    mEmailString = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.e("Email", mEmailString);
                }
                cur1.close(); //close the cursor to prevent memory leaks.
            }finally {
                c.close();//close the cursor to prevent memory leaks.
            }
        }
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Use the inflator to get the view from the layout
        View view = inflater.inflate(R.layout.fragment_beverage, container, false);

        //Get handles to the widget controls in the view
        mId = (EditText) view.findViewById(R.id.beverage_id);
        mName = (EditText) view.findViewById(R.id.beverage_name);
        mPack = (EditText) view.findViewById(R.id.beverage_pack);
        mPrice = (EditText) view.findViewById(R.id.beverage_price);
        mActive = (CheckBox) view.findViewById(R.id.beverage_active);

        //Set the widgets to the properties of the beverage
        mId.setText(mBeverage.getId());
        mId.setEnabled(false);
        mName.setText(mBeverage.getName());
        mPack.setText(mBeverage.getPack());
        mPrice.setText(Double.toString(mBeverage.getPrice()));
        mActive.setChecked(mBeverage.isActive());

        //Text changed listenter for the id. It will not be used since the id will be always be disabled.
        //It can be used later if we want to be able to edit the id.
        mId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBeverage.setId(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Text listener for the name. Updates the model as the name is changed
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBeverage.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Text listener for the Pack. Updates the model as the text is changed
        mPack.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBeverage.setPack(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Text listener for the price. Updates the model as the text is typed.
        mPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If the count of characters is greater than 0, we will update the model with the
                //parsed number that is input.
                if (count > 0) {
                    mBeverage.setPrice(Double.parseDouble(s.toString()));
                //else there is no text in the box and therefore can't be parsed. Just set the price to zero.
                } else {
                    mBeverage.setPrice(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Set a checked changed listener on the checkbox
        mActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBeverage.setActive(isChecked);
            }
        });



        // Create a constant intent to use for picking a contact from the contact contract.
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mContactButton = (Button) view.findViewById(R.id.seclect_contact);

        mContactButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact,REQUEST_CONTACT);

            }
        });



        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null){
            mContactButton.setEnabled(false);
        }

        mSendDetailsButton = (Button) view.findViewById(R.id.send_beverage_details);

        mSendDetailsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                String [] TO = {mEmailString};  // Must be in an array form for the Intent
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");

                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_TEXT, getBeverageReport());
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Beverage App Item Information");

                emailIntent = Intent.createChooser(emailIntent,"Send Beverage Data via");
                startActivity(emailIntent);
            }
        });


        //Lastley return the view with all of this stuff attached and set on it.
        return view;

    }

    private String getBeverageReport(){ //Create the email body report.
        if (mContactName == null){//When no contact name place generic greeting.
            mContactName = "Hi there";
        }
        String isActiveString;
        if (mBeverage.isActive()){//Parse out if the beverage is active and place appropreat message
            isActiveString = "Currently Active";
        }else {
            isActiveString = "Currently Inactive";
        }
        //Create the report into one string.
        String report =  mEmailString + ",\n\n" + mContactName +",\n\n" + "Please Review the Following Beverage.\n\n" + mBeverage.getId()+ "\n"
                + mBeverage.getName() + "\n" + mBeverage.getPack() + "\n" + mBeverage.getPrice() + "\n" + isActiveString;

        return report;
    }
}
