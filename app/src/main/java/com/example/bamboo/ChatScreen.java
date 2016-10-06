package com.example.bamboo.pandatalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.example.bamboo.pandatalk.LoginScreen.EXTRA_USERNAME;

public class ChatScreen extends AppCompatActivity {

    private String userName;
    private EditText userText;
    private ImageButton sendButton;
    private ListView list;
    private ArrayList<String> testList;
    private ChatBubbleAdapter chatAdapter;
    private PrintWriter toServer;
    private String dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            //start listening to the server
            Thread listenThread = new Thread(new MessageListener(SocketSingleton.getSocket().getInputStream()));
            listenThread.start();

            //PrintWriter cannot wrap OutputStream, so socket's OutputStream is wrapped in OutputStreamWriter
            //Boolean true is for autoflush
            toServer = new PrintWriter(new OutputStreamWriter(SocketSingleton.getSocket().getOutputStream(), "UTF-8"), true);
        } catch(IOException e) {
            e.printStackTrace();
        }



        userName = getIntent().getStringExtra(EXTRA_USERNAME);

        userText = (EditText) findViewById(R.id.user_chat_box);

        sendButton = (ImageButton) findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userText.getText().toString().isEmpty())
                {
                    LoginScreen.hideSoftKeyboard(ChatScreen.this);
                    //Toast.makeText(getApplicationContext(),"Send "+userText.getText().toString()+" to server", Toast.LENGTH_SHORT).show();
                    dialog = userText.getText().toString();
                    notifyServer();
                    userText.setText("");
                }
            }
        });

        list = (ListView) findViewById(R.id.list);

        testList = new ArrayList<String>();

        //list and chatadapter initalisation
        chatAdapter = new ChatBubbleAdapter(getApplicationContext(),testList, userName);
        list.setAdapter(chatAdapter);
        list.setStackFromBottom(true);
        list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        list.setDividerHeight(0);

        //ask server for chat history
        new Thread(new Runnable() {
            @Override
            public void run() {
                toServer.println(":history");
            }
        }).start();

        Toast.makeText(getApplicationContext(), "Hello "+userName, Toast.LENGTH_SHORT).show();

        //disable the burger button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //prevent exiting the activity when user use back button to exit drawer
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    //will call to synstate()
    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.logout_button:
                buildDialog();
                break;
            case R.id.open_drawer:
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(GravityCompat.END);

        }

        return super.onOptionsItemSelected(item);
    }

    //build alertdialog to confirm user exiting the application
    public void buildDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatScreen.this);
        builder.setTitle("Logout");
        builder.setCancelable(true);
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Phew, close call!", Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                unRegister();
                Intent back = new Intent (getApplicationContext(),LoginScreen.class);
                startActivity(back);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void notifyServer()
    {
        //send message to the server. server will broadcast it to every user
        new Thread(new Runnable() {
            @Override
            public void run() {
                toServer.println(dialog);
            }
        }).start();
    }

    public void unRegister ()
    {
        //send quit command to server
        new Thread(new Runnable() {
            @Override
            public void run() {
                toServer.println(":quit");
            }
        }).start();
    }


    public void update(String m) {
        final String message = m;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testList.add(message);
                chatAdapter.notifyDataSetChanged();
            }
        });
    }








    public class MessageListener implements Runnable {
        //input from server
        private BufferedReader in;

        public MessageListener(InputStream inFromServer) {
            try {
                in = new BufferedReader(new InputStreamReader(inFromServer, "UTF-8"));
            } catch(UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            while(true){
                try {
                    String message = in.readLine();
                    switch (message) {
                        //don't do anything when these inputs are received
                        case "":
                        case "History:":
                        case "> ":
                            break;
                        case "-66612341818":
                            finish();
                            break;
                        default:
                            update(message);
                            break;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
