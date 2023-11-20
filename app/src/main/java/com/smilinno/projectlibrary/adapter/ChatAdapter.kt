package com.smilinno.projectlibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smilinno.projectlibrary.R
import com.smilinno.projectlibrary.databinding.ChatItemRowBinding
import com.smilinno.projectlibrary.model.Chat
import com.smilinno.projectlibrary.util.ItemProvider
import com.smilinno.projectlibrary.util.PlayerApp
import com.smilinno.smilinnolibrary.callback.PlayerListener


class ChatAdapter(var chatList: MutableList<Chat>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemProvider<Chat> {

    var positionChat: Int? = null
    lateinit var recyclerView: RecyclerView

    object Constant {
        val VIEW_TYPE_CHAT_ITEM = 3
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item_row, parent, false)
        when (viewType) {
            Constant.VIEW_TYPE_CHAT_ITEM -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_item_row, parent, false)
                return TextChatViewHolder(view)
            }
        }
        return TextChatViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chatList[position]
        positionChat = position
        when (holder.itemViewType) {
            Constant.VIEW_TYPE_CHAT_ITEM -> (holder as TextChatViewHolder).bind(chat)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return Constant.VIEW_TYPE_CHAT_ITEM
    }

    override fun getItemCount(): Int = chatList.size

    inner class TextChatViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val binding: ChatItemRowBinding
        private lateinit var currentChat: Chat

        init { binding = ChatItemRowBinding.bind(view) }

        fun bind(chat: Chat) {
            currentChat = chat
            if (chat.text != null) {
                if (chat.isAssistant) {
                    binding.botText.text = chat.text
                    binding.botContainer.visibility = VISIBLE
                    binding.ownerContainer.visibility = GONE
                } else {
                    binding.ownerText.text = chat.text
                    binding.botContainer.visibility = GONE
                    binding.ownerContainer.visibility = VISIBLE
                }
            } else {
                if (chat.isAssistant) {
                    binding.botText.text = chat.custom.toString()
                    binding.botContainer.visibility = VISIBLE
                    binding.ownerContainer.visibility = GONE
                } else {
                    binding.ownerText.text = chat.text
                    binding.botContainer.visibility = GONE
                    binding.ownerContainer.visibility = VISIBLE
                }
            }
        }
    }

    fun addChat(chat: Chat) {
        chatList.add(0, chat)
        notifyItemInserted(0)
    }

    override fun get(position: Int): Chat {
        return chatList[position]
    }

    override fun size(): Int {
        return chatList.size
    }
}