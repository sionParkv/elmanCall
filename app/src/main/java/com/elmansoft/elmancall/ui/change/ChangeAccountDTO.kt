package com.elmansoft.elmancall.ui.change

data class ChangeAccountDTO (
    var custcd: String,
    var seq: String,
    var call_type: String,
    var useyn_call: String,
    var telnum: String,
    var telstate: Int,
    var recvtel: String,
    var recvpernm: String,
    var id: String,
    var passwd: String,
    var ipaddr: String,
    var visible: String,
)
