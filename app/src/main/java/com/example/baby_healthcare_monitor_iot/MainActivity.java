package com.example.baby_healthcare_monitor_iot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private CardView temp, fan, hum, cradle;
    private Switch switchFan, switchCradle;
    private TextView tempData, humData;

    private OkHttpClient client;
    private String url;
    private ScheduledExecutorService scheduleTaskExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp = findViewById(R.id.temperature);
        fan = findViewById(R.id.fan);
        hum = findViewById(R.id.humidity);
        cradle = findViewById(R.id.cradle);
        switchFan = findViewById(R.id.fanSwitch);
        switchCradle = findViewById(R.id.cradleSwitch);
        tempData = findViewById(R.id.tmp);
        humData = findViewById(R.id.humdt);

        client = new OkHttpClient();
        url = "https://api.thingspeak.com/channels/2057774/feeds.json?api_key=JMA39SHBS0OQLHCH&results=1";

        // Schedule a task to update data every 5 seconds
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                updateData();
            }
        }, 0, 5, TimeUnit.SECONDS);

        // Set the on click listener for the fan switch
        switchFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFan.isChecked()) {
                    sendFanData("1");
                } else {
                    sendFanData("0");
                }
            }
        });

        // Set the on click listener for the cradle switch
        switchCradle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchCradle.isChecked()) {
                    sendCradleData("1");
                } else {
                    sendCradleData("0");
                }
            }
        });
    }

    // Method to update data from Thingspeak
    private void updateData() {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            /*@Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }*/

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray feedsArray = jsonObject.getJSONArray("feeds");
                        JSONObject feedObject = feedsArray.getJSONObject(0);

                        // Update temperature and humidity data
                        final String temperature = feedObject.getString("field1") + " °C";
                        final String humidity = feedObject.getString("field2") + " %";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tempData.setText(temperature);
                                humData.setText(humidity);
                            }
                        });

                        // Update fan and cradle switches
                        final String fanData = feedObject.getString("field3");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fanData.equals("1")) {
                                    switchFan.setChecked(true);
                                } else {
                                    switchFan.setChecked(false);
                                }
                            }
                        });
                        // Update cradle switch
                        final String cradleData = feedObject.getString("field4");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (cradleData.equals("1")) {
                                    switchCradle.setChecked(true);
                                } else {
                                    switchCradle.setChecked(false);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendFanData(String fanState) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("api_key", "MJ7U2LILF8CT8FNT")
                .add("field3", fanState)
                .build();

        Request request = new Request.Builder()
                .url("https://api.thingspeak.com/update")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Data sent successfully
                } else {
                    // Data not sent successfully
                }
            }
        });
    }

    private void sendCradleData(String cradleState) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("api_key", "MJ7U2LILF8CT8FNT")
                .add("field4", cradleState)
                .build();

        Request request = new Request.Builder()
                .url("https://api.thingspeak.com/update")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Data sent successfully
                } else {
                    // Data not sent successfully
                }
            }
        });
    }
}

