package com.example.facebookauthentication.base

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.facebookauthentication.helper.SharedPreferenceHelper
import com.example.facebookauthentication.utils.Util
import java.util.*

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var preferenceHelper: SharedPreferenceHelper
    private var mProgressDialog: ProgressDialog? = null
    protected var context: Context? = null
    private var _binding: ViewBinding? = null
    protected abstract fun getViewBinding(): VB

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() {
            return _binding as VB
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Util.setLanguage(this@BaseActivity)
        _binding = getViewBinding()
        setContentView(requireNotNull(_binding).root)
        context = this
        preferenceHelper = SharedPreferenceHelper.getInstance()!!
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFontScale()
    }

    override fun onRestart() {
        super.onRestart()
        initFontScale()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgressDialog()

        _binding = null
    }

    protected open fun showProgressDialog(message: String?) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setMessage(message)
            mProgressDialog!!.isIndeterminate = true
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.setCancelable(false)
        }
        mProgressDialog!!.show()
    }

    protected open fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
            mProgressDialog = null
        }
    }

    protected open fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    open fun replaceFragment(id: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(id, fragment, fragment.javaClass.simpleName)
        transaction.commitAllowingStateLoss()
    }

    protected open fun replaceFragmentWithAnimation(id: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(id, fragment, fragment.javaClass.simpleName)
        transaction.commitAllowingStateLoss()
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        if (configuration.fontScale != 1f) {
            initFontScale()
        }
    }

    private fun initFontScale() {
        val configuration = resources.configuration
        configuration.fontScale = 1f
        val metrics = resources.displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = display
            display?.getRealMetrics(metrics)
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(metrics)
        }
        metrics.scaledDensity = configuration.fontScale * metrics.density
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context = createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, metrics)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}