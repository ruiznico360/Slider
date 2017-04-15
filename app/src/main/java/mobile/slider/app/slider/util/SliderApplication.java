package mobile.slider.app.slider.util;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import mobile.slider.app.slider.util.Util;

@ReportsCrashes(
        formUri = "http://www.bugsense.com/api/acra?api_key=b70181c8", formKey = "")

public class SliderApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}