package br.dataprev.gov.provadevidaandroidapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
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

import entidades.RetornoValidar;
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

    private Toolbar toolBar;

    public static Long codigoRetorno = 0l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_foto);
        this.server = getString( R.string.serverip);
        this.service = getString(R.string.usuarioservice);
        this.port = getString(R.string.port);
        this.method=getString(R.string.validarmethod);
        this.session = new UserSessionManager(getApplicationContext());

        this.toolBar = (Toolbar) findViewById(R.id.enviarFotoToolbar);
        toolBar.setTitle(getString(R.string.app_name));
        toolBar.setSubtitle("Enviar fotografia");
        toolBar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textIcons));
        toolBar.setSubtitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textIcons));
        if(session.checkLogin())
            finish();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        this.nomeUsuario = user.get(UserSessionManager.KEY_NAME);

        this.cpfUsuario = user.get(UserSessionManager.KEY_CPF);

        this.imageView = (ImageView) findViewById(R.id.fotoImageView);

        ImageView acaoCamera = (ImageView) findViewById(R.id.btCamera);

        acaoCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                baterFoto();
            }
        });

        ImageView btLogoff = (ImageView) findViewById(R.id.btLogoff);
        btLogoff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logoff();
            }
        });
        Button btEnviar = (Button) findViewById(R.id.fotoBtEnviar);

        btEnviar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enviarFoto();
            }
        });

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Neste passo iremos tirar uma foto que será utilizada para comprovação de vida através do reconhecimento das fotos do seu cadastro.")
                .setTitle("Atenção "+nomeUsuario);
        builder.setNeutralButton("Entendi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void baterFoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void logoff(){
        finish();
        this.session.logoutUser();

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

    class Verify extends AsyncTask<Object,Void,RetornoValidar>
    {

        private ProgressDialog progress = new ProgressDialog(EnviarFotoActivity.this);

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
                    JSONObject json = new JSONObject(sb.toString());
                    try {
                        Long codigo = json.getLong("validation_status");
                        retorno= new RetornoValidar(codigo);
                    }catch(org.json.JSONException e){
                        JSONArray jArray = json.getJSONArray("erros");
                        for(int i=0;i<jArray.length();i++){
                            JSONObject json_data = jArray.getJSONObject(i);
                            retorno= new RetornoValidar(json_data.getLong("validation_status"));
                        }
                    }

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
            AlertDialog.Builder builder = new AlertDialog.Builder(EnviarFotoActivity.this);
            String titulo ="ERRO";
            String mensagem ="";
            boolean passou = false;
            if (progress.isShowing()) {
                progress.dismiss();
            }
            EnviarFotoActivity.codigoRetorno = 0L;
            if(result!=null){
                if( result.getCodigo().equals(RetornoValidar.OK) ){
                    titulo = "Sucesso";
                    mensagem= "Você foi reconhecido, vamos ao próximo passo";
                    EnviarFotoActivity.codigoRetorno = 1L;
                }else{
                    if( result.getCodigo().equals(RetornoValidar.ERRO_FACE) ){
                        mensagem= "Nenhum rosto foi reconhecido, tente outra foto";
                    }else if( result.getCodigo().equals(RetornoValidar.ERRO_QUALIDADE) ){
                        mensagem= "A qualidade da foto não está adequada, tente outra foto";
                    }else if( result.getCodigo().equals(RetornoValidar.ERRO_NAO_RECONHECEU) ){
                        mensagem= "Seu rosto não é compatível";
                    }
                }
            }else{
                mensagem = "Erro ao conectar o serviço";
            }
            builder.setMessage(mensagem)
                    .setTitle(titulo);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    if( EnviarFotoActivity.codigoRetorno.equals(1L)){
                        finish();

                    }
                }
            });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();

            dialog.show();
        }
    }
}
