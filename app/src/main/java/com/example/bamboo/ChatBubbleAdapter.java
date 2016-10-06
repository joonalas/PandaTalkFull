package com.example.bamboo.pandatalk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Bamboo on 10/2/2016.
 */

public class ChatBubbleAdapter extends ArrayAdapter<String> {

    private String currentUser;

    public ChatBubbleAdapter (Context context, ArrayList<String> list, String currentUser)
    {
        super(context,0,list);
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String currentChat = getItem(position);

        String user = currentChat.substring(0,currentChat.indexOf("@"));

        String timeStamp = currentChat.substring(currentChat.indexOf("@"), currentChat.indexOf("&//*"));

        String chatDialog = currentChat.substring(currentChat.indexOf("&//*")+4,currentChat.length());

        if (user.equals(currentUser))
        {
            //message
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_chat_bubble, parent, false);
            TextView text = (TextView) convertView.findViewById(R.id.user_chat_bubble);
            text.setText(chatDialog);

            //timestamp
            TextView time = (TextView) convertView.findViewById(R.id.time);
            time.setText(timeStamp.substring(12, 17));
        }
        else
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.other_chat_bubble, parent, false);

            TextView sender = (TextView) convertView.findViewById(R.id.sender);
            sender.setText(user);

            TextView text = (TextView) convertView.findViewById(R.id.other_chat_bubble);
            text.setText(chatDialog);

            //timestamp
            TextView time_other = (TextView) convertView.findViewById(R.id.time_other);
            time_other.setText(timeStamp.substring(12, 17));
        }

        return convertView;
    }
}
