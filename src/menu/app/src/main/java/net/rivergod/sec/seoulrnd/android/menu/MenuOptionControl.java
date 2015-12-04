package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

public class MenuOptionControl {

    private LinearLayout lyMenu;

    private float maxWidth;
    private float closeViewWidth;

    private Timer timer;
    private float positionX;

    private final int FRAME = 10;
    private final int MOVE_PIXEL = 40;

    private boolean isOpen;

    public boolean isOpen(){
        return isOpen;
    }

    public void menuOpen(){
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                lyMenu.post(new Runnable() {
                    @Override
                    public void run() {
                        lyMenu.setX(positionX);

                        positionX -= MOVE_PIXEL;
                        if (positionX < MOVE_PIXEL) {
                            isOpen = true;
                            lyMenu.setX(0);
                            cancel();
                        }
                    }
                });
            }
        };

        positionX = maxWidth - closeViewWidth;

        timer.schedule(task, 0, FRAME);
    }

    public void menuClose(){
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                lyMenu.post(new Runnable() {
                    @Override
                    public void run() {
                        lyMenu.setX(positionX);

                        positionX += MOVE_PIXEL;
                        if (positionX > maxWidth - closeViewWidth) {
                            isOpen = false;
                            lyMenu.setX(maxWidth);
                            cancel();
                        }
                    }
                });
            }
        };

        positionX = 0;

        timer.schedule(task, 0, FRAME);
    }

    public void init(Activity activity){

        maxWidth = activity.getResources().getDimension(R.dimen.screen_max);
        closeViewWidth = activity.getResources().getDimension(R.dimen.close_menu);

        lyMenu = (LinearLayout) activity.findViewById(R.id.common_menu_layout);
        lyMenu.setX(maxWidth);

        activity.findViewById(R.id.menu_tab_setting).setOnClickListener(MenuShowClickListener);
        activity.findViewById(R.id.common_menu_close).setOnClickListener(MenuShowClickListener);

    }

    private View.OnClickListener MenuShowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menu_tab_setting:
                    menuOpen();
                    break;

                case R.id.common_menu_close:
                    menuClose();
                    break;
            }
        }
    };

}
