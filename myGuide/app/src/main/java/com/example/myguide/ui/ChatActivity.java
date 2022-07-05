package com.example.myguide.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myguide.R;
import com.example.myguide.adapters.ChatAdapter;
import com.example.myguide.databinding.ActivityChatBinding;
import com.example.myguide.databinding.ActivityStudentSetupBinding;
import com.example.myguide.interfaces.EducationInterface;
import com.example.myguide.interfaces.MessageInterface;
import com.example.myguide.models.Education;
import com.example.myguide.models.Message;
import com.example.myguide.models.User;
import com.example.myguide.services.EducationServices;
import com.example.myguide.services.MessageServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    public static final String TAG = "ChatActivity";
    List<Message> mMessages;
    private ChatAdapter mAdapter;
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    boolean mFirstLoad;
    User otherUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        otherUser = getIntent().getParcelableExtra("otherUser");
        mFirstLoad = true;

        mMessages = new ArrayList<>();
        mAdapter = new ChatAdapter(this, ParseUser.getCurrentUser().getObjectId(), mMessages);

        binding.rvChat.setAdapter(mAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setReverseLayout(true);
        binding.rvChat.setLayoutManager(linearLayoutManager);


        setupMessagePosting(otherUser);
        refreshMessages();

        ParseLiveQueryClient parseLiveQueryClient = null;
        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(getString(R.string.websocketurl)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Message");
        SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (query, object) -> {
            Message newMessage = new Message();
            Message output = (Message) object;
            newMessage.setMessage(output.getMessage());
            newMessage.setReceiver(output.getReceiver());
            mMessages.add(0, (Message) object);

            // RecyclerView updates need to be run on the UI thread
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyItemInserted(0);
                    binding.rvChat.smoothScrollToPosition(0);
                }
            });
        });


    }

    void setupMessagePosting(User otherUser) {

        binding.ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = binding.etMessage.getText().toString();
                if (data == null || data.length() == 0) {
                    Toast.makeText(ChatActivity.this, "Please enter a message!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Message message = new Message();
                message.setSender((User) ParseUser.getCurrentUser());
                message.setReceiver(otherUser);
                message.setMessage(data);


                MessageServices newMessageServices = new MessageServices(new MessageInterface() {
                    @Override
                    public void getProcessFinish(List<Message> output) {

                    }

                    @Override
                    public void postProcessFinish(ParseException e) {
                        if (e == null) {
                            Toast.makeText(ChatActivity.this, "Message posted successfully!", Toast.LENGTH_SHORT).show();
                            refreshMessages();
                            binding.etMessage.setText(null);
                        } else {}}
                });
                newMessageServices.sendMessage(message);

            }
        });
    }

    void refreshMessages() {
        ParseQuery<Message> query1 = ParseQuery.getQuery(Message.class);
        query1.whereEqualTo(Message.KEY_SENDER, (User) ParseUser.getCurrentUser());
        if (((User) ParseUser.getCurrentUser()).isLoggedAsTutor() == true) {
            query1.whereEqualTo(Message.KEY_IS_FOR_TUTOR, false);
        } else {
            query1.whereEqualTo(Message.KEY_IS_FOR_TUTOR, true);
        }
        query1.whereEqualTo(Message.KEY_RECEIVER, otherUser);

        ParseQuery<Message> query2 = ParseQuery.getQuery(Message.class);
        query2.whereEqualTo(Message.KEY_RECEIVER, (User) ParseUser.getCurrentUser());
        if (((User) ParseUser.getCurrentUser()).isLoggedAsTutor() == true) {
            query2.whereEqualTo(Message.KEY_IS_FOR_TUTOR, true);
        } else {
            query2.whereEqualTo(Message.KEY_IS_FOR_TUTOR, false);
        }
        query2.whereEqualTo(Message.KEY_SENDER, otherUser);


        List<ParseQuery<Message>> list = new ArrayList<ParseQuery<Message>>();
        list.add(query1);
        list.add(query2);

        ParseQuery<Message> query3 = ParseQuery.or(list);
        query3.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        MessageServices newMessageServices = new MessageServices(new MessageInterface() {
            @Override
            public void getProcessFinish(List<Message> output) {
                mMessages.clear();
                mMessages.addAll(output);
                if (mMessages.size() == 0) {
                    binding.rvChat.setVisibility(View.GONE);
                    binding.emptyViewChat.setVisibility(View.VISIBLE);
                }
                binding.progressbarChat.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                if (mFirstLoad) {
                    binding.rvChat.scrollToPosition(0);
                    mFirstLoad = false;
                }

            }

            @Override
            public void postProcessFinish(ParseException e) {

            }

        });
        newMessageServices.getMessage(query3);

    }
}