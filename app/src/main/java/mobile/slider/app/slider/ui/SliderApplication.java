package mobile.slider.app.slider.ui;

import android.app.Application;
import android.content.Context;
import org.acra.*;
import org.acra.annotation.*;

import mobile.slider.app.slider.R;
import mobile.slider.app.slider.util.Util;

@ReportsCrashes(
        formUri = "http://www.bugsense.com/api/acra?api_key=b70181c8", formKey = "")

public class SliderApplication extends Application {
    public static final String FORM_KEY = "FORM_KEY";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Util.log("Init ACRA");
        ACRA.init(this);
    }
}