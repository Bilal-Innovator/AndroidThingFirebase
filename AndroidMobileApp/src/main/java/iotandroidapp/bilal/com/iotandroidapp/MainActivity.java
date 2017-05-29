package iotandroidapp.bilal.com.iotandroidapp;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import android.speech.tts.TextToSpeech;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener, View.OnClickListener {

    public static final String TAG = "MainActivity";

    TextToSpeech textToSpeech;
    Switch lightSwitch, fanSwitch;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private static final int REQUEST_AUDIO_PERMISSIONS_ID = 33;
    AIService aiService;

    Button btnStartListening, btnStopListening;
    TextView txtListeningResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAudioRecordPermission();
        InitUI();
        InitFirebase();
        InitAI();
    }

    private void InitUI() {
        btnStartListening = (Button) findViewById(R.id.btnStartListening);
        btnStartListening.setOnClickListener(this);
        btnStopListening = (Button) findViewById(R.id.btnStopListening);
        btnStopListening.setOnClickListener(this);

        txtListeningResult = (TextView) findViewById(R.id.txtListeningResult);

        lightSwitch = (Switch) findViewById(R.id.lightSwitch);
        fanSwitch = (Switch) findViewById(R.id.fanSwitch);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {

                }
            }
        });

        lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    myRef.child("light").setValue("on");
                } else if (!isChecked) {
                    myRef.child("light").setValue("off");
                }
            }
        });

        fanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    myRef.child("fan").setValue("on");
                } else if (!isChecked) {
                    myRef.child("fan").setValue("off");
                }
            }
        });
    }

    private void InitAI() {
        AIConfiguration config = new AIConfiguration("8b61524ffb7b482894247e9c7259c775 ",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }

    private void InitFirebase() {

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FCM Token: " + token);

        // Write a message to the firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("IOT");

        myRef.addChildEventListener(mChildEventListener);

        // Read from the database
        /*myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);

                Log.d(TAG, "Value is: " + value);

                if (value.matches("on")) {
                    toggleButton.setChecked(true);
                } else if (value.matches("off")) {
                    toggleButton.setChecked(false);
                } else {
                    Log.d(TAG, "Invalid Value: " + value);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });*/

    }

    @Override
    public void onResult(AIResponse response) {
        Log.v(TAG, response + "");
        Gson gson = new Gson();
        txtListeningResult.setText(gson.toJson(response));
        Result result = response.getResult();
        Log.v(TAG + " Speech :", result.getFulfillment().getSpeech() + "");

        if (result.getFulfillment().getSpeech().matches("office light is now on")) {
            myRef.setValue("on");
        } else if (result.getFulfillment().getSpeech().matches("office light is now off")) {
            myRef.setValue("off");
        }

        textToSpeech.speak(result.getFulfillment().getSpeech(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onError(AIError error) {
        Log.v(TAG, error + "");
    }

    @Override
    public void onAudioLevel(float level) {
        Log.v(TAG, level + "");
    }

    @Override
    public void onListeningStarted() {
        Log.v(TAG, "Listening Started");
    }

    @Override
    public void onListeningCanceled() {
        Log.v(TAG, "Listening Canceled");
    }

    @Override
    public void onListeningFinished() {
        Log.v(TAG, "Listening Finished");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartListening:
                aiService.startListening();
                break;
            case R.id.btnStopListening:
                aiService.stopListening();
                break;
            default:
                break;
        }
    }

    protected void checkAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        REQUEST_AUDIO_PERMISSIONS_ID);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSIONS_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        aiService.cancel();
    }

    ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded: " + dataSnapshot + "");

            if (dataSnapshot.getKey().matches("light")) {
                if (dataSnapshot.getValue().toString().matches("on")) {
                    lightSwitch.setChecked(true);
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    lightSwitch.setChecked(false);
                } else {
                    Log.d(TAG, "lightSwitch Invalid Value: " + dataSnapshot.getValue() + "");
                }
            } else if (dataSnapshot.getKey().matches("fan")) {
                if (dataSnapshot.getValue().toString().matches("on")) {
                    fanSwitch.setChecked(true);
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    fanSwitch.setChecked(false);
                } else {
                    Log.d(TAG, "fanSwitch Invalid Value: " + dataSnapshot.getValue() + "");
                }
            } else {
                Log.d(TAG, "Invalid key: " + dataSnapshot.getKey() + "");
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged: " + dataSnapshot + "");

            if (dataSnapshot.getKey().matches("light")) {
                if (dataSnapshot.getValue().toString().matches("on")) {
                    lightSwitch.setChecked(true);
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    lightSwitch.setChecked(false);
                } else {
                    Log.d(TAG, "lightSwitch Invalid Value: " + dataSnapshot.getValue() + "");
                }
            } else if (dataSnapshot.getKey().matches("fan")) {
                if (dataSnapshot.getValue().toString().matches("on")) {
                    fanSwitch.setChecked(true);
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    fanSwitch.setChecked(false);
                } else {
                    Log.d(TAG, "fanSwitch Invalid Value: " + dataSnapshot.getValue() + "");
                }
            } else {
                Log.d(TAG, "Invalid key: " + dataSnapshot.getKey() + "");
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved: " + dataSnapshot + "");
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved: " + dataSnapshot + "");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "DatabaseError: " + databaseError + "");
        }
    };
}
