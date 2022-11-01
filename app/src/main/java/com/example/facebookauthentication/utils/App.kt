package com.example.facebookauthentication.utils

import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        printKeyHash()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    companion object {
        private var instance: App? = null

        fun getInstance(): App? {
            return instance
        }

        fun hasNetwork(): Boolean {
            return instance!!.checkIfHasNetwork()
        }
    }

    fun checkIfHasNetwork(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun printKeyHash() {
        try {
            val info =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.i("key_hash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

}