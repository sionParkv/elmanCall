package com.elmansoft.elmancall

import com.elmansoft.elmancall.ui.login.UserInfoDTO
import org.json.JSONObject

class UserInfo private constructor() {

    var data: UserInfoDTO? = null
    var telnum: String = ""

    companion object {
        @Volatile private var instance: UserInfo? = null

        @JvmStatic fun getInstance(): UserInfo =
            instance ?: synchronized(this) {
                instance ?: UserInfo().also {
                    instance = it
                }
            }
    }

    fun init(info: UserInfoDTO?, tel: String) {
        data = info
        telnum = tel
    }
}
