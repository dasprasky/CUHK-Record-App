package com.wbd101.hrvdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    SharedPreferences sp;
    private static final String ONESIGNAL_APP_ID = "eefd8e75-bc11-4803-bd66-5f67de015007";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        sp = getApplicationContext().getSharedPreferences("login", MODE_PRIVATE);
//        sp.edit().clear().apply();
        if(sp.getBoolean("logged",false)){
            goToTabActivity(sp.getInt("age",0), sp.getString("id", "0"), sp.getString("date","0"), sp.getString("prevDay","0"), sp.getString("nextDay", "0"));
        }

        EditText id_value = (EditText)findViewById(R.id.id_value);
        EditText age_value = (EditText)findViewById(R.id.age_value);
        MyEditTextDatePicker exam_date = new MyEditTextDatePicker(MainActivity.this, R.id.editTextDate);
        EditText date_value = (EditText)findViewById(R.id.editTextDate) ;
        Button agree_continue = (Button)findViewById(R.id.continue_button);
        agree_continue.setOnClickListener( v -> {
            if(!sp.getBoolean("agreed_to_terms", false)){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.Disclaimer))
                        .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TextInputLayout id_layout = (TextInputLayout)findViewById(R.id.id_input_layout);
                                TextInputLayout age_layout = (TextInputLayout)findViewById(R.id.age_input_layout);
                                TextInputLayout date_layout = (TextInputLayout)findViewById(R.id.date_input_layout);
                                id_layout.setError(null);
                                age_layout.setError(null);
                                date_layout.setError(null);
                                if(age_value.getText()!=null || id_value.getText()!=null || date_value.getText()!=null){
                                    if(id_value.getText().toString().isEmpty()) {id_layout.setError("Input a valid Student ID"); return;}
                                    if(age_value.getText().toString().isEmpty()||Integer.parseInt(age_value.getText().toString())==0) {age_layout.setError("Input a valid age"); return;}
                                    if(id_value.getText().toString().length() != 10) {id_layout.setError("Invalid Student ID! Student ID must be 10 digits"); return;}
                                    if(date_value.getText().toString().length()<5){date_layout.setError("Input a valid Date"); return;}
                                    String id = id_value.getText().toString();
                                    int age = Integer.parseInt(age_value.getText().toString());
                                    String date = date_value.getText().toString();

                                    String prev_day = String.format("%d", exam_date._prevDay);
                                    String prev_date = prev_day+ date.substring(date.indexOf('/'));
                                    String next_day = String.format("%d", exam_date._nextDay);
                                    String next_date = next_day+ date.substring(date.indexOf('/'));
                                    // can check values to age and id here, depending on requirements

                                    //using shared preferences so that the same user doesn't need to enter student id and age again
                                    goToTabActivity(age, id, date, prev_date, next_date);
                                    sp.edit().putBoolean("logged", true).apply();
                                    sp.edit().putString("id", id).apply();
                                    sp.edit().putInt("age", age).apply();
                                    sp.edit().putString("date",date).apply();
                                    sp.edit().putString("prevDay", prev_date).apply();
                                    sp.edit().putString("nextDay", next_date).apply();
                                    sp.edit().putBoolean("agreed_to_terms", true).apply();
                                }

                            }
                        })
                        .setNegativeButton("Disagree", null)
                        .setMessage(getResources().getString(R.string.Disclaimer_content))
                        .show();
            }


        });
    }
    public void goToTabActivity(int age, String id, String date, String prev_day, String next_day){
//        if(age==0 || id==0){
//            Toast.makeText(this, "STUDENT ID and AGE cannot be 0",Toast.LENGTH_SHORT).show();
//            return;
//        }
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("age", age);
        intent.putExtra("student_id", id);
        intent.putExtra("date", date);
        intent.putExtra("prevDate", prev_day);
        intent.putExtra("nextDate", next_day);
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }

    }
}