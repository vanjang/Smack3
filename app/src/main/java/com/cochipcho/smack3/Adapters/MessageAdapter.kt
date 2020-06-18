package com.cochipcho.smack3.Adapters

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cochipcho.smack3.Model.Message
import com.cochipcho.smack3.R
import com.cochipcho.smack3.Services.MessageService.messages
import com.cochipcho.smack3.Services.UserDataService
import org.w3c.dom.Text

class MessageAdapter(val context: Context, val message: ArrayList<Message>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userImage = itemView.findViewById<ImageView>(R.id.messageUserImage)
        val timestamp = itemView.findViewById<TextView>(R.id.timestampLabel)
        val username = itemView.findViewById<TextView>(R.id.messageUsernameLabel)
        val mesageBody = itemView.findViewById<TextView>(R.id.messageBodyLabel)

        fun bindMessage(context: Context, message: Message) {
            val resourceId = context.resources.getIdentifier(message.userAvatar,"drawable", context.packageName)
            userImage.setImageResource(resourceId)
            userImage.setBackgroundColor(UserDataService.returnAvatartColor(message.userAvatarColor))
            username.text = message.username
            timestamp.text = message.timestamp
            mesageBody.text = message.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return message.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindMessage(context, messages[position])
    }

}