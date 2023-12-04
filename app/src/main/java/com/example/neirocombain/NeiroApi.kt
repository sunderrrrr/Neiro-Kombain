package com.example.neirocombain

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NeiroApi {
    fun getResponse(question: String, client: OkHttpClient, msgList_FNL:MutableList<String>, mode: String, url_api:String,apiKey:String,vararg attemptsLeft: Int,selectedLang:String,callback: (String) -> Unit) { //Отправляем запрос
        if (mode == "ChatGPT") {

            val last_symb = msgList_FNL.toString().length
            val msg_req = msgList_FNL.toString().substring(1..last_symb - 2)
            val requestBody = """
            {
                "model": "gpt-3.5-turbo-1106",
                "messages": [$msg_req]     
            }
            """.trimIndent()
            println("REQUESRT BODY$requestBody")
            val request = Request.Builder()
                .url(url_api +"v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())).build()
            println(request.toString())
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")
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
                        val jsonObject = JSONObject(body!!)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val test = jsonArray.getJSONObject(0)
                        val message = test.getJSONObject("message")
                        val final_res = message.getString("content")
                        callback(final_res)
                    } catch (e: JSONException) {
                        println(body)
                       // attemptsLeft += 1
                    }
                }
            })
        }
        //Конец ChatGPT=======================================================================
        if (mode == "DALLE-E") {
            val JSONbody = JSONObject()
            try{
                JSONbody.put("model", "dall-e-2")
                JSONbody.put("prompt", question)
                JSONbody.put("size", "256x256")
            }catch(e:Exception) {
                e.printStackTrace()
            }
            val requestBody: RequestBody = JSONbody.toString().toRequestBody("application/json".toMediaType())
            val request: Request = Request.Builder().url(url_api+"v1/images/generations").header("Authorization", "Bearer $apiKey").post(requestBody).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val jsonObject= JSONObject(response.body!!.string())
                        val imgUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url")
                        println(imgUrl)
                        callback(imgUrl)
                     //   attemptsLeft = 0
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            })
        }
        //Конец DALLE-E============================================================================
        if (mode=="DeepL"){

            val requestBody = """
            {
                "model": "gpt-3.5-turbo-1106",
                "messages": [{"role": "user", "content": "Переведи этот текст на $selectedLang: $question"}]
            }
            """.trimIndent()
            val request = Request.Builder()
                .url(url_api +"v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody.trimIndent().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            println(request.toString())
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("API failed")

                   // attemptsLeft += 1
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (body != null) {
                        Log.v("data", body)
                    } else {
                        Log.v("data", "empty")
                    }
                    try {
                        val jsonObject = JSONObject(body!!)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val test = jsonArray.getJSONObject(0)
                        val message = test.getJSONObject("message")
                        val final_res = message.getString("content")
                        callback(final_res)
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers.toString())

                        //attemptsLeft += 1
                    }
                }
            })
        }
        //Конец DeepL
    }
}