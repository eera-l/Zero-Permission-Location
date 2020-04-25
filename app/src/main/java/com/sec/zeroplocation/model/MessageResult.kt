package com.sec.zeroplocation.model

class MessageResult constructor(result : Int, data : Geolocation) {

    var result : Int = 0
    var data : Geolocation = Geolocation(0.0, 0.0)
}