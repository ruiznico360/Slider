package mobile.slider.app.slider.services;

import java.util.LinkedList;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import mobile.slider.app.slider.util.IntentExtra;
import mobile.slider.app.slider.util.Util;

@TargetApi(21)
public class RestarterJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SystemOverlay.class.getName().equals(service.service.getClassName())) {
                running = true;
            }
        }
        if (!running) {
            Intent i = new Intent(getApplicationContext(),SystemOverlay.class);
            i.putExtra(IntentExtra.SAFE_REBOOT_SERVICE, true);
            startService(i);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}