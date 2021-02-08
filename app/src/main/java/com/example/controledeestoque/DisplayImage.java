package com.example.controledeestoque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageWriter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DisplayImage extends AppCompatActivity {

    com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView imageView;

    public Compra compra;
    public ComprasRepositorio compRep;
    public URI locImagem;

    public AlertDialog.Builder dlgDel;

    public File imagem;
    private File arquivoFoto = null;
    private Uri photoURI;
    private final int CAMERA = 103, FOTO_ALTERADA = 107, NOVA_FOTO = 108, FOTO_NAO_ENCONTRADA = 110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        getSupportActionBar().setTitle("Nota");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        compRep = new ComprasRepositorio(this);
        imageView = findViewById(R.id.mimageView);
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        Bundle bundle = getIntent().getExtras();
        compra = (Compra) bundle.getSerializable("COMPRA");
        imagem = compra.foto;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagem.getPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        try {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagem.getPath(), bmOptions);
            imageView.setImage(ImageSource.bitmap(rotateImage(bitmap)));
        } catch (NullPointerException e) {
            Toast.makeText(this,"Foto não encontrada",Toast.LENGTH_LONG).show();
            setResult(FOTO_NAO_ENCONTRADA);
            finish();
        }
    }

    private Bitmap rotateImage(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface((imagem).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case 8:
                matrix.setRotate(-90);
            default:
        }
        if(bitmap.getWidth() > bitmap.getHeight()){
            matrix.setRotate(90);
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

//    private File criarArquivo(){
//
//        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
//
//        String nomeArquivo;
//        if (!"".contains(compra.local)) {
//            nomeArquivo = compra.local + "_" + timeStamp;
//        } else {
//            nomeArquivo = "JPG_" + timeStamp;
//        }
//
//        File pasta = new File("/sdcard/Pictures/Notas");
//        if(!pasta.exists()){
//            pasta.mkdir();
//        }
//        imagem = new File(pasta.getAbsolutePath() + File.separator + nomeArquivo + ".jpg");
//
//        return imagem;
//    }
//
//    public void captureImage(){
//
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        if(cameraIntent.resolveActivity(getPackageManager()) != null){
//
//            arquivoFoto = criarArquivo();
//
//            if(arquivoFoto != null){
//                photoURI = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".fileprovider", arquivoFoto);
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(cameraIntent, CAMERA);
//            }
//
//        }
//    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_foto, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                finish();
                break;
            case R.id.menuItem_novaFoto:
                setResult(NOVA_FOTO);
                finish();
                break;
            case R.id.menuItem_del:
                dlgDel = new AlertDialog.Builder(this)
                        .setTitle("Deletar Foto")
                        .setMessage("Você tem certeza que deseja deletar a foto?")
                        .setNegativeButton("Não", null)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                compra.deletarFoto();
                                compra.foto = null;
                                if(compra.codigo != 0){
                                    compRep.alterar(compra);
                                }
                                Toast.makeText(getApplicationContext(), "Foto Deletada com Sucesso", Toast.LENGTH_SHORT).show();

                                Intent it = getIntent();
                                it.putExtra("Compra",compra);
                                setResult(FOTO_ALTERADA);
                                finish();
                            }
                        });
                dlgDel.show();
        }
        return super.onOptionsItemSelected(item);
    }

//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == CAMERA && resultCode == RESULT_OK){
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(arquivoFoto)));
//            Toast.makeText(this, "Foto Salva com Sucesso", Toast.LENGTH_LONG).show();
//            if (compra.foto != null) {
//                compra.deletarFoto();
//            }
//            compra.foto = imagem;
//            if(compra.codigo != 0){
//                compRep.alterar(compra);
//            }
//            Intent it = new Intent();
//            it.putExtra("Compra",compra);
//            setResult(FOTO_ALTERADA);
//            finish();
//        }
//    }
}
