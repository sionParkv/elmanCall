package com.elmansoft.elmancall.ui.contact

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ContactService {
    @FormUrlEncoded
    @POST("/etc/user_v2.php")
    abstract fun retrieve(
        @Field("custcd") custcd: String,
        @Field("regflag") regflag: String,
        @Field("page") page: String,
        @Field("tok") tok: String,
        @Field("database") database: String,
    ) : Call<ContactDTO>
}


