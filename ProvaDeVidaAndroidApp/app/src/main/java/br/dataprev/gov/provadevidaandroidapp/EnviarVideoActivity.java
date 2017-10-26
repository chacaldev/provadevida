package br.dataprev.gov.provadevidaandroidapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

import entidades.RetornoValidar;
import entidades.UserSessionManager;

public class EnviarVideoActivity extends AppCompatActivity {
    private ImageView imageView;
    private Bitmap foto;

    private String server;
    private String service;
    private String port;
    private String method;
    private UserSessionManager session;
    private String nomeUsuario;
    private String cpfUsuario;

    private Toolbar toolBar;

    static final int REQUEST_VIDEO_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_video);

        this.server = getString( R.string.serverip);
        this.service = getString(R.string.usuarioservice);
        this.port = getString(R.string.port);
        this.method=getString(R.string.videomethod);
        this.session = new UserSessionManager(getApplicationContext());

        this.toolBar = (Toolbar) findViewById(R.id.enviarVideoToolBar);
        toolBar.setTitle(getString(R.string.app_name));
        toolBar.setSubtitle("Gravar video");
        toolBar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textIcons));
        toolBar.setSubtitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textIcons));
        toolBar.setLogo(R.drawable.iconepequeno);
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
        Button btEnviar = (Button) findViewById(R.id.btEnviarVideo);

        btEnviar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gravarVideo();
            }
        });


        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Agora você precisa enviar um video de 10 segundos executando o comando recebido por SMS para finalizar o proccesso.")
                .setTitle("Parabéns "+nomeUsuario);
        builder.setNeutralButton("Entendi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();

    }

    private void logoff(){
        finish();
        this.session.logoutUser();

    }

    private void gravarVideo(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            String videoFullPath = this.getRealPathFromURI(getApplicationContext(),videoUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis;
            try {
                fis = new FileInputStream(new File(videoFullPath));
                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buf)))
                    baos.write(buf, 0, n);
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte[] bbytes = baos.toByteArray();
            Log.i("PROVA_DE_VIDA", "======= bytes:"+bbytes.length + "=======");

            String encoded = Base64.encodeToString(bbytes, Base64.DEFAULT);


            new Video().execute(this.server, this.port, this.service, this.cpfUsuario, this.method, encoded  );


        }

    }


    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    class Video extends AsyncTask<Object,Void,RetornoValidar>
    {

        private ProgressDialog progress = new ProgressDialog(EnviarVideoActivity.this);

        protected void onPreExecute() {
            //display progress dialog.
            this.progress.setMessage("Aguarde...");
            this.progress.show();
        }
        @Override
        protected RetornoValidar doInBackground(Object... param) {
            RetornoValidar retorno = null;

            try {

                //URL url = new URL("http://192.168.0.191:3000/usuario/06418444418/validar");
                URL url = new URL("http://"+param[0].toString()+":"+param[1].toString()+"/"+param[2].toString()+"/"+param[3].toString()+"/"+param[4]);
                Log.i("PROVA_DE_VIDA", "======= URL:"+url + "=======");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("video", param[5]);
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();


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

                Log.i("PROVA_DE_VIDA", "======= RETORNO:"+sb + "=======");

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    /*JSONObject json = new JSONObject(sb.toString());
                    try {
                        Long codigo = json.getLong("validation_status");
                        retorno= new RetornoValidar(codigo);
                    }catch(org.json.JSONException e){
                        JSONArray jArray = json.getJSONArray("erros");
                        for(int i=0;i<jArray.length();i++){
                            JSONObject json_data = jArray.getJSONObject(i);
                            retorno= new RetornoValidar(json_data.getLong("validation_status"));
                        }
                    }*/

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


        protected void onPostExecute(final RetornoValidar result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            finish();
            Intent i = new Intent(getApplicationContext(), FinalizacaoActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }





}
