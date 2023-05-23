package com.cloneUser.client.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import com.cloneUser.client.MainActivity
import com.cloneUser.client.drawer.DrawerActivity
import com.cloneUser.client.loginOrSignup.LoginOrSignupActivity
import com.cloneUser.client.splash.SplashActivity

@Module
abstract class ActivityBuildersModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class, ViewModelModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class, ViewModelModule::class])
    abstract fun contributeSplash(): SplashActivity

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class, ViewModelModule::class])
    abstract fun contributeDrawer(): DrawerActivity

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class, ViewModelModule::class])
    abstract fun contributeSignupActivity(): LoginOrSignupActivity

}