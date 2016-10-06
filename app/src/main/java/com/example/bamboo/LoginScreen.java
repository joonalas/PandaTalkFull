package com.example.bamboo.pandatalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class LoginScreen extends AppCompatActivity {

    public final static String EXTRA_USERNAME = "com.example.bamboo.pandatalk.USERNAME";
    /*toServer needs to be field variable, so that it can be used by threads that print
    *out to the chat server*/
    private PrintWriter toServer;
    /*same here*/
    private String userName;

    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        connect();

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.login_button);
        editText = (EditText) findViewById(R.id.user_name);

        //Set up the elements so that when tap screen, editext will get close
        setupUI(findViewById(R.id.activity_login_screen));

        //Call the chat activity when click on the button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Please input username", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    userName = editText.getText().toString();
                    notifyServer();
                    startChatScreen();
                }

            }
        });

        //Set so that when hit enter, exit the edittext
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    if(v.getText().toString().isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"Please input username", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"High five the panda!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void notifyServer()
    {
        //notify the server new user is loging in

        //create PrintWriter able to print out to the server
        try {
            //PrintWriter cannot wrap OutputStream, so socket's OutputStream is wrapped in OutputStreamWriter
            //Boolean true is for autoflush
            toServer = new PrintWriter(new OutputStreamWriter(SocketSingleton.getSocket().getOutputStream(), "UTF-8"), true);
        } catch(IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                toServer.println(":user" + " " + userName);
            }
        }).start();
    }

    private void connect() {
        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] addressBytes = {(byte) (87), (byte) (95), (byte) (52), (byte) (167)};
                    InetAddress address = InetAddress.getByAddress(addressBytes);
                    SocketSingleton.setSocket(new Socket(address, 52828));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        connectThread.start();
        try {
            connectThread.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void checkConnection() {
        Thread check = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.d("checkConnection", "running");
                    if(SocketSingleton.getSocket()!=null) {
                        if(!SocketSingleton.getSocket().isConnected()) {
                            connect();
                        }
                    } else {
                        connect();
                    }
                }
            }
        });
        check.isDaemon();
        check.start();
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(LoginScreen.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void startChatScreen()
    {

        Intent startChatscreen = new Intent (this, ChatScreen.class);
        startChatscreen.putExtra(EXTRA_USERNAME, userName);
        startActivity(startChatscreen);
        finish();
    }
}
