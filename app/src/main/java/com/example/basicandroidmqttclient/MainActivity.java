package com.example.basicandroidmqttclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.basicandroidmqttclient.MESSAGE";
    public static final String brokerURI = "3.223.10.115";

    Activity thisActivity;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;
        tv = (TextView) findViewById(R.id.textViewSubscribedMsg);
    }

    /** Called when the user taps the Send button */
    public void publishMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText topicName = (EditText) findViewById(R.id.editTextTopicName);
        EditText value = (EditText) findViewById(R.id.editTextValue);

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();
        client.publishWith().topic(topicName.getText().toString()).qos(MqttQos.AT_LEAST_ONCE).payload(value.getText().toString().getBytes()).send();
        client.disconnect();

        String message = topicName.getText().toString() + " " + value.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void sendSubscription(View view) {
        EditText topicName = (EditText) findViewById(R.id.editTextTopicNameSub);

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        //TextView textView = (TextView) findViewById(R.id.textViewSubscribedMsg);
        //textView.setText("Waiting for subscribed message.....");

        // Use a callback to show the message on the screen
        client.toAsync().subscribeWith()
                .topicFilter(topicName.getText().toString())
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    System.out.println("Subscribed message arrived............");
                    thisActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            tv.setText(msg.toString());
                        }
                    });
                })
                .send();
    }



}