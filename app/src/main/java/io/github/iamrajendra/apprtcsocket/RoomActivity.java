package io.github.iamrajendra.apprtcsocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class RoomActivity extends AppCompatActivity {
    private EditText editText_room;
    private ImageButton button_joinRoom;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        intent = new Intent(this,MainActivity.class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editText_room = findViewById(R.id.room_et);
        button_joinRoom  = findViewById(R.id.joinroom_btn);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);

        button_joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_joinRoom.startAnimation(myAnim);

                intent.putExtra("roomname",editText_room.getText().toString());
                startActivity(intent);
            }
        });

    }

}
