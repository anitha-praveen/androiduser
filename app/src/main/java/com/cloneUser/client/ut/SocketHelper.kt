package com.cloneUser.client.ut

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class SocketHelper {

    companion object {
        var TAG = "SocketHelper"

        var lastSocketConnected: Long? = null
        var callingFragment = ""
        var mSocket: Socket? = null
        private var socketDataReceiver: SocketListener? = null
        var sesssion: SessionMaintainence? = null
        fun init(
            preference: SessionMaintainence?,
            socketDataReceivers: SocketListener?,
            tag: String
        ) {
            socketDataReceiver = socketDataReceivers
            sesssion = preference
            setSocketListener()
            callingFragment = tag
        }

        private fun setSocketListener() {
            val opts = IO.Options()
            opts.forceNew = true
            opts.reconnection = true
            opts.transports = arrayOf(WebSocket.NAME)
            try {
                if (mSocket == null) mSocket = IO.socket(Config.SOCKET_URL, opts)
                //  if (!mSocket!!.connected()) {
                Log.v(
                    "SocketTriggering",
                    "xxxxxxxxxxxxxxxxxxxxx" + if (mSocket != null) "Is connected" + mSocket!!.connected() else "mSocket is Null"
                )
                mSocket!!.on(Socket.EVENT_CONNECT, onConnect)
                mSocket!!.on(Socket.EVENT_DISCONNECT, onDisconnect)
                mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                mSocket!!.on(
                    "request_" + sesssion!!.getString(SessionMaintainence.USER_ID),
                    onRequest
                )
                mSocket!!.on(
                    Config.locationchanged_ + sesssion!!.getString(
                        SessionMaintainence.USER_ID
                    ), onLocationChanged
                )
                mSocket!!.on(
                    Config.package_changed_ + sesssion!!.getString(
                        SessionMaintainence.USER_ID
                    ), onPackageChanged
                )
                mSocket!!.on(
                    Config.photo_upload_ + sesssion!!.getString(
                        SessionMaintainence.USER_ID
                    ), onPhotoUpload
                )
                mSocket!!.on(
                    Config.skip_photo_upload_ + sesssion!!.getString(
                        SessionMaintainence.USER_ID
                    ), onSkipNightImg
                )
                mSocket!!.on(
                    Config.kilometer_upload_ + sesssion!!.getString(
                        SessionMaintainence.USER_ID
                    ), onMeterImage
                )

                Log.e(
                    "locationChanges--",
                    "__" + Config.locationchanged_ + sesssion!!.getString(SessionMaintainence.USER_ID)
                )

                mSocket!!.connect()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }

        fun disconnectSocket() {
            if (mSocket == null) return
            /**********Trunning Off Socket */
            val `object` = JSONObject()
            try {
                `object`.put(
                    Config.USER_ID,
                    sesssion!!.getString(SessionMaintainence.USER_ID)
                )
                `object`.put("socket_id", mSocket!!.id() + "")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mSocket!!.disconnect()
            mSocket!!.off(Socket.EVENT_CONNECT, onConnect)
            mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket!!.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket!!.off(
                "request_" + sesssion!!.getString(SessionMaintainence.USER_ID),
                onRequest
            )
            mSocket!!.off(
                Config.locationchanged_ + sesssion!!.getString(
                    SessionMaintainence.USER_ID
                ), onLocationChanged
            )
            mSocket!!.off(
                Config.package_changed_ + sesssion!!.getString(
                    SessionMaintainence.USER_ID
                ), onPackageChanged
            )
            mSocket!!.off(
                Config.photo_upload_ + sesssion!!.getString(
                    SessionMaintainence.USER_ID
                ), onPhotoUpload
            )
            mSocket!!.off(
                Config.skip_photo_upload_ + sesssion!!.getString(
                    SessionMaintainence.USER_ID
                ), onSkipNightImg
            )
            mSocket!!.off(
                Config.kilometer_upload_ + sesssion!!.getString(
                    SessionMaintainence.USER_ID
                ), onMeterImage
            )
        }

        private val onConnect = Emitter.Listener {
            Log.i(TAG, "connected")
            val `object` = JSONObject()
            try {
                `object`.put(
                    Config.USER_ID,
                    sesssion!!.getString(SessionMaintainence.USER_ID)
                )
                //  `object`.put("socket_id", mSocket!!.id() + "")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (socketDataReceiver != null && socketDataReceiver!!.isNetworkConnected()) {
                mSocket!!.emit(
                    Config.start_connect,
                    `object`.toString()
                )
            }
        }

        private val onDisconnect = Emitter.Listener {
            Log.i(TAG, "onDisconnect")
            if (socketDataReceiver != null) socketDataReceiver!!.onDisconnect()
            lastSocketConnected = null
        }

        private val onConnectError = Emitter.Listener {
            Log.i(TAG, "onConnectError")
            if (socketDataReceiver != null) socketDataReceiver!!.onConnectError()
            lastSocketConnected = null
        }

        private val onRequest = Emitter.Listener {
            Log.i(TAG, "onRequest")
            if (it != null && socketDataReceiver != null)
                socketDataReceiver!!.onRequest(it[0].toString())
        }
        private val onLocationChanged =
            Emitter.Listener { args ->
                Log.e(
                    "locationChanges--",
                    "__" + args[0].toString()
                )
                if (socketDataReceiver != null && args != null)
                    socketDataReceiver!!.locationChanged(args[0].toString())
            }

        private val onPackageChanged =
            Emitter.Listener { args ->
                if (socketDataReceiver != null && args != null)
                    socketDataReceiver!!.packageChanged(args[0].toString())
            }

        private val onPhotoUpload =
            Emitter.Listener { args ->
                Log.e("onPhotoUpload","user-Socket")
                if(socketDataReceiver != null && args != null){
                    socketDataReceiver!!.photoUploaded(args[0].toString())
                    Log.e("onPhotoUpload","user-Socket"+args[0].toString())
                }
            }

        private val onSkipNightImg =
            Emitter.Listener { args ->
                if(socketDataReceiver != null && args != null)
                    socketDataReceiver!!.skipNightPhoto(args[0].toString())
            }

        private val onMeterImage =
            Emitter.Listener { args ->
                if(socketDataReceiver != null && args != null)
                    socketDataReceiver!!.meterUploaded(args[0].toString())
            }


        interface SocketListener {
            fun onConnect()
            fun onDisconnect()
            fun onConnectError()
            fun isNetworkConnected(): Boolean
            fun onRequest(request: String)
            fun locationChanged(request: String)
            fun packageChanged(data: String)
            fun photoUploaded(data: String)
            fun skipNightPhoto(data : String)
            fun meterUploaded(data: String)
        }

    }


}