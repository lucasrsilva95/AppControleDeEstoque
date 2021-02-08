package com.example.controledeestoque;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Class act;

    private ComprasRepositorio compRep;
    private ProdutosRepositorio prodRep;

    private static final int REQUEST_CODE = 101;
    private final int PERMISSAO_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        definirPagInicial();
        criarConexao();
//        salvarbackupBancos();
//        restaurarBackupBancos();
        compRep.definirNotificacoes();
        if(prodRep.buscarTodos().size() == 0){
            prodRep.novaLista(prodRep.produtosIniciais());
        }
        verificaPermissoes();
    }

    public void criarConexao(){
        compRep = new ComprasRepositorio(this);
        prodRep = new ProdutosRepositorio(this);
    }

    private void verificaPermissoes(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSAO_REQUEST);
                return;
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSAO_REQUEST);
                return;
            }
        }
        Intent it = new Intent(MainActivity.this, act);
        startActivity(it);
        finish();
    }

    public void definirPagInicial(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String pag = prefs.getString("act_ini","Compras");
        if(pag.contentEquals("Compras")){
            act = CompList.class;
        }else if(pag.contentEquals("Compras Futuras")){
            act = ComprasFuturas.class;
        }else if(pag.contentEquals("Estoque")){
            act = Estoque.class;
        }else {
            act = ProdList.class;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSAO_REQUEST:
                verificaPermissoes();
        }
    }
}

