package com.elmansoft.elmancall.ui.contact

data class ContactData(val actnm: String, val actadd: String, val fax: String, val mail: String,
                       val telnum: Array<UserInfoDetailTelDTO>, val det: String, val lat: String, val lng: String
)
