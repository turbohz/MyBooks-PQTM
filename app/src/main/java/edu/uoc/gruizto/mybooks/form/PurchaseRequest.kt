package edu.uoc.gruizto.mybooks.form

import android.net.Uri

class PurchaseRequest(private val url:String) {

    private val requiredFields = listOf("name", "num", "date")

    val isValid:Boolean
        get() {

            var isValid = true
            val uri:Uri = Uri.parse(url)

            // Validate parameters

            for (key in requiredFields) {
                val value = uri.getQueryParameter(key) ?: ""
                isValid = isValid && value != ""
            }

            return isValid
        }
}