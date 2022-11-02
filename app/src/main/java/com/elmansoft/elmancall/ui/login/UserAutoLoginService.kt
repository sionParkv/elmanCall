package com.elmansoft.elmancall.ui.login

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserAutoLoginService {
    @FormUrlEncoded
    @POST("/auto_login_result.php")
    abstract fun request(
        @Field("tel") tel:String,
    ) : Call<UserInfoDTO>
}