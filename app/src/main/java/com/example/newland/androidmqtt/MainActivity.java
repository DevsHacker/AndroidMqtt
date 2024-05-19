package com.example.newland.androidmqtt;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    static String client = "Android"+System.currentTimeMillis();
    static MqttClient mqttClient;
    static TextView tempText;
    static String tempData;
    static TextView humText;
    static String humData;
    static Button fan;
    static boolean isopen = false;
    static RotateAnimation rotateAnimation;
    static LinearInterpolator linearInterpolator;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tempText.setText("温度数据："+tempData+"°C");
            humText.setText("湿度数据："+humData+"%RH");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempText=findViewById(R.id.temp);
        humText=findViewById(R.id.hum);
        fan=findViewById(R.id.fan);

        rotateAnimation = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        linearInterpolator = new LinearInterpolator();
        rotateAnimation.setDuration(500);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(linearInterpolator);

        try {
            mqttClient = new MqttClient("tcp://172.16.40.16:1883",client,null);
            mqttClient.connect();
            mqttClient.subscribe("#", new IMqttMessageListener() {
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    //System.out.println("主题："+s+"消息："+mqttMessage);
                    switch (s){
                        case "sensor/465":
                            JSONObject a = new JSONObject(mqttMessage.toString());
                            String s1 = a.getString("datas");
                            JSONObject b = new JSONObject(s1);
                            if(b.has("m_temp")){
                                JSONObject c = b.getJSONObject("m_temp");
                                String time = c.keys().next();
                                tempData = c.getString(time);
                                System.out.println("温度数据："+tempData);
                            }
                            if(b.has("m_hum")){
                                JSONObject c = b.getJSONObject("m_hum");
                                String time = c.keys().next();
                                humData = c.getString(time);
                                System.out.println("湿度数据："+humData);
                            }
                            handler.sendEmptyMessage(1);
                            break;
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void Fan(View view) {
        if(isopen){
            isopen = false;
            publish("m_fan",0);
            fan.clearAnimation();
        }else{
            isopen = true;
            publish("m_fan",1);
            fan.startAnimation(rotateAnimation);
        }
    }

    public static void publish(String apitag , int data){
        try {
            mqttClient.publish("cmd/465",("{\"apitag\":\""+apitag+"\",\"cmdid\":\""+client+"\",\"data\":"+data+",\"t\":5}").getBytes(),1,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
