package com.msah.mobilepos.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import com.msah.mobilepos.R

object AlertMessageViewer {

    fun showAlertDialogMessage(context: Context, message: String){

        var newMessage = message
        if(message.isDigitsOnly()){
            newMessage = context.getString(message.toInt())
        }

        AlertDialog.Builder(context)
            .setMessage(newMessage)
            .setPositiveButton(context.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .show()

    }

}