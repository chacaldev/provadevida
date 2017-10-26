package br.dataprev.gov.provadevidaandroidapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;

import entidades.UserSessionManager;

public class FinalizacaoActivity extends AppCompatActivity {
    private Toolbar toolBar;
    private UserSessionManager session;
    private String nomeUsuario;
    private String cpfUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalizacao);

        this.toolBar = (Toolbar) findViewById(R.id.finalizacaoToolBar);
        toolBar.setTitle(getString(R.string.app_name));
        toolBar.setSubtitle("Parabéns");
        toolBar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textIcons));
        toolBar.setSubtitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textIcons));
        toolBar.setLogo(R.drawable.iconepequeno);
        this.session = new UserSessionManager(getApplicationContext());
        if(session.checkLogin())
            finish();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        this.nomeUsuario = user.get(UserSessionManager.KEY_NAME);

        this.cpfUsuario = user.get(UserSessionManager.KEY_CPF);

        ImageView btLogoff = (ImageView) findViewById(R.id.btLogoff);
        btLogoff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logoff();
            }
        });

    }

    private void logoff(){
        finish();
        this.session.logoutUser();

    }

}
