package com.example.neirocombain

import android.util.Log
import android.util.MutableInt
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

class ApiClass {
    fun getResponse(question: String, callback: (String) -> Unit, client:OkHttpClient, msgList_FNL:MutableList<String>, attemptsLeft:Int, mode: String, selectedLang:String) { //Отправляем запрос
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
                .post(requestBody.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())).build()
            println(request.toString())
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")
                    callback("К сожалению произошла ошибка. Проверьте соединение с интернетом или попробуйте позже. Количество запросов не уменьшено")
                    //attemptsLeft += 1
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
                        callback(final_res)
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers?.toString())
                        callback("К сожалению сервер сейчас недоступен. Количество запросов не уменьшено")
                        //attemptsLeft += 1
                    }
                }
            })
        }
        //Конец ChatGPT=======================================================================
        if (mode == "DALLE-E") {
            println("Отправляю запрос")
            val body = """
            {
            "model": "dall-e-3",
            "prompt": "$question",
            "n": 1,
            "size": "256x256"
          }
            """.trimIndent()
            println(body)
            val request = Request.Builder().url("https://api.proxyapi.ru/openai/v1/images/generations").header("Content-Type", "application/json").addHeader("Authorization", "Bearer sk-tTpyI6t2yLieHQTmXsLFiorT1Z66seo9").post(body.toRequestBody()).build()
            println(request.toString())
            //val executor = Executors.newSingleThreadExecutor()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")
                    callback("К сожалению произошла ошибка. Количество запросов не уменьшено")
                    //attemptsLeft += 1
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
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers?.toString())
                        callback("К сожалению сервер сейчас недоступен. Проверьте соединение с интернетом или попробуйте позже")
                        //attemptsLeft += 1
                    }
                }
            })
        }
        //Конец DALLE-E============================================================================
        if (mode=="DeepL"){
            val apiKey = "sk-tTpyI6t2yLieHQTmXsLFiorT1Z66seo9"
            val url = "https://api.proxyapi.ru/openai/v1/chat/completions"
            val requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [{"role": "user", "content": "Переведи этот текст на $selectedLang: $question"}]
            }
            """.trimIndent()
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody.trimIndent().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            println(request.toString())
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")
                    callback("К сожалению произошла ошибка(. Количество запросов не уменьшено")
                    //attemptsLeft += 1
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
                        callback(final_res)
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers?.toString())
                        callback("К сожалению сервер сейчас недоступен. Попробуйте позже")
                        //attemptsLeft += 1
                    }
                }
            })
        }
        //Конец DeepL
    }
}