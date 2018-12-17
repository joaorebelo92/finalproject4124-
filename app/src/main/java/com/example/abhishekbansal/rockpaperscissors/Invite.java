package com.example.abhishekbansal.rockpaperscissors;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Invite extends AppCompatActivity {

    private final int REQUEST_CODE=99;
    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 900;
    private static final String TAG = "test";
    EditText smsField, no;
    Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        checkManyPermisions();

        Button btPick=(Button)findViewById(R.id.selectNumber);
        btPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        smsField = findViewById(R.id.main_smsField_id);

        sendBtn = findViewById(R.id.main_smsSendBtn_id);

        no = findViewById(R.id.main_id_no);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:"+ no.getText().toString()));
                intent.putExtra("sms_body", "Heyy found this amazing game. Come Lets Play..!!");
                startActivity(intent);

            }
        });

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (REQUEST_CODE):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        String hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String num = "";
                        if (Integer.valueOf(hasNumber) == 1) {
                            Cursor numbers = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                            while (numbers.moveToNext()) {
                                num = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Toast.makeText(Invite.this, "Number="+num, Toast.LENGTH_LONG).show();

                                no.setText(num);
                            }
                        }
                    }
                    break;
                }
        }
    }

    private void checkManyPermisions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not yet granted. Use requestPermissions().
            Log.d(TAG, "App does not have permission to open contact book.");
            Log.d(TAG, "Asking for permission now!");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS},
                    ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted.
            Log.d(TAG, "App already has permission for phone calls.");
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // For the requestCode, check if permission was granted or not.
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean phonePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean smsPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    Log.d(TAG, "Does phone have permission? " + phonePermission);
                    Log.d(TAG, "Does SMS have permission? " + smsPermission);
                }
            }
        }
    }

}

