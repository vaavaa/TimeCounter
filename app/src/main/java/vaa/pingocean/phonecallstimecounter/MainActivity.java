package vaa.pingocean.phonecallstimecounter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import core.Helper;
import core.StatEntry;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void readyForMonitoring() {

        StatEntry resultNeeded = Helper.OutgoingTotalTime(this);
        String result = Helper.getLastCall(this);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    @OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void showRationaleForState(final PermissionRequest request) {

        new AlertDialog.Builder(this)
                .setMessage(R.string.message)
                .setPositiveButton(R.string.rationale_ask_ok, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.rationale_ask_cancel, (dialog, button) -> request.cancel())
                .show();


    }

    @OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void showDeniedForState() {
        Toast.makeText(this, R.string.denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void showNeverAskForState() {
        Toast.makeText(this, R.string.never_ask, Toast.LENGTH_SHORT).show();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityPermissionsDispatcher.readyForMonitoringWithPermissionCheck(this);
    }

    public void closeApplication(View view) {
        this.finish();
        System.exit(0);
    }
}