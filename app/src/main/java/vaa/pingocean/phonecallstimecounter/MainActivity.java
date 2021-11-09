package vaa.pingocean.phonecallstimecounter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.widget.Toast;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    final int PHONE_STATE = 98;
    final int READ_STORAGE_PERMISSION_REQUEST = 99;

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG})
    void showCamera() {
        LastCall();
    }

    @OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG})
    void showRationaleForCamera(final PermissionRequest request) {

        new AlertDialog.Builder(this)
                .setMessage(R.string.message)
                .setPositiveButton(R.string.rationale_ask_ok, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.rationale_ask_cancel, (dialog, button) -> request.cancel())
                .show();


    }

    @OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG})
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG})
    void showNeverAskForCamera() {
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
        MainActivityPermissionsDispatcher.showCameraWithPermissionCheck(this);
    }

    public String LastCall() {
        StringBuffer sb = new StringBuffer();
        Cursor cur = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

        int number = cur.getColumnIndex(CallLog.Calls.NUMBER);
        int duration = cur.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details : \n");
        while (cur.moveToNext()) {
            String phNumber = cur.getString(number);
            String callDuration = cur.getString(duration);
            sb.append("\nPhone Number:" + phNumber);
            break;
        }
        cur.close();
        String str = sb.toString();
        return str;
    }
}