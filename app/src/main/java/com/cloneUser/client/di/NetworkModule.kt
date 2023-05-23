package com.cloneUser.client.di

import com.cloneUser.client.connection.ConnectionHelper
import com.cloneUser.client.connection.HeaderInterceptor
import com.cloneUser.client.connection.MapsHelper
import com.cloneUser.client.ut.Config
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object NetworkModule {
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): ConnectionHelper {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(Config.BASEURL)
            .build()
        return retrofit.create(ConnectionHelper::class.java)
    }


    @Singleton
    @Provides
    fun provideGson() = Gson()

    @Singleton
    @Provides
    fun provideOkHttpClient() = OkHttpClient().newBuilder().apply {
        addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
         addInterceptor(HeaderInterceptor())
        connectTimeout(0, TimeUnit.SECONDS)
        readTimeout(0, TimeUnit.SECONDS)
        writeTimeout(0, TimeUnit.SECONDS)
    }.build()


    @Singleton
    @Provides
    fun provideRetrofitMaps(okHttpClient: OkHttpClient, gson: Gson): MapsHelper {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(Config.GooglBaseURL)
            .build()
        return retrofit.create(MapsHelper::class.java)
    }

}