@file:Suppress("SpellCheckingInspection")

package com.example.neirocombain



import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequest.Builder
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.instream.MobileInstreamAds
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
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
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    private var DEBUG_MODE = false//ВЫКЛ ВКЛ ДЕБАГ
    val url_api = "https://api.proxyapi.ru/openai/" // v1/chat/completions
    val apiKey = "sk-tTpyI6t2yLieHQTmXsLFiorT1Z66seo9"
    val reward_ad_id = "R-M-4088559-2"
    val banner_ad_id = "R-M-4088559-1"
    lateinit var txtResponse: TextView
    private var rewardedAd: RewardedAd? = null
    private var rewardedAdLoader: RewardedAdLoader? = null
    lateinit var etQuestion: EditText
    lateinit var attempts_text: TextView
    lateinit var edittextval: String
    lateinit var messageRV: RecyclerView
    lateinit var image: ImageView
    lateinit var messageRVAdapter: MessageRVAdapter
    lateinit var messageList: ArrayList<MessageRVModal>
    lateinit var DeepLList: ArrayList<MessageRVModal>
    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
    var mode = "ChatGPT"
    var selectedNl = 1
    var msgList_FNL = mutableListOf<String>()
    var selectedLang = ""
    var attemptsLeft: Int = 0
    val connectionChecker = InternetConnection()
    var was_recently_seen = false
    val nLinks = listOf("DALLE-E", "ChatGPT", "DeepL")
    var isSended = false
    var isFirstGPT = true
    var isFirstDeepL = true
    var isFirstDalle = true
    val JSON: MediaType = "application/json".toMediaType()
    var pref: SharedPreferences? = getSharedPreferences("shared", Context.MODE_PRIVATE)
    val Saver = SaveData()
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")


    override fun onCreate(savedInstanceState: Bundle?) {
        //ИНИЦИАЛИЗАЦИЯ=========================================
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etQuestion=findViewById(R.id.request)
        val left_btn = findViewById<ImageView>(R.id.leftarr)
        val right_btn = findViewById<ImageView>(R.id.rightarr)
        val model = findViewById<TextView>(R.id.model)
        val banner = findViewById<BannerAdView>(R.id.banner)
        val layoutManager = LinearLayoutManager(applicationContext)
        val mainLO = findViewById<LinearLayout>(R.id.main)
        image = findViewById(R.id.image)
        val langTV = findViewById<AutoCompleteTextView>(R.id.lang)
        val dropMenu = findViewById<TextInputLayout>(R.id.dropMenu)
        txtResponse=findViewById(R.id.desc)
        attempts_text = findViewById(R.id.attemts)
        messageRV = findViewById(R.id.msgRV)
        attemptsLeft = pref?.getInt("attempts", 3)!!
        was_recently_seen = pref?.getBoolean("wrs", false)!!
        attempts_text.text = attemptsLeft.toString() +"/3"
        messageList = ArrayList()
        DeepLList = ArrayList()
        messageRV.layoutManager = layoutManager
        messageRVAdapter = MessageRVAdapter(messageList)
        messageRV.adapter = messageRVAdapter
        //Update.check(this, was_recently_seen)
        //ВЫБОР ЯЗЫКОВ

        val languages = resources.getStringArray(R.array.lang_array)
        val arrayAdapter = ArrayAdapter(/* context = */ this, /* resource = */ R.layout.dropdown_item, /* objects = */ languages)
        langTV.setAdapter(arrayAdapter)
        langTV.onItemClickListener= AdapterView.OnItemClickListener { adapterView, view, i, l ->
            selectedLang = adapterView.getItemAtPosition(i).toString()
        }
        dropMenu.visibility = View.GONE
        //КОНЕЦ ИНИЦИАЛИЗАЦИИ===================================================
        //БЛОК РЕКЛАМЫ===========================================================
        rewardedAdLoader = RewardedAdLoader(this).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    this@MainActivity.rewardedAd = rewardedAd
                }
                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                }
            })
        }
        loadRewardedAd()
        MobileAds.initialize(this){
        MobileInstreamAds.setAdGroupPreloading(true)
        MobileAds.enableLogging(true)
        banner.setAdUnitId(banner_ad_id)// BANER
        banner.setAdSize(BannerAdSize.fixedSize(this, 320, 80))
            val adRequest: AdRequest = Builder().build()

        banner.run {
            println(adRequest)
            loadAd(adRequest) } }
        println("Баннер инициализирован")
        //КОНЕЦ БЛОКА РЕКЛАМЫ====================

        //КНОПКИ НАВИГАЦИИ================================
        left_btn.setOnClickListener{
            if (selectedNl==2) {
                Timer().schedule(150) {
                    selectedNl = 1
                    println("ВТОРИЧНАЯ ИНИЦИАЛИЗАЦИЯ АДАПТЕРА В ЖПТ")
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        model.alpha = 0f
                        mainLO.alpha = 0f
                        attempts_text.alpha = 0f
                        model.text = nLinks[selectedNl]
                        messageRV.visibility = View.VISIBLE
                        dropMenu.visibility = View.GONE
                        messageRVAdapter = MessageRVAdapter(messageList)
                        messageRV.adapter = messageRVAdapter
                        if(isFirstGPT){txtResponse.visibility = View.VISIBLE}
                        else{txtResponse.visibility = View.GONE}
                        image.visibility=View.GONE
                        txtResponse.text = "Что умеет ChatGPT: \n\n"+" 1. Писать сочинения. \n 'Напиши сочинение о конфликте поколений' \n\n 2.Объяснять что-либо.\n 'Объясни вкратце законы Ньютона' \n\n 3. Переводить на другие языки \n 'Переведи привет на Японский'"
                        mainLO.animate().alpha(1f).duration = 500
                        model.animate().alpha(1f).duration = 500
                        attempts_text.animate().alpha(1f).duration = 500
                    }
                }
            }
            if (selectedNl ==1) {
                Timer().schedule(150) {
                    selectedNl = 0
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        mainLO.alpha = 0f
                        model.alpha = 0f
                        attempts_text.alpha = 0f
                        model.text = nLinks[selectedNl]
                        messageRV.visibility = View.GONE
                        if(isFirstDalle) {
                            txtResponse.visibility = View.VISIBLE
                            txtResponse.text =
                                "Что умеет Dall-e 2:\n\n Может нарисовать картинку по вашему текстовому запросу в разрешении 512*512 пикселей. \n Фотореализм, аниме, краски итд. \n\nСценарии применения:\n Референсы для творческих работ, обложка альбома, обои и так далее"
                        }else{
                            image.visibility = View.VISIBLE
                            txtResponse.visibility = View.GONE
                        }
                        dropMenu.visibility= View.GONE
                        image.visibility=View.VISIBLE
                        attempts_text.animate().alpha(1f).duration = 500
                        mainLO.animate().alpha(1f).duration = 500
                        model.animate().alpha(1f).duration = 500
                    }
                }
            }
            if (selectedNl == 0) {
                Timer().schedule(150) {
                    selectedNl = 2
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        mainLO.alpha = 0f
                        model.alpha = 0f
                        attempts_text.alpha = 0f
                        model.text = nLinks[selectedNl]
                        dropMenu.visibility = View.VISIBLE
                        if(isFirstDeepL){
                            txtResponse.visibility = View.VISIBLE}
                        else{txtResponse.visibility = View.GONE}
                        println("ВТОРИЧНАЯ ИНИЦИАЛИЗАЦИЯ АДАПТЕРА В ДИПЛ")
                        messageRVAdapter = MessageRVAdapter(DeepLList)
                        messageRV.adapter = messageRVAdapter
                        image.visibility=View.GONE
                        txtResponse.text = "Что умеет DeepL: \n\n1.Автоматически обнажуривать язык источника\n\n 2.Понимает сленг и идиомы\n\n 3.Имеет при себе большую языковую базу \n\n 4.Более точный перевод с помощью нейросетей"
                        mainLO.animate().alpha(1f).duration = 500
                        model.animate().alpha(1f).duration = 500
                        attempts_text.animate().alpha(1f).duration = 500
                    }
                }
            }

        }
        right_btn.setOnClickListener{
            if (selectedNl==2) {
                Timer().schedule(150) {
                    selectedNl = 0
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        mainLO.alpha = 0f
                        model.alpha = 0f
                        attempts_text.alpha = 0f
                        model.text = nLinks[selectedNl]
                        messageRV.visibility = View.GONE
                        if(isFirstDalle) {
                            txtResponse.visibility = View.VISIBLE
                            txtResponse.text =
                                "Что умеет Dall-e 2:\n\n Может нарисовать картинку по вашему текстовому запросу в разрешении 512*512 пикселей. \n Фотореализм, аниме, краски итд. \n\nСценарии применения:\n Референсы для творческих работ, обложка альбома, обои и так далее"
                        }else{
                            image.visibility = View.VISIBLE
                            txtResponse.visibility = View.GONE
                        }
                        dropMenu.visibility= View.GONE
                        image.visibility=View.VISIBLE
                        attempts_text.animate().alpha(1f).duration = 500
                        mainLO.animate().alpha(1f).duration = 500
                        model.animate().alpha(1f).duration = 500
                    }
                }
            }
            if (selectedNl ==1) {
                Timer().schedule(150) {
                    selectedNl = 2
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        mainLO.alpha = 0f
                        model.alpha = 0f
                        attempts_text.alpha = 0f
                        model.text = nLinks[selectedNl]
                        dropMenu.visibility = View.VISIBLE
                        if(isFirstDeepL){ txtResponse.visibility = View.VISIBLE}
                        else{txtResponse.visibility = View.GONE}
                        println("ВТОРИЧНАЯ ИНИЦИАЛИЗАЦИЯ АДАПТЕРА В ДИПЛ")
                        messageRVAdapter = MessageRVAdapter(DeepLList)
                        messageRV.adapter = messageRVAdapter
                        image.visibility=View.GONE
                        txtResponse.text = "Что умеет DeepL: \n\n1.Автоматически обнажуривать язык источника\n\n 2.Понимает сленг и идиомы\n\n 3.Имеет при себе большую языковую базу \n\n 4.Более точный перевод с помощью нейросетей"
                        mainLO.animate().alpha(1f).duration = 500
                        model.animate().alpha(1f).duration = 500
                        attempts_text.animate().alpha(1f).duration = 500
                    }
                }
            }
            if (selectedNl == 0) {
                Timer().schedule(150) {
                    selectedNl = 1
                    mode = nLinks[selectedNl]
                    runOnUiThread {
                        mainLO.alpha = 0f
                        model.alpha = 0f
                        attempts_text.alpha = 0f
                        model.text = nLinks[selectedNl]
                        messageRV.visibility = View.VISIBLE
                        txtResponse.visibility = View.GONE
                        messageRVAdapter=MessageRVAdapter(messageList)
                        messageRV.adapter=messageRVAdapter
                        if(isFirstGPT){txtResponse.visibility = View.VISIBLE}
                        else{txtResponse.visibility = View.GONE}
                        image.visibility=View.GONE
                        txtResponse.text = "Что умеет ChatGPT: \n\n" + " 1. Писать сочинения. \n 'Напиши сочинение о конфликте поколений' \n\n 2.Объяснять что-либо.\n 'Объясни вкратце законы Ньютона' \n\n 3. Переводить на другие языки \n 'Переведи привет на Японский'"
                        mainLO.animate().setDuration(1000).alpha(1f)
                        model.animate().alpha(1f).duration = 500
                        attempts_text.animate().alpha(1f).duration = 500
                    }
                }
            }
        }
        //КОНЕЦ КНОПОК НАВИГАЦИИ================================

        //Блок отправки сообщений========================
        var isConnected = connectionChecker.checkConnection(this)
        etQuestion.setOnEditorActionListener(OnEditorActionListener{ textView, i, keyEvent ->
            if (i==EditorInfo.IME_ACTION_SEND && !DEBUG_MODE) {
                isConnected = connectionChecker.checkConnection(this)
                val final_send = etQuestion.text.toString().trim().replaceFirstChar { it.uppercase() }
                edittextval = etQuestion.text.toString().trim().replaceFirstChar { it.uppercase() }
                val question = edittextval.replace(" ", "")
                if (attemptsLeft > 0 && isConnected) {
                    txtResponse.visibility = View.GONE
                    messageRV.visibility = View.VISIBLE
                    isConnected = connectionChecker.checkConnection(this)
                    if (question.isNotEmpty() && question.length >= 5 && !isSended && isConnected) {
                        if (mode == "ChatGPT") {
                            val user_mask = """{"role": "user", "content" :"$final_send"}"""
                            msgList_FNL.add(user_mask) //Сообщение для апи
                            messageList.add(MessageRVModal(final_send, "user"))//Сообщение для чата
                            messageRVAdapter.notifyDataSetChanged()
                            isSended = true
                            isFirstGPT = false
                            etQuestion.setText("")
                            messageList.add(MessageRVModal("Печатает...", "bot"))
                            //Отправляем строку в функцию
                            getResponse(question) { response ->
                                runOnUiThread {
                                    messageRV.visibility = View.VISIBLE
                                    messageList.removeLast()
                                    var response_to_list = response.replace("\n", "")
                                    response_to_list = response_to_list.replace("\"", "'")
                                    messageList.add(MessageRVModal(response, "bot"))
                                    messageRVAdapter.run { notifyDataSetChanged() }
                                    messageRV.smoothScrollToPosition(messageRVAdapter.itemCount)
                                    println("МАССИВ $messageList")
                                    val nl_mask = """{"role": "assistant", "content" :"$response_to_list"}"""
                                    msgList_FNL.add(nl_mask)
                                    attemptsLeft -= 1
                                    attempts_text.text = "$attemptsLeft/3"
                                    Saver.Save(pref!!, attemptsLeft, was_recently_seen)
                                }
                                //КОНЕЦ UI ПОТОКА
                            }
                            isSended = false
                        }
                        if (mode == "DALLE-E") {//DALL E
                            isFirstDalle = false
                            messageRV.visibility = View.GONE
                            Toast.makeText(applicationContext, "В разработке", Toast.LENGTH_SHORT).show()
                            image.visibility = View.VISIBLE
                            getResponse(final_send) { response ->
                                DownloadImageTask123(image).execute(response)
                                attemptsLeft = 0
                                attempts_text.text = "$attemptsLeft/3"
                                showAd()
                            }
                        }
                        if (mode == "DeepL") {
                            txtResponse.visibility = View.GONE
                            isFirstDeepL = false
                            messageRV.visibility = View.VISIBLE
                            DeepLList.add(MessageRVModal(final_send, "user"))
                            messageRVAdapter.run { notifyDataSetChanged() }
                            DeepLList.add(MessageRVModal("Печатает...", "bot"))
                            getResponse(final_send) { response ->
                                runOnUiThread {
                                    println(response)
                                    println("ОТВЕТ ПОЛУЧЕН")
                                    DeepLList.removeLast()
                                    DeepLList.add(MessageRVModal(response, "bot"))
                                    println(DeepLList)
                                    attemptsLeft -= 1
                                    attempts_text.text = "$attemptsLeft/3"
                                    messageRVAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    } else {
                        if (isSended) { Toast.makeText(applicationContext, "Вы уже отправили запрос! Дождитесь ответа", Toast.LENGTH_SHORT).show() }
                    }
                }
                if (attemptsLeft == 0 && isConnected) {
                    Toast.makeText(applicationContext, "Количество запросов исчерпано. После воспроизведения рекламы они восстановятся", Toast.LENGTH_SHORT).show()
                    showAd()
                    println("Реклама показывается")
                } }
            if (!isConnected){
                Toast.makeText(applicationContext, "Проверьте соединение с интернетом", Toast.LENGTH_SHORT).show()
            }
            if (DEBUG_MODE){ Toast.makeText(applicationContext, "Эта версия предназначена для проверки дизайна и не имеет функционала", Toast.LENGTH_SHORT).show() }
        false
        })
        //КОНЕЦ ОТПРАВКИ ЗАПРОСА
        //КОНЕЦ КЛАССА OnCreate и UI потока
    }
    //НАЧАЛО ОТПРАВКИ ЗАПРОСА К АПИ=================================================
    fun getResponse(question: String, callback: (String) -> Unit) { //Отправляем запрос
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
                    runOnUiThread { Toast.makeText(applicationContext, "К сожалению произошла ошибка. Проверьте соединение с интернетом или попробуйте позже. Количество запросов не уменьшено", Toast.LENGTH_SHORT).show() }
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
                        val jsonObject = JSONObject(body!!)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val test = jsonArray.getJSONObject(0)
                        val message = test.getJSONObject("message")
                        val final_res = message.getString("content")
                        callback(final_res)
                    } catch (e: JSONException) {
                        println(body)
                        runOnUiThread { Toast.makeText(applicationContext, "К сожалению сервер сейчас недоступен. Количество запросов не уменьшено", Toast.LENGTH_SHORT).show() }
                        attemptsLeft += 1
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
            val requestBody: RequestBody = JSONbody.toString().toRequestBody(JSON)
            val request: Request = Request.Builder().url(url_api+"v1/images/generations").header("Authorization", "Bearer $apiKey").post(requestBody).build()
            client.newCall(request).enqueue(object :Callback{
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка в генерации изображения",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val jsonObject= JSONObject(response.body!!.string())
                        val imgUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url")
                        println(imgUrl)
                        callback(imgUrl)
                        attemptsLeft = 0
                    }catch (e:Exception){
                        e.printStackTrace()
                        runOnUiThread { Toast.makeText(applicationContext, "Произошла ошибка! Повторите позже", Toast.LENGTH_SHORT).show() }
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
                    runOnUiThread { Toast.makeText(
                        applicationContext,
                        "К сожалению произошла ошибка. Проверьте соединение с интернетом или попробуйте позже. Количество запросов не уменьшено",
                        Toast.LENGTH_SHORT
                    ).show() }
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
                        val jsonObject = JSONObject(body!!)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val test = jsonArray.getJSONObject(0)
                        val message = test.getJSONObject("message")
                        val final_res = message.getString("content")
                        callback(final_res)
                    } catch (e: JSONException) {
                        println(body)
                        println("HEADER "+ response.headers.toString())
                        runOnUiThread { Toast.makeText(
                            applicationContext,
                            "К сожалению произошла ошибка. Проверьте соединение с интернетом или попробуйте позже. Количество запросов не уменьшено",
                            Toast.LENGTH_SHORT
                        ).show() }
                        attemptsLeft += 1
                    }
                }
            })
          }
        //Конец DeepL
        }
        //КОНЕЦ ОТПРАВКИ ЗАПРОСОВ К АПИ===========================================================
        private fun loadRewardedAd() {
            val adRequestConfiguration = AdRequestConfiguration.Builder(reward_ad_id).build()
            rewardedAdLoader?.loadAd(adRequestConfiguration)
        }
       private fun showAd() {
           rewardedAd?.apply {
               setAdEventListener(object : RewardedAdEventListener {
                   override fun onAdShown() {
                   }
                   override fun onAdFailedToShow(adError: AdError) {
                       runOnUiThread { Toast.makeText(
                           applicationContext,
                           "Ошибка показа рекламы",
                           Toast.LENGTH_SHORT
                       ).show() }
                   }
                   override fun onAdDismissed() {
                       rewardedAd?.setAdEventListener(null)
                       rewardedAd = null
                       loadRewardedAd() }
                   override fun onAdClicked() {}
                   override fun onAdImpression(impressionData: ImpressionData?) {}
                   @SuppressLint("SetTextI18n")
                   override fun onRewarded(reward: Reward) {
                       // Called when the user can be rewarded.
                       attemptsLeft = 3
                       attempts_text.text = "$attemptsLeft/3"
                       Saver.Save(pref!!, attemptsLeft, was_recently_seen)
                   }
               })
               show(this@MainActivity)
           }
    }



    override fun onDestroy() {
        super.onDestroy()
        Saver.Save(pref!!, attemptsLeft, was_recently_seen)
    }


//КОНЦ MAIN ACTIVITY==================================================================================
}



