package com.elmansoft.elmancall.ui.login

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginService {
    @FormUrlEncoded
    @POST("/login.php")
    abstract fun requestLogin(
        @Field("tel") tel:String,
        @Field("uid") userid:String,
        @Field("upw") userpw:String
    ) : Call<String>
}


