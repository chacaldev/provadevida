package br.dataprev.gov.provadevidaandroidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import entidades.RetornoLogin;
import entidades.UserSessionManager;
import utils.ProvaDeVidaUtils;

public class MainActivity extends Activity {
    private EditText edtCpf;
    private EditText edtNumeroBeneficio;
    private EditText edtDatanascimento;
    private String server;
    private String service;
    private String port;
    // User Session Manager Class
    private UserSessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnVerify = (Button) findViewById(R.id.loginBtEntrar);
        this.edtCpf = (EditText) findViewById(R.id.loginEdtCpf);
        this.edtDatanascimento = (EditText) findViewById(R.id.loginEdtDataNascimento);
        this.edtNumeroBeneficio = (EditText) findViewById(R.id.loginEdtNumeroBeneficio);
        this.server = getString( R.string.serverip);
        this.service = getString(R.string.loginservice);
        this.port = getString(R.string.port);

        // User Session Manager
        session = new UserSessionManager(getApplicationContext());

        if( session.isUserLoggedIn() ){
            // Starting MainActivity
            Intent i = new Intent(getApplicationContext(), EnviarFotoActivity.class);
            //Intent i = new Intent(getApplicationContext(), EnviarVideoActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

            finish();
        }
        btnVerify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logar();
            }
        });
    }

    private boolean validate(String nb, String cpf, String data){
        boolean retorno = true;
        if(nb == null || nb.trim().equals("") ||
           cpf==null || cpf.trim().equals("") ||
           data==null || data.trim().equals("")  ){
            retorno = false;
        }
        return retorno;
    }
    private void logar(){
        String nb = this.edtNumeroBeneficio.getText().toString();
        String cpf = this.edtCpf.getText().toString();
        String data = this.edtDatanascimento.getText().toString();
        if( this.validate(nb,
                          cpf,
                           data)){
        new Logar().execute(this.server, this.port, this.service, cpf, nb, data, this.session);


        }
    }
    class Logar extends AsyncTask<Object,Void,RetornoLogin>
    {


        protected void onPreExecute() {
            //display progress dialog.

        }
        @Override
        protected RetornoLogin doInBackground(Object... param) {
            RetornoLogin retorno = null;

            try {

                URL url = new URL("http://"+param[0].toString()+":"+param[1].toString()+"/"+param[2].toString());

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("cpf", param[3].toString());
                postDataParams.put("nm_beneficio", param[4].toString());
                postDataParams.put("dt_nascimento", param[5].toString());
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    Log.i("PROVA_DE_VIDA", "======= RETORNO:"+sb.toString() + "=======");
                    in.close();
                    JSONObject json = new JSONObject(sb.toString());
                    Long objCpf = json.getLong("cpf");
                    String objNome = json.getString("nome");
                    retorno = new RetornoLogin(RetornoLogin.OK, ProvaDeVidaUtils.cpfToString(objCpf),objNome.toString());
                    UserSessionManager sessao = (UserSessionManager) param[6];
                    session.createUserLoginSession(retorno.getNome(),retorno.getCpf());
                }
                else {
                    retorno = new RetornoLogin(RetornoLogin.ERRO, null,null);
                }


            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            } catch(Exception e){
                e.printStackTrace();
            }

            return retorno;
        }


        public String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while(itr.hasNext()){

                String key= itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));

            }
            return result.toString();
        }


        protected void onPostExecute(RetornoLogin result) {
            Context context = getApplicationContext();
            String texto = "";
            if( result.getCodigo() == RetornoLogin.OK){
                // Starting MainActivity
                Intent i = new Intent(getApplicationContext(), EnviarFotoActivity.class);

                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Add new Flag to start new Activity
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                finish();
            }else{
                texto = "Falha ao autenticar";
            }
            CharSequence text = texto;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
