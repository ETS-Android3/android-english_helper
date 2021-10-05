package com.alexsp.englishhelper;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class DelayTimer
{
    private Timer timer = new Timer();
    private long DELAY = 1000;
    Handler uiHandler;
    OnRunTask runTask;

    interface OnRunTask
    {
        void run();
    }

    public DelayTimer(Handler uiHandler, OnRunTask task)
    {
        this.uiHandler = uiHandler;
        runTask = task;
    }

    public DelayTimer(Handler uiHandler, long delay, OnRunTask task)
    {
        this.uiHandler = uiHandler;
        DELAY = delay;
        runTask = task;
    }

    public void Start()
    {
        timer.cancel();
        timer = new Timer();
        timer.schedule(
            new TimerTask()
            {
                @Override
                public void run()
                {
                    uiHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            runTask.run();
                        }
                    });
                }
            }, DELAY
        );
    }
}



