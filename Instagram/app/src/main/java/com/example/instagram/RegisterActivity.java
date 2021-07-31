package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText username,name,email,password;
    private TextView login_user;
    private Button register;
    FirebaseDatabase databse;
    DatabaseReference mref;
    FirebaseAuth mAuth;

    ProgressDialog pd ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username=findViewById(R.id.username);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        login_user=findViewById(R.id.login_user);
        mAuth = FirebaseAuth.getInstance();
        databse=FirebaseDatabase.getInstance();
        mref=databse.getReference("Writing");
        register=findViewById(R.id.register);
        pd = new ProgressDialog(this);
        login_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtUsername = username.getText().toString();
                String txtName = name.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPassword = password.getText().toString();

                    if(txtName.isEmpty()||txtName.isEmpty()||txtEmail.isEmpty()||txtPassword.isEmpty())
                    {
                        Toast.makeText(RegisterActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                    }
                    else if (txtPassword.length() < 6)
                    {
                        Toast.makeText(RegisterActivity.this, "Password Too Short!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        registerUser(txtUsername,txtName,txtEmail,txtPassword);
                    }
            }
        });
    }

    private void registerUser(final String userName, final String name, final String email, String password)
    {
        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait");
        pd.show();

        mAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                HashMap <String ,Object> map = new HashMap<>();
                map.put("name",name);
                map.put("email",email);
                map.put("username",userName);
                map.put("id",mAuth.getCurrentUser().getUid());
                map.put("bio","");
                map.put("imageUrl","default");
                mref.child(mAuth.getCurrentUser().getUid()).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterActivity.this, "Registration Successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                        startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                        finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }
}