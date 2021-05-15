package com.example.simpleapicalldemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.simpleapicalldemo.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CallAPILoginAsyncTask("KM", "1234").execute()
    }

    // AsyncTask는 coroutine으로 인해 Deprecated.
    private inner class CallAPILoginAsyncTask(val username: String, val password: String): AsyncTask<Any, Void, String>(){
        private lateinit var customProgressDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null
            try {
                val url  = URL("https://run.mocky.io/v3/6364c0db-cb36-4820-b1c0-da6cb585b24e")
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                // 리다이렉션 불가
                connection.instanceFollowRedirects = false
                // 헤더 설정
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept", "application/json")
                // 캐시설정
                connection.useCaches = false
                // 데이터 설정
                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                jsonRequest.put("username", username)
                jsonRequest.put("password", password)

                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

                val httpResult: Int = connection.responseCode
                if(httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    try{
                        while(reader.readLine().also { line = it } != null){
                            stringBuilder.append(line + "\n")
                        }
                    }catch (e: IOException){
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e: IOException){
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                }else{
                    result = connection.responseMessage
                }
            }catch (e: SocketTimeoutException){
                result = "Connection Time Out"
            }catch (e: Exception){
                result = "I don't Know?"
            }finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            Log.i("JSON RESPONSE RESULT:","$result")

            // result 값을 자바 리플렉션으로 ResponseData에 사상Mapping시킴
            val responseData = Gson().fromJson(result, ResponseData::class.java)
            // 리플렉션으로 줘서 바로 접근 가능한듯
            Log.i("Message",responseData.message)
            Log.i("User Id","${responseData.user_id}")
            Log.i("Name",responseData.name)
            Log.i("Email",responseData.email)
            Log.i("Mobile","${responseData.mobile}}")
            Log.i("Profile Completed","${responseData.profile_details.is_profile_completed}}")
            Log.i("Rating","${responseData.profile_details.rating}}")
            for (item in responseData.data_list.indices){
                Log.i("Value","${responseData.data_list[item]}")
                Log.i("ID","${responseData.data_list[item].id}")
                Log.i("Value","${responseData.data_list[item].value}")
            }
        }
        private fun showProgressDialog(){
            customProgressDialog = Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.show()
        }

        private fun cancelProgressDialog(){
            customProgressDialog.dismiss()
        }
    }
}