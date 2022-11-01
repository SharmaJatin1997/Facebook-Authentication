package com.example.facebookauthentication.base

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.facebookauthentication.helper.SharedPreferenceHelper

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected lateinit var preferenceHelper: SharedPreferenceHelper
    private var mProgressDialog: ProgressDialog? = null
    private var _binding: ViewBinding? = null
    protected abstract fun getViewBinding(): VB

    protected var mFragmentNavigation: FragmentNavigation? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceHelper = SharedPreferenceHelper.getInstance()!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding()
        return requireNotNull(_binding).root
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(view)
        _binding = null
    }


    protected open fun showProgressDialog(message: String?) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(requireActivity())
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

    // hide keyboard
    protected open fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentNavigation) {
            mFragmentNavigation = context
        }
    }

    interface FragmentNavigation {
        fun pushFragment(fragment: Fragment?)
    }
}