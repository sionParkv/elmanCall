package com.elmansoft.elmancall.ui.login

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserInfoService {
    @FormUrlEncoded
    @POST("/login_result.php")
    abstract fun request(
            @Field("tel") tel:String,
            @Field("custcd") userid:String,
            @Field("perid") userpw:String,
            @Field("rnum") rnum:String
    ) : Call<UserInfoDTO>
}
