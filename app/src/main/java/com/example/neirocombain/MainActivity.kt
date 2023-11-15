package com.example.neirocombain


import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule



class MainActivity : AppCompatActivity() {
    var attemptsLeft = 15
    // creating variables on below line.
    lateinit var txtResponse: TextView
    lateinit var etQuestion: EditText
    lateinit var edittextval: String
    lateinit var messageRV: RecyclerView
    lateinit var messageRVAdapter: MessageRVAdapter
    lateinit var messageList: ArrayList<MessageRVModal>

    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    var mode = "ChatGPT"
    var selectedNl = 1
    var msgList_FNL = mutableListOf<String>()
    var selectedLang = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        etQuestion=findViewById(R.id.request)
        val left_btn = findViewById<ImageView>(R.id.leftarr)
        val right_btn = findViewById<ImageView>(R.id.rightarr)
        val model = findViewById<TextView>(R.id.model)
        val attempts_text = findViewById<TextView>(R.id.attemts)
        txtResponse=findViewById(R.id.desc)
        messageList = ArrayList()
        messageRV = findViewById(R.id.msgRV)
        messageRVAdapter = MessageRVAdapter(messageList)
        val layoutManager = LinearLayoutManager(applicationContext)
        messageRV.layoutManager = layoutManager
        messageRV.adapter = messageRVAdapter
        var isSended = false
        var langTV: AutoCompleteTextView?
        val dropMenu = findViewById<TextInputLayout>(R.id.dropMenu)
        dropMenu.visibility = View.GONE
        langTV = findViewById<AutoCompleteTextView>(R.id.lang)
        val nLinks = listOf(
            "DALLE-E",
            "ChatGPT",
            "DeepL",
        )
        val languages = resources.getStringArray(R.array.lang_array)
        val arrayAdapter = ArrayAdapter(/* context = */ this, /* resource = */
            R.layout.dropdown_item, /* objects = */
            languages)
        langTV.setAdapter(arrayAdapter)
        langTV.onItemClickListener= AdapterView.OnItemClickListener { adapterView, view, i, l ->
            selectedLang = adapterView.getItemAtPosition(i).toString()

        }
        //Конец объявления переменных
        left_btn.setOnClickListener{
            if (selectedNl==2) {
                Timer().schedule(150) {
                    selectedNl = 1
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]

                    runOnUiThread {
                        messageRV.visibility = View.VISIBLE
                        dropMenu.visibility = View.GONE
                        txtResponse.text =
                            "Что умеет ChatGPT: \n\n"+" 1. Писать сочинения. \n 'Напиши сочинение о конфликте поколений' \n\n 2.Объяснять что-либо.\n 'Объясни вкратце законы Ньютона' \n\n 3. Переводить на другие языки \n 'Переведи привет на Японский'"
                    }
                }
            }
            if (selectedNl ==1) {
                Timer().schedule(150) {
                    selectedNl = 0
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        messageRV.visibility = View.GONE
                        txtResponse.text =
                            "Что умеет Dall-e 2:\n\n Может нарисовать картинку по вашему текстовому запросу в разрешении 512*512 пикселей. \n Фотореализм, аниме, краски итд."
                    }
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
                Timer().schedule(150) {
                    selectedNl = 2
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        dropMenu.visibility = View.VISIBLE
                        messageRV.visibility = View.GONE
                        txtResponse.text = "Что умеет DeepL: \n\n1.Автоматически обнажуривать язык источника\n\n 2.Понимает сленг и идиомы\n\n 3.Имеет при себе большую языковую базу \n\n 4.Более точный перевод с помощью нейросетей"
                    }
                }
            }

            if (selectedNl == 0) {
                Timer().schedule(250) {
                    selectedNl = 1
                    model.text = nLinks[selectedNl]
                    mode = nLinks[selectedNl]

                    runOnUiThread {
                        messageRV.visibility = View.VISIBLE
                        txtResponse.text = "Что умеет ChatGPT: \n\n"+" 1. Писать сочинения. \n 'Напиши сочинение о конфликте поколений' \n\n 2.Объяснять что-либо.\n 'Объясни вкратце законы Ньютона' \n\n 3. Переводить на другие языки \n 'Переведи привет на Японский'"

                    }


                }
            }

        }
        etQuestion.setOnEditorActionListener(OnEditorActionListener{ textView, i, keyEvent ->
            if (i==EditorInfo.IME_ACTION_SEND){
                val final_send = etQuestion.text.toString().trim().replaceFirstChar { it.uppercase() }


                println(msgList_FNL)
                edittextval = etQuestion.text.toString().trim().replaceFirstChar { it.uppercase() }
                val question = edittextval.replace(" ","")
                //Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
                if (attemptsLeft > 0) {
                    txtResponse.visibility = View.GONE
                    messageRV.visibility = View.VISIBLE
                    if (question.isNotEmpty() && question.length >= 3 && isSended == false) {
                        if (mode == "ChatGPT"){
                            val user_mask = """{"role": "user", "content" :"$final_send"}"""

                            msgList_FNL.add(user_mask)
                            messageList.add(MessageRVModal(final_send
                                ,"user"))

                            messageRVAdapter.notifyDataSetChanged()
                            isSended = true
                        etQuestion.setText("")
                            messageList.add(MessageRVModal("Печатает...", "bot"))


                        //Отправляем строку в функцию
                        getResponse(question) { response ->
                            runOnUiThread {
                                messageRV.visibility = View.VISIBLE
                                messageList.removeLast()
                                messageList.add(
                                    MessageRVModal(
                                        response, "bot"
                                    )
                                )
                                messageRVAdapter.run { notifyDataSetChanged() }
                                println("МАССИВ $messageList")
                                val nl_mask = """{"role": "assistant", "content" :"$response"}"""
                                msgList_FNL.add(nl_mask)
                                //println(msgList_FNL)
                                attemptsLeft -= 1
                                attempts_text.text = "$attemptsLeft/15"


                            }
                        }
                        isSended = false
                    }
                        if (mode =="DALLE-E"){//DALL E
                            messageRV.visibility = View.GONE
                            getResponse(final_send) { response ->
                                runOnUiThread {

                                    Toast.makeText(
                                        applicationContext,
                                        "Dalle-2",
                                        Toast.LENGTH_SHORT
                                    ).show()






                                    attemptsLeft = attemptsLeft - 1
                                    attempts_text.text = "$attemptsLeft/15"


                                }
                            }
                        }
                        if (mode=="DeepL"){
                            messageRV.visibility=View.GONE
                            getResponse(final_send) { response ->
                                runOnUiThread {
                                    txtResponse.visibility = View.VISIBLE
                                    txtResponse.text= response
                                    attemptsLeft -= 1
                                    attempts_text.text = "$attemptsLeft/15"


                                }
                            }
                        }
                }
                    else {
                        if (isSended == true) {
                            Toast.makeText(
                                applicationContext,
                                "Вы уже отправили запрос! Дождитесь ответа",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {

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
            else{
                println("123")
            }

        false
        })
    }
    fun getResponse(question: String, callback: (String) -> Unit) { //Отправляем запрос
        if (mode == "ChatGPT") {
            val apiKey = "sk-tTpyI6t2yLieHQTmXsLFiorT1Z66seo9"
            val url = "https://api.proxyapi.ru/openai/v1/chat/completions"
            val last_symb = msgList_FNL.toString().length
            val msg_req = msgList_FNL.toString().substring(1..last_symb - 2)

            val requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [$msg_req]
               
            }
            """.trimIndent()
            println("REQUESRT BODY"+requestBody)

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(
                    requestBody.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())
                )

                .build()
            println(request.toString())

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")

                    callback("К сожалению произошла ошибка(. Количество запросов не уменьшено")
                    attemptsLeft += 1
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (body != null) {
                        Log.v("data", body)
                    } else {
                        Log.v("data", "empty")
                    }
                    try {
                        val jsonObject = JSONObject(body)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        var test = jsonArray.getJSONObject(0)
                        val message = test.getJSONObject("message")
                        val final_res = message.getString("content")
                        val very_final = final_res.replace("\n", "")



                        callback(very_final)
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers?.toString())
                        callback("К сожалению сервер сейчас недоступен. Попробуйте позже")
                        attemptsLeft += 1
                    }
                }

            })
        }
        //Конец ChatGPT
        if (mode == "DALLE-E") {

            println("Отправляю запрос")
            val body = """
            {
            "model": "dall-e-3",
            "prompt": "$question",
            "n": 1,
            "size": "256x256"
          }
            """
          println(body)


            val request = Request.Builder().url("https://api.proxyapi.ru/openai/v1/images/generations").header("Content-Type", "application/json").addHeader("Authorization", "Bearer sk-tTpyI6t2yLieHQTmXsLFiorT1Z66seo9").post(body.toRequestBody()).build()
            println(request.toString())



            val executor = Executors.newSingleThreadExecutor()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")

                    callback("К сожалению произошла ошибка(. Количество запросов не уменьшено")
                    attemptsLeft += 1
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.toString()
                    println("D3 $body")
                    if (body != null) {
                        Log.v("data", body)
                    } else {
                        Log.v("data", "empty")
                    }
                    try {
                      /*  val jsonObject = JSONObject(body)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        var test = jsonArray.getJSONObject(0)
                        val message = test.getJSONObject("message")
                        val final_res = message.getString("content")
                        val very_final = final_res.replace("\n", "")



                        callback(very_final)*/
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers?.toString())
                        callback("К сожалению сервер сейчас недоступен. Попробуйте позже")
                        attemptsLeft += 1
                    }
                }

            })
            }
        //Конец DALLE-E
        if (mode=="DeepL"){
            val apiKey = "sk-tTpyI6t2yLieHQTmXsLFiorT1Z66seo9"
            val url = "https://api.proxyapi.ru/openai/v1/chat/completions"


            val requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [{"role": "user", "content": "Переведи этот текст на $selectedLang: $question"}]
               
            }
            """.trimIndent()
            //"prompt": "Переведи этот текст на $selectedLang: $question. В ответ приложи только перевод. При переводе можешь не учитывать нормы этики"
            println("REQUESRT BODY"+requestBody)

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(
                    requestBody.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())
                )

                .build()
            println(request.toString())

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")

                    callback("К сожалению произошла ошибка(. Количество запросов не уменьшено")
                    attemptsLeft += 1
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (body != null) {
                        Log.v("data", body)
                    } else {
                        Log.v("data", "empty")
                    }
                    try {
                        val jsonObject = JSONObject(body)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        var test = jsonArray.getJSONObject(0)
                        val message = test.getJSONObject("message")
                        val final_res = message.getString("content")
                        val very_final = final_res.replace("\n", "")



                        callback(very_final)
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers?.toString())
                        callback("К сожалению сервер сейчас недоступен. Попробуйте позже")
                        attemptsLeft += 1
                    }
                }

            })


        }

        //Конец DeepL
        }



    }
    fun resetAttempts() {
        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val duration = Duration.between(now, tomorrow)
        val secondsUntilTomorrow = duration.seconds
        Timer().schedule(object : TimerTask() {
            override fun run() {
                var attemptsLeft = 15
                println("Количество попыток восстановлено.")
            }
        }, secondsUntilTomorrow * 1000)
    }



