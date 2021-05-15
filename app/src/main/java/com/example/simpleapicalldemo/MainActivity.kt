package com.example.simpleapicalldemo

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleapicalldemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MyCoroutine("Km", "1234").execute()
    }

    // AsyncTask는 coroutine으로 인해 Deprecated.
    class HttpCon(private val username: String, private val password: String) {
        suspend fun connection(): String {
            val url = URL("https://run.mocky.io/v3/6364c0db-cb36-4820-b1c0-da6cb585b24e")
            // withContext(Dispatchers.IO)를 해줌으로써 IO 스레드에서 실행되게 된다.
            return withContext(Dispatchers.IO) {
                (url.openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    instanceFollowRedirects = false
                    useCaches = false

                    val writeDataOutputStream = DataOutputStream(outputStream)
                    val jsonRequest = JSONObject()
                    jsonRequest.put("username", username)
                    jsonRequest.put("password", password)

                    writeDataOutputStream.writeBytes(jsonRequest.toString())
                    writeDataOutputStream.flush()
                    writeDataOutputStream.close()

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("커넥션 성공", "성공!")
                        var text: String = inputStream.use {
                            it.reader().use { reader ->
                                reader.readText()
                            }
                        }
                        Log.d("텍스트", text)
                        text
                    } else {
                        responseMessage
                    }
                } ?: "connection fail"
            }
        }

    }

    // https://stackoverflow.com/a/58900195
    // https://developer.android.com/kotlin/coroutines
    inner class MyCoroutine(
            private val username: String,
            private val password: String) : ViewModel() {
        private lateinit var customProgressDialog: Dialog

        // 메인 스레드에서 실행, UI를 직접 다룰 수 있다.
        fun execute() = viewModelScope.launch {
            showProgressDialog()
            val result = login()
            done(result)
        }

        private suspend fun login(): String {
            var resultJson: String? = null
            resultJson = HttpCon(username, password).connection()
            return resultJson!!
        }


        private fun done(result: String?) {
            binding.tv.text = result
            cancelProgressDialog()
        }

        private fun showProgressDialog() {
            customProgressDialog = Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.show()
        }

        private fun cancelProgressDialog() {
            customProgressDialog.dismiss()
        }
    }
}