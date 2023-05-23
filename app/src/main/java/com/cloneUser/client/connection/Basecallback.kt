package com.cloneUser.client.connection

import retrofit2.Call

/**
 * Custom Call Back function to return
 * response Success or Failure in All APIS
 * T is the response type class
 * */
interface Basecallback<T> {
    fun onSuccessfulApi(response: T?)
    fun onFailureApi(call: Call<T?>?, e : CustomException )
}