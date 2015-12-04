package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Dialog;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomPopup extends Dialog implements View.OnClickListener{

    private TextView tvTitle;

    private LinearLayout lyMsg;
    private TextView tvMsg;

    private LinearLayout lyTime;
    private EditText etTime;

    private ButtonClickEvent eventSender;


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.custom_popup_msg_ok:
                if(eventSender != null){
                    eventSender.onMessageOk();
                }
                break;
            case R.id.custom_popup_set_time_cancel:
                if(eventSender != null){
                    eventSender.onSetTimeCancel();
                }
                break;
            case R.id.custom_popup_set_time_ok:
                if(eventSender != null) {
                    eventSender.onSetTime(etTime.getText().toString());
                }
                break;
        }
    }

    public CustomPopup(MenuActivity mainClass, ButtonClickEvent event){
        super(mainClass, android.R.style.Theme_Translucent_NoTitleBar);
        setContentView(R.layout.custom_popup);

        tvTitle = (TextView)findViewById(R.id.custom_popup_title);

        lyMsg = (LinearLayout) findViewById(R.id.custom_popup_msg_layout);
        tvMsg = (TextView) findViewById(R.id.custom_popup_msg);
        tvMsg.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.custom_popup_msg_ok).setOnClickListener(this);

        lyTime = (LinearLayout) findViewById(R.id.custom_popup_time_layout);
        etTime = (EditText) findViewById(R.id.custom_popup_set_time);
        findViewById(R.id.custom_popup_set_time_ok).setOnClickListener(this);
        findViewById(R.id.custom_popup_set_time_cancel).setOnClickListener(this);

        this.eventSender = event;
    }

    public void setTitle(String title, boolean isTimeSet){
        if(tvTitle != null){
            tvTitle.setText(title);
        }

        if(isTimeSet){
            lyTime.setVisibility(View.VISIBLE);
            lyMsg.setVisibility(View.GONE);
        }else{
            lyTime.setVisibility(View.GONE);
            lyMsg.setVisibility(View.VISIBLE);
        }
    }

    public void setTimeText(String time){
        if(etTime != null) {
            etTime.setText(time);
        }
    }

    interface ButtonClickEvent {
        void onSetTime(String time);
        void onSetTimeCancel();
        void onMessageOk();

    }
}
