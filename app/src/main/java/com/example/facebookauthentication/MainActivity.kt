package com.example.facebookauthentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.Nullable
import com.example.facebookauthentication.base.BaseActivity
import com.example.facebookauthentication.utils.Util
import com.example.facebookauthentication.data.UserModel
import com.example.facebookauthentication.databinding.ActivityMainBinding
import com.example.facebookauthentication.helper.FacebookHelper
import com.example.facebookauthentication.utils.App
import com.facebook.FacebookException

class MainActivity : BaseActivity<ActivityMainBinding>(), FacebookHelper.FacebookHelperCallback,
    AdapterView.OnItemSelectedListener {

    private lateinit var facebookHelper: FacebookHelper
    private var language: String? = null

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        facebookHelper = FacebookHelper(this, this)
        initSpinner()
        initListener()
    }

    private fun initListener() {
        binding.tvLogin.setOnClickListener {
            if (App.hasNetwork()) {
                facebookHelper.login(this)
            } else
                Toast.makeText(this, "Please check internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initSpinner() {
        val adapter =
            SpinnerAdapter(this, resources.getStringArray(R.array.language).toMutableList())
        binding.spinnerTime.adapter = adapter
        binding.spinnerTime.onItemSelectedListener = this

        if (preferenceHelper.getLanguage() == "ENG") {
            binding.spinnerTime.setSelection(0)
        } else if (preferenceHelper.getLanguage() == "GER") {
            binding.spinnerTime.setSelection(1)
        } else {
            binding.spinnerTime.setSelection(0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookHelper.onResult(requestCode, resultCode, data)
    }


    override fun onSuccessFacebook(result: Bundle?) {
        initFBApi(result)
    }

    private fun initFBApi(bundle: Bundle?) {
        val model = UserModel()
        model.name = bundle?.getString(FacebookHelper.FIRST_NAME)
        model.lastName = bundle?.getString(FacebookHelper.LAST_NAME)
        model.imgUrl = bundle?.getString(FacebookHelper.PROFILE_PIC)
        model.facebookId = bundle?.getString(FacebookHelper.FACEBOOK_ID)

        Toast.makeText(this, bundle?.getString(FacebookHelper.FIRST_NAME), Toast.LENGTH_SHORT)
            .show()

    }

    override fun onCancelFacebook() {

    }

    override fun onErrorFacebook(ex: FacebookException?) {
        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        language = parent?.getItemAtPosition(position) as String
        if (language != preferenceHelper.getLanguage()) {
            preferenceHelper.saveLanguage(language)
            Util.setLanguage(this)
            recreate()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}