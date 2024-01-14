package com.example.neirocombain

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageRVAdapter(private val messageList: ArrayList<MessageRVModal>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class UserMessageViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView){
        val userMessageTV:TextView = itemView.findViewById(R.id.MSGUser)
    }
    class BotMessageViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView){
        val botMessageTV:TextView = itemView.findViewById(R.id.MSGBot)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType==0) {
                view = LayoutInflater.from(parent.context).inflate(R.layout.useritem,parent,false)
                UserMessageViewHolder(view)
        }
        else{
            view = LayoutInflater.from(parent.context).inflate(R.layout.botitem,parent,false)
            BotMessageViewHolder(view)
        }
    }


    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sender = messageList.get(position).sender
        when(sender){
            "user" -> (holder as UserMessageViewHolder).userMessageTV.setText(messageList.get(position).message)
            "bot" -> (holder as BotMessageViewHolder).botMessageTV.setText(messageList.get(position).message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(messageList.get(position).sender){
            "user"->0
            "bot" ->1
             else ->1
        }
    }

}
