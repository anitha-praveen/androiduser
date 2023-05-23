package com.cloneUser.client

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.maps.MapsInitializer
import com.cloneUser.client.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

/**
 * @see https://medium.com/@shashankmohabia/taxiappz-android-with-com-dependency-injection-for-android-3a7e33ad1013
 * */
class MyApplication : DaggerApplication(), LifecycleObserver {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.factory().create(this)
    }
    companion object{
        var appContext: Context? = null
        var inBackground = false
    }
    override fun onCreate() {
        super.onCreate()
        appContext = this
        MapsInitializer.initialize(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        inBackground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // app moved to background
        inBackground = true
    }
}