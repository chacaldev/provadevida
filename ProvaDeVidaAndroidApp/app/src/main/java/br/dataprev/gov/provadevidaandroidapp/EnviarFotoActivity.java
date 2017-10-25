package br.dataprev.gov.provadevidaandroidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import entidades.UserSessionManager;

public class EnviarFotoActivity extends Activity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private Bitmap foto;

    private String server;
    private String service;
    private String port;
    private String method;
    private UserSessionManager session;
    private String nomeUsuario;
    private String cpfUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_foto);
        this.server = getString( R.string.serverip);
        this.service = getString(R.string.usuarioservice);
        this.port = getString(R.string.port);
        this.method=getString(R.string.validarmethod);
        this.session = new UserSessionManager(getApplicationContext());

        if(session.checkLogin())
            finish();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        this.nomeUsuario = user.get(UserSessionManager.KEY_NAME);

        this.cpfUsuario = user.get(UserSessionManager.KEY_CPF);

        TextView textLogin = (TextView) findViewById(R.id.txtLogin);

        textLogin.setText("Bem-vindo "+this.nomeUsuario);

        this.imageView = (ImageView) findViewById(R.id.fotoImageView);

        this.imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        Button btEnviar = (Button) findViewById(R.id.fotoBtEnviar);

        btEnviar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enviarFoto();
            }
        });

    }

    private void enviarFoto(){
        if( this.foto != null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            this.foto.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            new Verify().execute(this.server, this.port, this.service, this.cpfUsuario, this.method, encoded  );

        }else{
            Context context = getApplicationContext();
            CharSequence text = "Por favor tire uma foto antes de enviar";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            this.foto = imageBitmap;
            this.imageView.setImageBitmap(imageBitmap);


        }
    }

    class Verify extends AsyncTask<Object,Void,String>
    {


        protected void onPreExecute() {
            //display progress dialog.

        }
        @Override
        protected String doInBackground(Object... param) {
            String retorno = "";

            try {

                //URL url = new URL("http://192.168.0.191:3000/usuario/06418444418/validar");
                URL url = new URL("http://"+param[0].toString()+":"+param[1].toString()+"/"+param[2].toString()+"/"+param[3].toString()+"/"+param[4]);

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("imagem", param[5]);
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

                    in.close();
                    retorno= sb.toString();

                }
                else {
                    retorno = new String("false : "+responseCode);
                }

                Log.i("PROVA_DE_VIDA", "======= RETORNO:"+retorno + "=======");
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


        protected void onPostExecute(String result) {
            Context context = getApplicationContext();
            CharSequence text = result;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
