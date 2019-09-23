package com.physicomtech.kit.physis_psmdetector;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.physicomtech.kit.physis_psmdetector.dialog.NotifyDialog;
import com.physicomtech.kit.physislibrary.ble.BluetoothLEManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    // region Check Permissions && Request Permissions
    private static final int REQ_APP_PERMISSION = 1000;
    private List<String> appPermisstions
            = Collections.singletonList(Manifest.permission.ACCESS_COARSE_LOCATION);

    private static final int INTENT_DELAY = 1000;

    private void checkPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 권한 요청 목록 생성
            final List<String> reqPermissions = new ArrayList<>();
            for(String permission : appPermisstions){
                if(checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                    reqPermissions.add(permission);
                }
            }

            // 권한 요청 목록 확인
            if(reqPermissions.size() == 0){
                nextActivity();
            }else{
                // 권한 요청 필요 - 알림 다이얼로그 출력
                new NotifyDialog().show(IntroActivity.this, R.string.permission_notify_title, R.string.permission_notify_message,
                        "확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(reqPermissions.toArray(new String[reqPermissions.size()]), REQ_APP_PERMISSION);
                            }
                        });
            }
        }else{
            nextActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQ_APP_PERMISSION){
            // 요청 권한 허용 상태 확인
            boolean accessStatus = true;
            for(int grantResult : grantResults){
                if(grantResult == PackageManager.PERMISSION_DENIED)
                    accessStatus = false;
            }

            if(!accessStatus){
                // 필요 권한 거부
                Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }else{
                // 필요 권한 허용
                nextActivity();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // Block - Back Key
    }

    private void nextActivity(){
        if(!BluetoothLEManager.getInstance(getApplicationContext()).getEnable()){
            Toast.makeText(getApplicationContext(), "블루투스 활성화 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), MonitoringActivity.class));
                    finish();
                }
            }, INTENT_DELAY);
        }
    }
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        checkPermissions();
    }
}
