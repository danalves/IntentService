package com.example.logonrm.intentservice

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.support.v4.os.ResultReceiver
import android.text.TextUtils
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by logonrm on 17/02/2018.
 */
class DownloadService : IntentService(DownloadService::class.java.name) {
    companion object {
        val STATUS_RUNNING = 0
        val STATUS_FINISHED = 1
        val STATUS_ERROR = 2
    }

    override fun onHandleIntent(intent: Intent?) {
        val receiver = intent!!.getParcelableExtra<ResultReceiver>("receiver")
        val url = intent.getStringExtra("url")
        val bundle = Bundle()

        if(!TextUtils.isEmpty(url)){
            receiver.send(STATUS_RUNNING, Bundle.EMPTY)
            try {
                val results = downloadData(url)

                if(null != results && results.isNotEmpty()){
                    bundle.putStringArray("result", results.toTypedArray())
                    receiver.send(STATUS_FINISHED, bundle)
                }
            } catch (e: Exception){
                bundle.putString(Intent.EXTRA_TEXT, e.toString())
                receiver.send(STATUS_ERROR, bundle)
            }
        }
        this.stopSelf()
    }

    @Throws(IOException::class, DownloadException::class)
    private fun downloadData(requestUrl: String): List<String?>{
        var inputStream: InputStream?
        var urlConnection: HttpURLConnection?
        var url = URL(requestUrl)

        urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("Accept", "application/json")
        urlConnection.requestMethod = "GET"

        val statusCode = urlConnection.responseCode

        if (statusCode == 200) {
            inputStream = BufferedInputStream(urlConnection.inputStream)
            val response = convertInputStreamToString(inputStream)
            val result = parseResult(response)
            return result.toList()
        } else{
            throw DownloadException("Falha na busca dos dados")
        }
    }


}