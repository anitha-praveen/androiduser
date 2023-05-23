package com.cloneUser.client.ut

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.cloneUser.client.R
import com.cloneUser.client.connection.BaseResponse
import com.cloneUser.client.splash.SplashActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    val gson = Gson()
    lateinit var sessionM: SharedPreferences
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val keyGenParameterSpec=
            MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        sessionM = EncryptedSharedPreferences.create(this,Config.sharedPrefName,keyGenParameterSpec,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            Log.d(TAG, (remoteMessage.data["body"].toString()))

            val baseResponse: BaseResponse? =
                Utilz.getSingleObject(remoteMessage.data["body"], BaseResponse::class.java)
            when (baseResponse?.notificationEnum) {
                "local_to_rental" -> {
                    val intent = Intent(Config.LOC_TO_RENT_PUSH)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                "trip_accepted" -> {
                    val intent = Intent(Config.TRIP_ACCEPTED)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                "request_cancelled_by_driver" -> {
                    val intent = Intent(Config.TRIP_CANCEL)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                "request_cancelled_by_dispatcher" -> {
                    val intent = Intent(Config.TRIP_CANCEL)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                "driver_arrived" -> {
                    val intent = Intent(Config.TRIP_ARRIVED)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                "driver_started_the_trip" -> {
                    val intent = Intent(Config.TRIP_STARTED)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                "driver_end_the_trip" -> {
                    val intent = Intent(Config.TRIP_COMPLETED)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                "passenger_upload_images" -> {
                    val intent = Intent(Config.PUSH_UPLOAD_NIGHT)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            }

            val response: BaseResponse =
                Gson().fromJson(remoteMessage.data["body"].toString(), BaseResponse::class.java)
            if (remoteMessage.data["title"].equals("trip_location_changed", ignoreCase = true) ||
                remoteMessage.data["title"].equals("drive_not_approved", ignoreCase = true)
            ) {
                val intent = Intent(Config.ADDRESS_CHANGE)
                intent.putExtra("loc_type", response.result!!.loc_type)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            sendNotification(remoteMessage.data["title"] ?: "Notification")

        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        val keyGenParameterSpec =
            MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        sessionM = EncryptedSharedPreferences.create(
            this,
            Config.sharedPrefName,
            keyGenParameterSpec,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        Log.d(TAG, "Refreshed token: $token")
        sessionM.edit().apply {
            putString(SessionMaintainence.FCM_TOKEN, token)
            apply()
        }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        /*// [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance().beginWith(work).enqueue()
        // [END dispatch_job]*/
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.road_logo_adjust)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"

    }

}