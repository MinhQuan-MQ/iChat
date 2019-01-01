package com.nmq.minhquan.multichoicequiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpActivity extends AppCompatActivity {

    Button btnSignUp, btnCancel;
    EditText edtUser, edtPassword, edtFullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        registerSession();

        btnSignUp = (Button) findViewById(R.id.signup_btnSignup);
        btnCancel = (Button) findViewById(R.id.signup_btnCancel);

        edtUser = (EditText) findViewById(R.id.signup_EditUsername);
        edtPassword = (EditText) findViewById(R.id.signup_EditPassword);
        edtFullname = (EditText) findViewById(R.id.signup_EditFullName);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = edtUser.getText().toString();
                String password = edtPassword.getText().toString();
                QBUser qbUser = new QBUser(userName, password);
                qbUser.setFullName(edtFullname.getText().toString());
                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Sign Up Successfull", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), "Sign Up Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void registerSession(){
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.d("ERROR", e.getMessage());
            }
        });
    }
}
