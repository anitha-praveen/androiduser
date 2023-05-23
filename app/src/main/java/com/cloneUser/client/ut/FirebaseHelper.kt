package com.cloneUser.client.ut

import android.util.Log
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.google.gson.Gson
import org.json.JSONObject

class FirebaseHelper(
    sessionMaintainence: SessionMaintainence,
    observer: FirebaseObserver,
    isInTrip: Boolean
) {

    companion object {
        const val TAG = "FirebaseHelper"
    }

    var session: SessionMaintainence = sessionMaintainence
    var firebaseObserver: FirebaseObserver = observer
    var isInsideTrip = isInTrip

    //    var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("drivers")
//    var requestRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("requests")
    var geoRef: GeoFire? = null
    var geoQuery: GeoQuery? = null
    var listeners = HashMap<String, ValueEventListener>()
    var queryEventListener: GeoQueryEventListener? = null
    var ref: DatabaseReference? = null
    var requestRef: DatabaseReference? = null

    init {
        val database = FirebaseDatabase.getInstance()
        ref = database.getReference("drivers")
        requestRef = database.getReference("requests")
    }

    fun queryDrivers(latLng: LatLng, radius: Double) {
        geoRef = GeoFire(ref)
        geoQuery = geoRef!!.queryAtLocation(
            GeoLocation(latLng.latitude, latLng.longitude),
            radius
        )
        queryEventListener = object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                Log.v("fatal_log", "onKeyEntered: $key")
                ref!!.child(key)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val jsonObject = JSONObject()
                            try {
                                for (ds in snapshot.children) {
                                    jsonObject.put(ds.key!!, ds.value)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            firebaseObserver.driverEnteredFence(
                                key,
                                location,
                                jsonObject.toString()
                            )
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("DatabaseError", error.message)
                        }
                    })
            }

            override fun onKeyExited(key: String) {
                Log.v("fatal_log", "onKeyExited $key")

                ref!!.child(key)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val jsonObject = JSONObject()
                            try {
                                for (ds in snapshot.children) {
                                    jsonObject.put(ds.key!!, ds.value)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            firebaseObserver.driverExitedFence(
                                key,
                                jsonObject.toString()
                            )
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                Log.v("fatal_log", "onKeyMoved $key")
                ref!!.child(key)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val jsonObject = JSONObject()
                            try {
                                for (ds in snapshot.children) {
                                    jsonObject.put(ds.key!!, ds.value)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            firebaseObserver.driverMovesInFence(
                                key,
                                location,
                                jsonObject.toString()
                            )
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onGeoQueryReady() {
                Log.v("fatal_log", "onGeoQueryReady")
            }

            override fun onGeoQueryError(error: DatabaseError) {
                Log.v("fatal_log", "Error: $error")
            }
        }
        geoQuery!!.addGeoQueryEventListener(queryEventListener)
    }


    fun addObserverFor(key: String?) {
        val driverRef: DatabaseReference = ref!!.child(key!!)
        val eventListener = driverRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jsonObject = JSONObject()
                try {
                    for (ds in snapshot.children) {
                        jsonObject.put(ds.key, ds.value)
                    }
                    var latLng: LatLng? = null
                    if (jsonObject.has("is_active")) {
                        val isActive = jsonObject.getBoolean("is_active")
                        val stringLatLng =
                            jsonObject.getString("l").replace("[", "").replace("]", "")
                                .replace(" ", "")
                        val parts = stringLatLng.split(",")
                        if (parts.size == 2) {
                            val lat = parts[0]
                            val lng = parts[1]
                            latLng = LatLng(lat.toDouble(), lng.toDouble())
                        }

                        if (!isActive) {
                            firebaseObserver.driverWentOffline(key)
                        } else {
                            firebaseObserver.driverDataUpdated(
                                key, latLng!!,
                                jsonObject.toString()
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        listeners.put(key!!, eventListener)
    }


    fun removeObserverFor(key: String?) {
        val driverRef: DatabaseReference = ref!!.child(key!!)
        if (listeners.containsKey(key)) {
            driverRef.removeEventListener(listeners.get(key)!!)
        }
    }

    fun addTripObserverFor(requestId: String) {
        Log.v("", "requestId: $requestId")
        val tripRef: DatabaseReference = requestRef!!.child(requestId)
        tripRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jsonStr = Gson().toJson(snapshot.value)
                firebaseObserver.tripStatusReceived(jsonStr)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    interface FirebaseObserver {
        fun driverEnteredFence(key: String?, location: GeoLocation?, response: String?)
        fun driverExitedFence(key: String?, response: String?)
        fun driverMovesInFence(key: String?, location: GeoLocation?, response: String?)
        fun driverWentOffline(key: String?)
        fun driverDataUpdated(key: String?, latLng: LatLng, response: String?)
        fun tripStatusReceived(response: String?)
    }
}