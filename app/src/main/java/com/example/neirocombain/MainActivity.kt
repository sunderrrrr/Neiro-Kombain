package com.example.neirocombain


import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // creating variables on below line.
    lateinit var txtResponse: TextView
    lateinit var idTVQuestion: TextView
    lateinit var etQuestion: EditText
    lateinit var edittextval: String
    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etQuestion=findViewById(R.id.request)
        val load = findViewById<ProgressBar>(R.id.load)
        val btnSubmit=findViewById<Button>(R.id.sumbit)
        //idTVQuestion=findViewById<TextView>(R.id.quest)
        txtResponse=findViewById<TextView>(R.id.result)
        txtResponse.movementMethod = ScrollingMovementMethod()

        btnSubmit.setOnClickListener {
                edittextval = etQuestion.text.toString().trim()
                println(edittextval)
                val question = edittextval.replace(" ","")
                //Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
                if(question.isNotEmpty() && question.length>=3){
                    txtResponse.text = ""
                    load.visibility = View.VISIBLE//Отправляем строку в функцию
                    getResponse(question) { response ->
                        runOnUiThread {
                            load.visibility = View.GONE
                            txtResponse.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                            txtResponse.text = "Вы: $edittextval.capitalize()\n \nChatGPT: $response\n"

                        }
                    }
                }
                else{
                    load.visibility = View.GONE
                    Toast.makeText(applicationContext, "Вы не ввели запрос или он слишком короткий!", Toast.LENGTH_SHORT).show()

                }
        }
    }
    fun getResponse(question: String, callback: (String) -> Unit){ //Отправляем запрос
        val apiKey="sk-tTpyI6t2yLieHQTmXsLFiorT1Z66seo9"
        val url="https://api.proxyapi.ru/openai/v1/chat/completions"

        val requestBody="""
            {
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "user", "content": "$question"}],
            "temperature": 0.0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error","API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                if (body != null) {
                    Log.v("data",body)
                }
                else{
                    Log.v("data","empty")
                }
                val jsonObject= JSONObject(body)
                val jsonArray=jsonObject.getJSONArray("choices")
                println("JSON ARRAY: $jsonArray")
                var test = jsonArray.getJSONObject(0)
                println("TEST $test")
                val message = test.getJSONObject("message")
                val final_res = message.getString("content").toString()
                println(final_res)
                callback(final_res)

            }
        })
    }


}