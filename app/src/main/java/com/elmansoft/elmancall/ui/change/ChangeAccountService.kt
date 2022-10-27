package com.elmansoft.elmancall.ui.change

import retrofit2.Call
import retrofit2.http.*

interface ChangeAccountService {
    @POST("/call/info/account")
    abstract fun retrieve(
        @Header("API-TARGET") custcd: String?
    ) : Call<Array<ChangeAccountDTO>>

    @FormUrlEncoded
    @POST("/fix/search_perid.php")
    abstract fun users(
        @Field("custcd") custcd: String,
        @Field("spjangcd") spjangcd: String,
        @Field("database") database: String,
        @Field("ischk") ischk: String,
        @Field("tok") tok: String,
    ) : Call<Array<ChangeUserDTO>>

    @FormUrlEncoded
    @POST("/etc/changeCallState.php")
    abstract fun change(
        @Field("custcd") custcd: String,
        @Field("database") database: String,
        @Field("type") type: String,
        @Field("state") state: String,
        @Field("tel") tel: String,
        @Field("target") target: String,
    ) : Call<Void>
}


