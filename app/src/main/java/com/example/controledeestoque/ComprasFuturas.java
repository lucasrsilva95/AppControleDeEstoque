package com.example.controledeestoque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.controledeestoque.Adapters.CompFutAdapter;
import com.example.controledeestoque.database.BancoOpenHelper;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class ComprasFuturas extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView lstComp;

    private BancoOpenHelper bancoOpenHelper;
    private ComprasRepositorio compRep;
    private SQLiteDatabase conexao;
    private final int ATUALIZAR = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_comp_fut);
        NavigationView navView = (NavigationView) findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setTitle("Compras Futuras");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lstComp = (RecyclerView) findViewById(R.id.lstCompra);

        criarConexao();
        criarLista();

    }

    private void criarConexao() {

        try {
            compRep = new ComprasRepositorio(this);
            bancoOpenHelper = new BancoOpenHelper(this);
            conexao = bancoOpenHelper.getWritableDatabase();

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    public void criarLista() {
        compRep = new ComprasRepositorio(this);
        compRep.atualizarComprasFuturas();
        lstComp.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lstComp.setLayoutManager(linearLayoutManager);
        List<Compra> dados = compRep.comprasFuturas();
        CompFutAdapter adapter = new CompFutAdapter(dados, this);
        lstComp.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_compfut, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        switch (id) {
//
//            case R.id.itemAtualizar:
//                try {
//                    compRepositorio.atualizarComprasFuturas();
//                    criarLista();
//                    Toast.makeText(this,"Compras Futuras Atualizadas",Toast.LENGTH_SHORT).show();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//        }
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compras:
                Intent it1 = new Intent(this, CompList.class);
                startActivity(it1);
                break;
            case R.id.comprasFuturas:
                Intent it2 = new Intent(this, ComprasFuturas.class);
                startActivity(it2);
                break;
            case R.id.produtos:
                Intent it3 = new Intent(this, ProdList.class);
                startActivity(it3);
                break;
            case R.id.estoque:
                Intent it4 = new Intent(this, Estoque.class);
                startActivity(it4);
                break;
            case R.id.config:
                Intent it5 = new Intent(this, SettingsActivity.class);
                startActivityForResult(it5, ATUALIZAR);

                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ATUALIZAR) {
            criarLista();
        }
    }
}
