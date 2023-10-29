package com.example.neirocombain


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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
        //  val title = findViewById<TextView>(R.id.title)
        //val text = "<font color=#6314F4>Neiro</font><font color=#FFFFFF>.Combain</font>"
        //title.text = Html.fromHtml(text)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etQuestion=findViewById(R.id.request)

        val btnSubmit=findViewById<Button>(R.id.sumbit)
        idTVQuestion=findViewById<TextView>(R.id.quest)
        txtResponse=findViewById<TextView>(R.id.result)
        txtResponse.movementMethod = ScrollingMovementMethod()

        btnSubmit.setOnClickListener {


                // setting response tv on below line.

                txtResponse.text = "ChatGPT: Печатает..."

                // validating text
                edittextval = etQuestion.text.toString().trim()
                println(edittextval)
                val question = edittextval.replace(" ","")
                val stringBuilder = StringBuilder()
                //Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
                if(question.isNotEmpty()){
                    getResponse(question) { response ->
                        runOnUiThread {

                            Thread{
                                for (letter in response){
                                    stringBuilder.append(letter)
                                    Thread.sleep(25)
                                    runOnUiThread{
                                        txtResponse.text = stringBuilder.toString()
                                    }
                                }
                            }.start()
                           // txtResponse.text = response
                        }
                    }
                }
        }
    }
    fun getResponse(question: String, callback: (String) -> Unit){

        // setting text on for question on below line.
        idTVQuestion.text = "Ваш запрос: $edittextval"
        //etQuestion.setText("")

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