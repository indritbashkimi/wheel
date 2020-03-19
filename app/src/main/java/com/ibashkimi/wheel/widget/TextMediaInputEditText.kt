package com.ibashkimi.wheel.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputEditText

class TextMediaInputEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    val inputContentInfoLiveData = MutableLiveData<InputContentInfoCompat>()

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        val ic: InputConnection = super.onCreateInputConnection(editorInfo)!!
        EditorInfoCompat.setContentMimeTypes(
            editorInfo,
            arrayOf("image/*", "video/mp4", "video/webm")
        )
        val callback =
            InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
                val lacksPermission = (flags and
                        InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0
                // read and display inputContentInfo asynchronously
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && lacksPermission) {
                    try {
                        inputContentInfo.requestPermission()
                    } catch (e: Exception) {
                        return@OnCommitContentListener false // return false if failed
                    }
                }

                inputContentInfoLiveData.value = inputContentInfo
                // read and display inputContentInfo asynchronously.
                // call inputContentInfo.releasePermission() as needed.
                true  // return true if succeeded
            }
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback)
    }
}