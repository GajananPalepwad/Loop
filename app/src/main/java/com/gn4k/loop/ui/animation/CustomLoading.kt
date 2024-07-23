package com.gn4k.loop.ui.animation

import android.app.Activity
import android.app.Dialog
import com.gn4k.loop.R

class CustomLoading(activity: Activity?) {
    private var activity: Activity? = activity
    private var dialog: Dialog? = null

    fun startLoading() {
        dialog = Dialog(activity!!, R.style.AppBottomSheetDialogTheme)
        dialog!!.setContentView(R.layout.custom_loading_dialog)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    fun stopLoading() {
        dialog!!.dismiss()
    }
}
