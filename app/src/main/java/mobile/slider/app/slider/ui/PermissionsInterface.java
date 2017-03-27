package mobile.slider.app.slider.ui;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import mobile.slider.app.slider.R;

public class PermissionsInterface extends Activity {
    Button retryButton;
    TextView permissionDenied;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_interface);
        retryButton = (Button) findViewById(R.id.retryButton);
        permissionDenied = (TextView) findViewById(R.id.permissionDenied);


    }
}
