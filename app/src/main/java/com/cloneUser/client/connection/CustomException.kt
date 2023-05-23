package com.cloneUser.client.connection

class CustomException constructor(code: Int, exception: String?): Exception() {
    var code = code
    var exception = exception

}