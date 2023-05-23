package com.cloneUser.client.di

import android.app.Application
import dagger.Module
import dagger.Provides
import com.cloneUser.client.ut.SessionMaintainence

@Module
object AppModule {
    // it is in the companion object because we want a static method here
//    companion object {
    @Provides
    fun provideSession(application: Application): SessionMaintainence {
        return SessionMaintainence(application)
    }

    @Provides
    fun someString(): String {
        return M("Hai I am from Injection").iAmAStr
    }

//    val iAmAStr = "Hai I am from Injection"

    class M {
        lateinit var iAmAStr: String;

        constructor(abcd: String) {
            iAmAStr = abcd
        }
    }

    @Provides
    fun bindStorage(str: String): Any {
        return (str.toString())
    }
}