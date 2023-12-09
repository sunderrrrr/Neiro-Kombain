package com.example.neirocombain

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import java.util.Timer

import kotlin.concurrent.schedule

class SelectNL(selectedNL: Int, var mode: String, var nLinks: List<String>, model: TextView, mainLO: LinearLayout, attempts_text: TextView, messageRV: RecyclerView, dropMenu:TextInputLayout, messageRVAdapter: MessageRVAdapter, messageList:ArrayList<MessageRVModal>, DeepLList: ArrayList<MessageRVModal>, txtResponse: TextView, isFirstGPT: Boolean, isFirstDalle:Boolean, isFirstDeepL: Boolean, image: ImageView) {
    var selectedNl = selectedNL
    val model = model
    val mainLO = mainLO
    val attempts_text = attempts_text
    val messageRV = messageRV
    val dropMenu = dropMenu
    var messageRVAdapter = messageRVAdapter
    val messageList = messageList
    val DeepLList = DeepLList
    val isFirstGPT = isFirstGPT
    val isFirstDalle = isFirstDalle
    val isFirstDeepL = isFirstDeepL
    val txtResponse = txtResponse
    val image= image

    fun MoveLeft(context: Context){

        if (selectedNl==2) {
            Timer().schedule(150) {
                selectedNl = 1
                println("ВТОРИЧНАЯ ИНИЦИАЛИЗАЦИЯ АДАПТЕРА В ЖПТ")
                mode = nLinks[selectedNl]

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
                    image.visibility= View.GONE
                    txtResponse.text = "Что умеет ChatGPT: \n\n"+" 1. Писать сочинения. \n 'Напиши сочинение о конфликте поколений' \n\n 2.Объяснять что-либо.\n 'Объясни вкратце законы Ньютона' \n\n 3. Переводить на другие языки \n 'Переведи привет на Японский'"
                    mainLO.animate().alpha(1f).duration = 500
                    model.animate().alpha(1f).duration = 500
                    attempts_text.animate().alpha(1f).duration = 500

            }
        }
        if (selectedNl ==1) {
            Timer().schedule(150) {
                selectedNl = 0
                mode = nLinks[selectedNl]

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
                    image.visibility= View.VISIBLE
                    attempts_text.animate().alpha(1f).duration = 500
                    mainLO.animate().alpha(1f).duration = 500
                    model.animate().alpha(1f).duration = 500

            }
        }
        if (selectedNl == 0) {
            Timer().schedule(150) {
                selectedNl = 2
                mode = nLinks[selectedNl]

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
                    image.visibility= View.GONE
                    txtResponse.text = "Что умеет DeepL: \n\n1.Автоматически обнажуривать язык источника\n\n 2.Понимает сленг и идиомы\n\n 3.Имеет при себе большую языковую базу \n\n 4.Более точный перевод с помощью нейросетей"
                    mainLO.animate().alpha(1f).duration = 500
                    model.animate().alpha(1f).duration = 500
                    attempts_text.animate().alpha(1f).duration = 500

            }
        }
    }
}