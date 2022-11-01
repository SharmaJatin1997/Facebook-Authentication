package com.example.facebookauthentication.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Color
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.example.facebookauthentication.helper.SharedPreferenceHelper

import java.io.File
import java.net.URISyntaxException
import java.util.*
import java.util.regex.Pattern

object Util {

    val FILTER_EMOJI =
        label@ InputFilter { source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int ->
            var index = start
            while (index < end) {
                val type = Character.getType(source[index])
                if (type == Character.SURROGATE.toInt() || type == Character.NON_SPACING_MARK.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    return@InputFilter ""
                }
                index++
            }
            null
        }

    val FILTER_WHITE_SPACE =
        label@ InputFilter { source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int ->
            var i = start
            while (i < end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
                i++
            }
            null
        }


    fun hideWindowStatusBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    @SuppressLint("NewApi")
    @Throws(URISyntaxException::class)
    fun getFilePath(context: Context, uri: Uri): String? {
        var uri = uri
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(
                context.applicationContext,
                uri
            )
        ) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(
                    split[1]
                )
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(
                MediaStore.Images.Media.DATA
            )
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver
                    .query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }



    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getTotalPages(total: Int, perPage: Int): Int {
        return (total + perPage - 1) / perPage
    }

    fun capSentences(text: String?): String {
        return if (text == null || text.isEmpty()) {
            ""
        } else text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase()
    }

    fun sharePost(context: Context, title: String?, description: String?) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, description)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isValidPhone(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches()
    }

    fun sendEmail(context: Context, emailTo: String, subject: String?, message: String?) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailTo))
        i.putExtra(Intent.EXTRA_SUBJECT, subject)
        i.putExtra(Intent.EXTRA_TEXT, message)
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {

        }
    }

    fun openUrl(context: Context, url: String?) {
        if (!URLUtil.isValidUrl(url)) {
            return
        }
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun isRunning(ctx: Context): Boolean {
        val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
        for (task in tasks) {
            if (ctx.packageName.equals(
                    task.baseActivity!!.packageName,
                    ignoreCase = true
                )
            ) return true
        }
        return false
    }


    fun calculateDiscount(amount: String, totalAmount: String): Float {
        return amount.toFloat() * totalAmount.toFloat() / 100
    }

    fun removeLastChar(str: String): String {
        return str.substring(0, str.length - 1)
    }

    fun convertDpToPixel(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun validateEmail(email: String?): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pattern = Pattern.compile(emailRegex)
        return if (email == null) false else pattern.matcher(email).matches()
    }

    private fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

    fun composeEmail(context: Context, addresses: String, subject: String?, message: String?) {
        val intent = Intent(Intent.ACTION_SENDTO)
        val recipients = arrayOf(addresses)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, recipients)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (ex: ActivityNotFoundException) {
        }
    }

    fun setFullTextUnderLine(textview: TextView) {
        textview.paintFlags = textview.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun setMaxLength(length: Int, editText: EditText) {
        val fArray = arrayOfNulls<InputFilter>(2)
        fArray[0] = LengthFilter(length)
        fArray[1] = FILTER_EMOJI
        editText.filters = fArray
    }

    fun setMaxLengthEmojiEnabled(length: Int, editText: EditText) {
        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = LengthFilter(length)
        editText.filters = fArray
    }

    fun setMaxLengthWithoutSpace(length: Int, editText: EditText) {
        val fArray = arrayOfNulls<InputFilter>(3)
        fArray[0] = LengthFilter(length)
        fArray[1] = FILTER_EMOJI
        fArray[2] = FILTER_WHITE_SPACE
        editText.filters = fArray
    }

    fun getEmojiFilter(): Array<InputFilter> {
        return arrayOf(FILTER_EMOJI, FILTER_WHITE_SPACE)
    }

    fun roundOffTo2DecPlaces(value: Float): String {
        return String.format("%.1f", value)
    }


    fun getLetters(string: String, letterCount: Int): String? {
        var string = string
        var letterCount = letterCount
        var stringBuilder = StringBuilder()
        string = capSentences(string)
        val array = string.split(" ".toRegex()).toTypedArray()
        if (letterCount > array.size) {
            letterCount = array.size
        }
        for (i in 0 until letterCount) {
            if (stringBuilder.toString().isEmpty()) stringBuilder =
                StringBuilder(array[i]) else stringBuilder.append(" ").append(
                array[i]
            )
        }
        return stringBuilder.toString()
    }

    fun getFirstName(name: String): String? {
        val index = if (name.indexOf(" ") > 0) name.indexOf(" ") else name.length
        return capSentences(name.substring(0, index))
    }

    fun getLastName(name: String): String? {
        val index = if (name.indexOf(" ") > 0) name.indexOf(" ") else name.length
        return if (index >= name.length) {
            ""
        } else capSentences(name.substring(index))
    }

    fun formatNumber(
        editText: EditText,
        insertedChar: String?,
        isInsert: Boolean,
        formatAfterInteger: Int,
    ) {
        val string = editText.text.toString()
        val sb = StringBuilder()
        if (isInsert) {
            val givenString = string.replace(insertedChar!!, "")
            for (i in 0 until givenString.length) {
                if (i > formatAfterInteger - 1 && i % formatAfterInteger == 0) {
                    sb.append(insertedChar)
                }
                sb.append(givenString[i])
            }
        } else {
            sb.append(string)
            sb.deleteCharAt(string.length - 1)
        }
        editText.setText(sb.toString())
        editText.setSelection(editText.text.length)
    }

    fun maskCardNumber(cardNumber: String, maskChar: String?): String? {
        var cardNumber = cardNumber
        cardNumber = cardNumber.replace(" ", "")
        var var1 = 0
        val buffer: StringBuffer = StringBuffer()
        while (var1 < cardNumber.length) {
            val string = buffer.toString().replace(" ", "")
            if (string.length > 3 && string.length % 4 == 0) {
                buffer.append(" ")
            }
            if (var1 > cardNumber.length - 5) {
                buffer.append(cardNumber[var1])
            } else {
                buffer.append(maskChar)
            }
            ++var1
        }
        return buffer.toString()
    }

    fun loadDataInWebView(description: String, webView: WebView) {
        val encodedHtml = Base64.encodeToString(description.toByteArray(), Base64.NO_PADDING)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.defaultFixedFontSize = 12
        webSettings.fixedFontFamily = "Poppins-Light.otf"
        webView.webViewClient = WebViewClient()
        webView.loadData(encodedHtml, "text/html", "base64")
    }

    fun openFile(context: Context, file: File) {
        try {
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_VIEW)
            if (file.toString().contains(".doc") || file.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword")
            } else if (file.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf")
            } else if (file.toString().contains(".ppt") || file.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
            } else if (file.toString().contains(".xls") || file.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel")
            } else if (file.toString().contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip")
            } else if (file.toString().contains(".rar")) {
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed")
            } else if (file.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf")
            } else if (file.toString().contains(".wav") || file.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav")
            } else if (file.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif")
            } else if (file.toString().contains(".jpg") || file.toString()
                    .contains(".jpeg") || file.toString().contains(".png")
            ) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg")
            } else if (file.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain")
            } else if (file.toString().contains(".3gp") || file.toString().contains(".mpg") ||
                file.toString().contains(".mpeg") || file.toString()
                    .contains(".mpe") || file.toString().contains(".mp4") || file.toString()
                    .contains(".avi")
            ) {
                // Video files
                intent.setDataAndType(uri, "video/*")
            } else {
                intent.setDataAndType(uri, "*/*")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {

        }
    }

    fun openAppSetting(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    @SuppressLint("Recycle")
    fun getFileName(imagePath: String?): String? {
        return File("" + Uri.parse(imagePath)).name
    }

    fun getFileSize(uri: String): String {
        val file = File(uri)
        val length = file.length() / 1024
        return getSize(length)
    }

    fun getSize(size: Long): String {
        var s = ""
        val kb = (size / 1024).toDouble()
        val mb = kb / 1024
        val gb = mb / 1024
        val tb = gb / 1024
        if (size < 1024L) {
            s = "$size KB"
        } else if (size >= 1024 && size < 1024L * 1024) {
            s = String.format("%.2f", kb) + " KB"
        } else if (size >= 1024L * 1024 && size < 1024L * 1024 * 1024) {
            s = String.format("%.2f", mb) + " MB"
        } else if (size >= 1024L * 1024 * 1024 && size < 1024L * 1024 * 1024 * 1024) {
            s = String.format("%.2f", gb) + " GB"
        } else if (size >= 1024L * 1024 * 1024 * 1024) {
            s = String.format("%.2f", tb) + " TB"
        }
        return s
    }

    fun getFilterEmoji(): Array<InputFilter> {
        return arrayOf(FILTER_EMOJI)
    }

    fun setLanguage(context: Activity) {
        val lang = SharedPreferenceHelper.getInstance()!!.getLanguage()
        var locale: Locale? = null
        if (lang == null)
            locale = Locale("en", "US")
        else if (lang.equals("ENG", true))
            locale = Locale("en", "US")
        else if (lang.equals("GER", true))
            locale = Locale("de", "DE")

        Locale.setDefault(locale!!)

        val config = Configuration()

        config.locale = locale
        context.baseContext.resources.updateConfiguration(
            config,
            context.baseContext.resources.displayMetrics
        )
    }

    object EmojiFilter {
        val filter: Array<InputFilter>
            get() {
                val EMOJI_FILTER =
                    InputFilter { source, start, end, dest, dstart, dend ->
                        for (index in start until end) {
                            val type = Character.getType(source[index])
                            if (type == Character.SURROGATE.toInt() || type == Character.NON_SPACING_MARK.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                                return@InputFilter ""
                            }
                        }
                        null
                    }
                return arrayOf(EMOJI_FILTER)
            }
    }

    fun spinnerIndex(spinner: Spinner, string: String?): Int {
        var index = 0
        string?.let {
            for (i in 0 until spinner.count) {
                if (spinner.getItemAtPosition(i).toString().equals(it, ignoreCase = true)) {
                    index = i
                    break
                }
            }
        }
        return index
    }

    @SuppressLint("MissingPermission")
    fun hasNetwork(): Boolean {
        var isConnected = false
        val connectivityManager =
            App.getInstance()!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            isConnected = when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                activeNetworkInfo?.run {
                    isConnected = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }
        return isConnected
    }
}