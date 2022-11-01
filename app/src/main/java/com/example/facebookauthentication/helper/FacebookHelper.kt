package com.example.facebookauthentication.helper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.GraphRequest.GraphJSONObjectCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import org.json.JSONObject
import java.net.URL


class FacebookHelper(private var context: Context?, private var callback: FacebookHelperCallback?) :
    FacebookCallback<LoginResult>, GraphJSONObjectCallback {

    companion object {
        const val FACEBOOK_ID = "idFacebook"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val EMAIL = "email"
        const val BIRTHDAY = "birthday"
        const val AGE_RANGE = "age_range"
        const val LOCATION = "location"
        const val GENDER = "gender"
        const val PROFILE_PIC = "profile_pic"
    }

    interface FacebookHelperCallback {

        fun onSuccessFacebook(result: Bundle?)

        fun onCancelFacebook()

        fun onErrorFacebook(ex: FacebookException?)
    }

    private var callbackManager: CallbackManager? = null
    private var loginButton: LoginButton? = null

    init {
        callbackManager = CallbackManager.Factory.create()
    }



    fun login(context: Context?) {
        loginButton = LoginButton(context)
        loginButton!!.setPermissions(listOf("public_profile, email"))
        loginButton!!.performClick()
        LoginManager.getInstance().registerCallback(callbackManager, this)
    }

    fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSuccess(loginResult: LoginResult) {
        getFaceBookProfile(loginResult)
    }

    private fun getFaceBookProfile(loginResult: LoginResult) {
        val request: GraphRequest = GraphRequest.newMeRequest(loginResult.accessToken, this)
        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id, first_name, last_name, email, gender, birthday,age_range, location"
        )
        request.parameters = parameters
        request.executeAsync()
    }

    private fun getFacebookData(`object`: JSONObject): Bundle? {
        return try {
            val bundle = Bundle()
            val id = `object`.getString("id")
            val profilePic = URL("https://graph.facebook.com/$id/picture?width=200&height=150")
            bundle.putString(PROFILE_PIC, profilePic.toString())
            bundle.putString(FACEBOOK_ID, id)
            if (`object`.has(FIRST_NAME)) bundle.putString(
                FIRST_NAME,
                `object`.getString("first_name")
            )
            if (`object`.has(LAST_NAME)) bundle.putString(
                LAST_NAME,
                `object`.getString("last_name")
            )
            if (`object`.has(EMAIL)) bundle.putString(EMAIL, `object`.getString("email"))
            if (`object`.has(GENDER)) bundle.putString(GENDER, `object`.getString("gender"))
            if (`object`.has(BIRTHDAY)) bundle.putString(BIRTHDAY, `object`.getString("birthday"))
            if (`object`.has(AGE_RANGE)) bundle.putString(
                AGE_RANGE,
                `object`.getString("age_range")
            )
            if (`object`.has(LOCATION)) bundle.putString(
                LOCATION,
                `object`.getJSONObject("location").getString("name")
            )
            bundle
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun onCancel() {
        callback!!.onCancelFacebook()
    }

    override fun onError(error: FacebookException) {
        callback!!.onErrorFacebook(error)
    }

    fun logout() {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken != null) {
            LoginManager.getInstance().logOut()
        }

    }

    override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
        val facebookData = getFacebookData(obj!!)
        callback!!.onSuccessFacebook(facebookData)
    }

}