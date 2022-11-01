package com.example.facebookauthentication.data

import java.io.Serializable

class UserModel : Serializable {

    var name: String? = null
    var lastName: String? = null
    var facebookId: String? = null
    var imgUrl: String? = null

}