@file:Suppress("SpellCheckingInspection")

package com.example.neirocombain



import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import okhttp3.OkHttpClient
import java.lang.reflect.Type
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    private var DEBUG_MODE = false//ВЫКЛ ВКЛ ДЕБАГ
    private val url_api = "https://api.proxyapi.ru/openai/" // v1/chat/completions
    private val apiKey = "sk-PumVBSKFnaHTR2u5QfK3qopF3sDxOBsr"
    private val reward_ad_id = "R-M-4312016-3"
    private val banner_ad_id = "R-M-4312016-1"
    private lateinit var txtResponse: TextView
    private var rewardedAd: RewardedAd? = null
    private var rewardedAdLoader: RewardedAdLoader? = null
    private lateinit var etQuestion: EditText
    lateinit var attempts_text: TextView
    private lateinit var edittextval: String
    private lateinit var messageRV: RecyclerView
    private lateinit var image: ImageView
    private lateinit var messageRVAdapter: MessageRVAdapter
    lateinit var messageList: ArrayList<MessageRVModal>
    private lateinit var DeepLList: ArrayList<MessageRVModal>
    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
    private var mode = "ChatGPT"
    private var selectedNl = 1
    private var msgList_FNL = mutableListOf<String>()
    private var selectedLang = ""
    var attemptsLeft: Int = 0
    private val connectionChecker = InternetConnection()
    var was_recently_seen = false
    private val nLinks = listOf("DALLE-E", "ChatGPT", "DeepL")
    private var isSended = false
    private var isFirstGPT = true
    private var isLangSelected = false
    private var isFirstDeepL = true
    private var isFirstDalle = true
    var pref: SharedPreferences? = null
    val Saver = SaveData()
    private val gson = Gson()
    private lateinit var appUpdateManager: AppUpdateManager

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")


    override fun onCreate(savedInstanceState: Bundle?) {

        //ИНИЦИАЛИЗАЦИЯ=========================================
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        Updater(appUpdateManager).checlForUpdates(this)
        pref = this.getSharedPreferences("shared", Context.MODE_PRIVATE)!!
        etQuestion=findViewById(R.id.request)
        val left_btn = findViewById<ImageView>(R.id.leftarr)
        val right_btn = findViewById<ImageView>(R.id.rightarr)
        val model = findViewById<TextView>(R.id.model)
        val banner = findViewById<BannerAdView>(R.id.banner)
        val layoutManager = LinearLayoutManager(applicationContext)
        val mainLO = findViewById<LinearLayout>(R.id.main)
        val reset_btn = findViewById<ImageButton>(R.id.reset_msg)
        image = findViewById(R.id.image)
        val langTV = findViewById<AutoCompleteTextView>(R.id.lang)
        val dropMenu = findViewById<TextInputLayout>(R.id.dropMenu)
        txtResponse=findViewById(R.id.desc)
        attempts_text = findViewById(R.id.attemts)
        messageRV = findViewById(R.id.msgRV)
        attemptsLeft = pref?.getInt("attempts", 3)!!
        attempts_text.text = "$attemptsLeft/3"
        val json = pref?.getString("ui_msg", null)
        println("JSON SAVE = "+ json)
        val type: Type = object : TypeToken<ArrayList<MessageRVModal>>() {}.type
        println("TYPE = "+type)
        messageList = if ((json != null) && (json != "")){
            gson.fromJson<Any>(json, type) as ArrayList<MessageRVModal>
        } else{
            println("ДЕФОЛТНЫЙ СПИСОК")
            ArrayList()
        }

        val temp=intent.getStringExtra("temp")
        println("TEMP = $temp")
        DeepLList = ArrayList()
        messageRV.layoutManager = layoutManager
        messageRVAdapter = MessageRVAdapter(messageList)
        messageRV.adapter = messageRVAdapter
        //ВЫБОР ЯЗЫКОВ
        val back_btn = findViewById<ImageView>(R.id.settings)
        back_btn.setOnClickListener{

            Toast.makeText(applicationContext, "Что же там?", Toast.LENGTH_SHORT).show()
        }
        reset_btn.setOnClickListener {
            messageList.clear()
            messageRV.layoutManager = layoutManager
            messageRVAdapter = MessageRVAdapter(messageList)
            messageRV.adapter = messageRVAdapter
            msgList_FNL.clear()
            Toast.makeText(applicationContext, "История сообщений сброшена!", Toast.LENGTH_SHORT).show()
        }
        val languages = resources.getStringArray(R.array.lang_array)
        val arrayAdapter = ArrayAdapter(/* context = */ this, /* resource = */ R.layout.dropdown_item, /* objects = */ languages)
        langTV.setAdapter(arrayAdapter)
        langTV.onItemClickListener= AdapterView.OnItemClickListener { adapterView, view, i, l ->
            selectedLang = adapterView.getItemAtPosition(i).toString()
            isLangSelected = true
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
        banner.setAdSize(BannerAdSize.fixedSize(this, 320, 90))
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

        etQuestion.setOnEditorActionListener(OnEditorActionListener{ textView, i, keyEvent ->
            println(messageList)
            if (i==EditorInfo.IME_ACTION_SEND && !DEBUG_MODE) {

                edittextval = etQuestion.text.toString().trim().replaceFirstChar { it.uppercase() }
                val question = edittextval.replace(" ", "")
                if (attemptsLeft > 0 && connectionChecker.checkConnection(this)) {
                    txtResponse.visibility = View.GONE
                    messageRV.visibility = View.VISIBLE

                    if (question.isNotEmpty() && question.length >= 5 && !isSended && connectionChecker.checkConnection(this)) {
                        isSended = true
                        if (mode == "ChatGPT") {
                            val user_mask = """{"role": "user", "content" :"$edittextval"}"""
                            msgList_FNL.add(user_mask) //Сообщение для апи
                            messageList.add(MessageRVModal(edittextval, "user"))//Сообщение для чата
                            messageRVAdapter.notifyDataSetChanged()
                            isFirstGPT = false
                            etQuestion.setText("")
                            messageList.add(MessageRVModal("Печатает...", "bot"))
                            messageRV.smoothScrollToPosition(messageRVAdapter.itemCount)
                            //Отправляем строку в функцию
                            NeiroApi(attemptsLeft).getResponse(question, client, msgList_FNL, mode, url_api, apiKey,selectedLang) { response ->
                                runOnUiThread {
                                    messageRV.visibility = View.VISIBLE
                                    messageList.removeLast()
                                    val response_to_list = response.replace("\n", "").replace("\"", "'")
                                    messageList.add(MessageRVModal(response, "bot"))
                                    messageRVAdapter.run { notifyDataSetChanged() }
                                    messageRV.smoothScrollToPosition(messageRVAdapter.itemCount)
                                    val nl_mask = """{"role": "assistant", "content" :"$response_to_list"}"""
                                    msgList_FNL.add(nl_mask)
                                    attemptsLeft -= 1
                                    attempts_text.text = "$attemptsLeft/3"
                                    Saver.Save(pref!!, attemptsLeft, was_recently_seen, messageList)
                                }
                                //КОНЕЦ UI ПОТОКА
                            }
                            isSended = false
                        }
                        if (mode == "DALLE-E") {//DALL E
                            isFirstDalle = false
                            messageRV.visibility = View.GONE
                            Toast.makeText(applicationContext, "В разработке", Toast.LENGTH_SHORT).show()

                        }
                        if (mode == "DeepL" && isLangSelected) {
                            txtResponse.visibility = View.GONE
                            isFirstDeepL = false
                            messageRV.visibility = View.VISIBLE
                            DeepLList.add(MessageRVModal(edittextval, "user"))
                            messageRVAdapter.run { notifyDataSetChanged() }
                            DeepLList.add(MessageRVModal("Печатает...", "bot"))
                            messageRV.smoothScrollToPosition(messageRVAdapter.itemCount)
                            NeiroApi(attemptsLeft).getResponse(question, client, msgList_FNL, mode, url_api, apiKey,selectedLang) { response ->
                                runOnUiThread {
                                    DeepLList.removeLast()
                                    DeepLList.add(MessageRVModal(response, "bot"))
                                    attemptsLeft -= 1
                                    attempts_text.text = "$attemptsLeft/3"
                                    messageRVAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                        if(!isLangSelected && mode=="DeepL"){ Toast.makeText(applicationContext, "Вы не выбрали язык", Toast.LENGTH_SHORT).show() }

                    } else if (isSended) { Toast.makeText(applicationContext, "Вы уже отправили запрос! Дождитесь ответа", Toast.LENGTH_SHORT).show() }
                }
                if (attemptsLeft == 0 && connectionChecker.checkConnection(this)) {
                    Toast.makeText(applicationContext, "Количество запросов исчерпано. После воспроизведения рекламы они восстановятся", Toast.LENGTH_SHORT).show()
                    showAd() } }
            when {
                !connectionChecker.checkConnection(this) -> { Toast.makeText(applicationContext, "Проверьте соединение с интернетом", Toast.LENGTH_SHORT).show() }
            }
            if (DEBUG_MODE){ Toast.makeText(applicationContext, "Эта версия предназначена для проверки дизайна и не имеет функционала", Toast.LENGTH_SHORT).show() }
        false
        })
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
                       println("AD SHOWn")
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
                       Saver.Save(pref!!, attemptsLeft, was_recently_seen, messageList)
                   }
               })
               show(this@MainActivity)
           }
    }
    override fun onResume() {
        super.onResume()
        Updater(appUpdateManager).resunmeUpdate(this)

    }
    override fun onDestroy() {
        super.onDestroy()
        Saver.Save(pref!!, attemptsLeft, was_recently_seen, messageList)
    }


//КОНЦ MAIN ACTIVITY==================================================================================
}



