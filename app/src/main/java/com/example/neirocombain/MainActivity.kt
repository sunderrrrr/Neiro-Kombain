package com.example.neirocombain


import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import java.time.Duration
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    var attemptsLeft = 15
    // creating variables on below line.
    lateinit var txtResponse: TextView
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
        val left_btn = findViewById<ImageView>(R.id.leftarr)
        val right_btn = findViewById<ImageView>(R.id.rightarr)
        val model = findViewById<TextView>(R.id.model)
        txtResponse=findViewById<TextView>(R.id.result)
        var isSended = false
        var isFirstQuestInQuery = true
        txtResponse.movementMethod = ScrollingMovementMethod()
        var selectedNl = 1
        var mode = "GPT"
        val delay = 500
        var nLinks = listOf(
            "Kandinsky",
            "ChatGPT",
            "GigaChat",
        )
        left_btn.setOnClickListener{
            if (selectedNl==2) {
                Timer().schedule(250) {
                    selectedNl = 1
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]

                }
            }
            if (selectedNl ==1) {
                Timer().schedule(250) {
                    selectedNl = 0
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]

                }
            }
            if (selectedNl == 0) {
                Toast.makeText(
                    applicationContext,
                    "Увы, дальше ничего нет",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        right_btn.setOnClickListener{
            if (selectedNl==2) {
                Toast.makeText(
                    applicationContext,
                    "Увы, дальше ничего нет",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (selectedNl ==1) {
                Timer().schedule(250) {
                    selectedNl = 2
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]

                }
            }

            if (selectedNl == 0) {
                Timer().schedule(250) {
                    selectedNl = 1
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]


                }
            }

        }
        btnSubmit.setOnClickListener {
                edittextval = etQuestion.text.toString().trim().replaceFirstChar { it.uppercase() }
                println(edittextval)
                val question = edittextval.replace(" ","")
                //Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
                if (attemptsLeft > 0) {
                    if (question.isNotEmpty() && question.length >= 3 && isSended == false) {
                        isSended = true
                        attemptsLeft= attemptsLeft-1
                        //count_str.text = "${attemptsLeft.toString()}/15"
                        load.visibility = View.VISIBLE//Отправляем строку в функцию
                        getResponse(question) { response ->
                            runOnUiThread {
                                load.visibility = View.GONE
                                txtResponse.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                                if (isFirstQuestInQuery == true) {
                                    txtResponse.text = "Вы: $edittextval\n \nChatGPT: $response\n"
                                    isSended = false
                                    Toast.makeText(
                                        applicationContext,
                                        "Ура! Это ваш первый запрос",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isFirstQuestInQuery = false
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "В прошлый раз было " + txtResponse.text.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    txtResponse.text =
                                        txtResponse.text.toString() + "\nВы: $edittextval\n \nChatGPT: $response\n"
                                    isSended = false
                                }

                            }
                        }
                    } else {
                        if (isSended == true) {
                            Toast.makeText(
                                applicationContext,
                                "Вы уже отправили запрос! Дождитесь ответа",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            load.visibility = View.GONE
                            Toast.makeText(
                                applicationContext,
                                "Вы не ввели запрос или он слишком короткий!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
                else{
                    Toast.makeText(
                        applicationContext,
                        "Количество попыток исчерпано. Посмотрите рекламу или приходите завтра",
                        Toast.LENGTH_SHORT
                    ).show()
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
    fun resetAttempts() {
        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val duration = Duration.between(now, tomorrow)
        val secondsUntilTomorrow = duration.seconds
        Timer().schedule(object : TimerTask() {
            override fun run() {
                attemptsLeft = 15
                println("Количество попыток восстановлено.")
            }
        }, secondsUntilTomorrow * 1000)
    }


}


