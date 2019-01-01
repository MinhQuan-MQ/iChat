package com.nmq.minhquan.multichoicequiz;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import org.jivesoftware.smack.SmackException;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    static final String APP_ID = "74470";
    static final String AUTH_KEY = "7VzuNgZ2ak4DsQh";
    static final String AUTH_SECRET = "a-nsQfGjBRe92zf";
    static final String ACCOUNT_KEY = "WFFGczrjveoqp_JtYst2";
//    static final int REQUEST_CODE = 1000;

    Button btnLogin, btnSignup;
    EditText edtUser, edtPassword;

    String userName, password;
    QBUser qbUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();
        initializeFramework();

        btnLogin = (Button) findViewById(R.id.main_btnLogin);
        btnSignup = (Button) findViewById(R.id.main_btnSignup);
        edtUser = (EditText) findViewById(R.id.main_EditUsername);
        edtPassword = (EditText)findViewById(R.id.main_EditPassword);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = "";
                password = "";
                userName = edtUser.getText().toString();
                password = edtPassword.getText().toString();
                qbUser = new QBUser(userName, password);
                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Login Successfull", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ChatDialogsActivity.class);
                        intent.putExtra("user", userName);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish();// Close login activity after logged
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void requestRuntimePermission(){
        int permissionRead = PermissionChecker.checkSelfPermission(getBaseContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = PermissionChecker.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionRead == PermissionChecker.PERMISSION_GRANTED
                && permissionWrite == PermissionChecker.PERMISSION_GRANTED ){
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
//        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            requestPermissions(new String[]{
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//            }, REQUEST_CODE);
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode){
//            case REQUEST_CODE:
//            {
//                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//                }
//            }
//            break;
//        }
//    }

    private void initializeFramework(){
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}



