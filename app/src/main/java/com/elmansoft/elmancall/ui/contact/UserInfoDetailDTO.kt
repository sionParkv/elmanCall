package com.elmansoft.elmancall.ui.contact

data class UserInfoDetailDTO (
    var actcd: String,
    var actmail: String,
    var address: String,
    var ctclafi: String,
    var custcd: String,
    var email: String,
    var fax: String,
    var groupnm: String,
    var isgroup: String,
    var lat: String,
    var lng: String,
    var oldaddress: String,
    var regflag: String,
    var remark2: String,
    var tels: Array<UserInfoDetailTelDTO>,
)
