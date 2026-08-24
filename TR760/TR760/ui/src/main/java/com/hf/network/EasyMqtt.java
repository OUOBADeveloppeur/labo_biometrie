//package com.hf.network;
//
//import static android.content.Context.TELEPHONY_SERVICE;
//
//import android.app.Activity;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//
//import org.eclipse.paho.android.service.MqttAndroidClient;
//import org.eclipse.paho.client.mqttv3.IMqttActionListener;
//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.json.JSONObject;
//
//import java.math.BigInteger;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//
///**
// * @author tx
// * @date 2023/6/14 10:31
// * @target MQTT请求
// */
//public class EasyMqtt {
//
//    private final String TAG = "EasyMqtt";
//    /* 设备三元组信息 */
//    final private String PRODUCTKEY = "i2krG******";
//    final private String DEVICENAME = "MPU6050_Android";
//    final private String DEVICESECRET = "bf4bff7cc******b3f5**80******3ff";
//    final private String REGION_ID = "cn-shanghai";
//
//    /*获取手机IMEI号*/
//    public static String TelephonyIMEI="";
//
//    /* 自动Topic, 用于上报消息 */
//    final private String PUB_TOPIC = "/sys/" + PRODUCTKEY + "/" + DEVICENAME + "/thing/event/property/post";
//    /* 自动Topic, 用于接受消息 */
//    final private String SUB_TOPIC = "/sys/" + PRODUCTKEY + "/" + DEVICENAME + "/thing/service/property/set";
//    /*上报正文*/
//    private String BODY_FORMAT = "{\"id\":\""+TelephonyIMEI+"\",\"params\":{%s},\"version\":\"1.0\",\"method\":\"thing.event.property.post\"}";
//
//
//    /* 阿里云Mqtt服务器域名 */
//    final String host = "tcp://" + PRODUCTKEY + ".iot-as-mqtt." + REGION_ID + ".aliyuncs.com:1883";     //"tcp://" +
//    private String clientId;
//    private String userName;
//    private String passWord;
//
//    MqttAndroidClient mqttAndroidClient;
//
//    public EasyMqtt(Activity activity){
//
//        /*获取手机IMEI号*/
//        TelephonyManager mTm = (TelephonyManager)activity.getSystemService(TELEPHONY_SERVICE);
//        TelephonyIMEI = mTm.getDeviceId();
//
//        AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
//        if (aiotMqttOption == null) {
//            Log.e(TAG, "device info error");
//        } else {
//            clientId = aiotMqttOption.getClientId();
//            userName = aiotMqttOption.getUsername();
//            passWord = aiotMqttOption.getPassword();
//        }
//
//        /* 创建MqttConnectOptions对象，并配置username和password。 */
//        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setUserName(userName);
//        mqttConnectOptions.setPassword(passWord.toCharArray());
//
//        /* 创建MqttAndroidClient对象，并设置回调接口。 */
//        mqttAndroidClient = new MqttAndroidClient(activity.getApplicationContext(), host, clientId);
//        mqttAndroidClient.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//                Log.i(TAG, "connection lost");
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
//                //JSON解析
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//                Log.i(TAG, "msg delivered");
//            }
//        });
//
//        /* 建立MQTT连接。 */
//        try {
//            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "connect succeed");
//
////                    subscribeTopic(SUB_TOPIC);
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "connect failed");
//                }
//            });
//
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//
////        //此处为发送数据的部分，可能用不到，注意注释掉
////        Button OledOpen = findViewById(R.id.OledOpen);
////        OledOpen.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                publishMessage("\"CardID\":\"" + Old_Delete_CardID + "\"");
////            }
////        });
//    }
//
//
//    /**
//     * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
//     */
//    public static class AiotMqttOption {
//        private String username = "";
//        private String password = "";
//        private String clientId = "";
//
//        public String getUsername() { return this.username;}
//        public String getPassword() { return this.password;}
//        public String getClientId() { return this.clientId;}
//
//        /**
//         * 获取Mqtt建连选项对象
//         * @param productKey 产品秘钥
//         * @param deviceName 设备名称
//         * @param deviceSecret 设备机密
//         * @return AiotMqttOption对象或者NULL
//         */
//        public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
//            if (productKey == null || deviceName == null || deviceSecret == null) {
//                return null;
//            }
//
//            try {
//                String timestamp = Long.toString(System.currentTimeMillis());
//
//                // clientId
//                this.clientId = TelephonyIMEI + "." + deviceName + "|timestamp=" + timestamp +
//                        ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";
//
//                // userName
//                this.username = deviceName + "&" + productKey;
//
//                // password
//                String macSrc = "clientId" + TelephonyIMEI + "." + deviceName + "deviceName" +
//                        deviceName + "productKey" + productKey + "timestamp" + timestamp;
//                String algorithm = "HmacSHA256";
//                Mac mac = Mac.getInstance(algorithm);
//                SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
//                mac.init(secretKeySpec);
//                byte[] macRes = mac.doFinal(macSrc.getBytes());
//                password = String.format("%064x", new BigInteger(1, macRes));
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//
//            return this;
//        }
//    }
//
//    /*JSON解析*/
//    private void parseDiffJson(String json){
//        try {
//            //获取JSON数据中，字符串checkFailedData数据
//            JSONObject jsonObjectAll = new JSONObject(json);
//
//            String checkFailedData = jsonObjectAll.optString("items");
//            JSONObject jsonObject = new JSONObject(checkFailedData);
//            //解析传过来的数据，标识名(CardID)替换为自己的即可
///*            try {
//                String SCardID = jsonObject.optString("CardID");
//                JSONObject json_card = new JSONObject(SCardID);
//                String asd = json_card.optString("value");
//                if(!TextUtils.isEmpty(asd))
//                { CardID = asd; }
//                Log.i(TAG, "卡号： "+ CardID);
//            } catch (Exception e) {
//            }*/
//
//        }catch (Exception e){
//
//        }
//    }
//
//    /*
//     * 向默认的主题/user/update发布消息
//     * @param payload 消息载荷
//     */
//    public void publishMessage(String payload2) {
//        try {
//            String payload  = String.format(BODY_FORMAT,payload2);
//
//            if (mqttAndroidClient.isConnected() == false) {
//                mqttAndroidClient.connect();
//            }
//
//            MqttMessage message = new MqttMessage();
//            message.setPayload(payload.getBytes());
//            message.setQos(0);
//            mqttAndroidClient.publish(PUB_TOPIC, message,null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "publish succeed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "publish failed!");
//                }
//            });
//        } catch (MqttException e) {
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        }
//    }
//
//    /*
//     * 订阅特定的主题
//     * @param topic mqtt主题
//     */
//    public void subscribeTopic(String topic) {
//        try {
//            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "subscribed succeed");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "subscribed failed");
//                }
//            });
//
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
