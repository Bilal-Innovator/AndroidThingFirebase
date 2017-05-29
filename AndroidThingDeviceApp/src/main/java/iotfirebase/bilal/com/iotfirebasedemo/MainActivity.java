package iotfirebase.bilal.com.iotfirebasedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    Gpio fanGpio, lightGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManagerService service = new PeripheralManagerService();

        try {
            String pinLight = BoardDefaults.getGPIOForLight();
            String pinFan = BoardDefaults.getGPIOForFan();

            lightGpio = service.openGpio(pinLight);
            lightGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            fanGpio = service.openGpio(pinFan);
            fanGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("IOT");
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
                    try {
                        mButtonGpio.setValue(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (value.matches("off")) {
                    try {
                        mButtonGpio.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
    protected void onDestroy() {
        super.onDestroy();
        if (fanGpio != null || lightGpio != null) {
            // Close the Gpio pin
            Log.i(TAG, "Closing Button GPIO pin");
            try {
                fanGpio.close();
                lightGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            } finally {
                fanGpio = null;
                lightGpio = null;
            }
        }
    }

    ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded: " + dataSnapshot + "");

            if (dataSnapshot.getKey().matches("light")) {
                if (dataSnapshot.getValue().toString().matches("on")) {
                    try {
                        lightGpio.setValue(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    try {
                        lightGpio.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "lightSwitch Invalid Value: " + dataSnapshot.getValue() + "");
                }
            } else if (dataSnapshot.getKey().matches("fan")) {
                if (dataSnapshot.getValue().toString().matches("on")) {
                    try {
                        fanGpio.setValue(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    try {
                        fanGpio.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                    try {
                        lightGpio.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    try {
                        lightGpio.setValue(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "lightSwitch Invalid Value: " + dataSnapshot.getValue() + "");
                }
            } else if (dataSnapshot.getKey().matches("fan")) {
                if (dataSnapshot.getValue().toString().matches("on")) {
                    try {
                        fanGpio.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (dataSnapshot.getValue().toString().matches("off")) {
                    try {
                        fanGpio.setValue(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
