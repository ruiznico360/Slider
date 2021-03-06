package mobile.slider.app.slider.garbage;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RelativeLayout;

import mobile.slider.app.slider.services.SystemOverlay;

@TargetApi(21)
public class RestarterJobService extends JobService {
    public RelativeLayout layout;

    @Override
    public boolean onStartJob(JobParameters params) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SystemOverlay.class.getName().equals(service.service.getClassName())) {
                running = true;
            }
        }
        if (running) {
        }else {
            Intent i = new Intent(getApplicationContext(),SystemOverlay.class);
            i.putExtra(SystemOverlay.IntentExtra.SAFE_REBOOT_SERVICE, true);
            startService(i);
        }
        jobFinished(params, true);
        ComponentName mServiceComponent = new ComponentName(this, RestarterJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, mServiceComponent);
        builder.setMinimumLatency(10000);
        builder.setPersisted(true);
        builder.setOverrideDeadline((long)(10000 * 1.05));
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}