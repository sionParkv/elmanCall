package com.elmansoft.elmancall.ui.contact

data class ContactDTO (
    var hash: String,
    var message: String,
    var status: String,
    var response: Array<UserInfoDetailDTO>,
)