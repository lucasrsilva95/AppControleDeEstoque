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

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.controledeestoque.Adapters.CompListAdapter;
import com.example.controledeestoque.Adapters.MesCompraAdapter;
import com.example.controledeestoque.database.BancoOpenHelper;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class CompList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView lstComp;

    private BancoOpenHelper bancoOpenHelper;
    private MesCompraAdapter mesCompAdapter;
    private ComprasRepositorio compRep;
    private ProdutosRepositorio prodRep;
    private SQLiteDatabase conexao;

    private List<String> mesesAbertos;

    private AlertDialog.Builder dlgDel;

    public static final int ITEM_EDITAR = 1, ITEM_DELETAR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_compras);
        NavigationView navView = (NavigationView) findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setTitle("Compras");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lstComp = (RecyclerView) findViewById(R.id.lstCompra);

        criarConexao();

        mesesAbertos = new ArrayList<>();

        criarLista();

    }

    private void criarConexao() {

        try {
            compRep = new ComprasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);
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

    public void criarLista(){
        lstComp.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lstComp.setLayoutManager(linearLayoutManager);
        compRep = new ComprasRepositorio(this);
        List<Compra> dados = compRep.ordenarComprasPorDataUp(compRep.comprasEfetivadas());
        mesCompAdapter = new MesCompraAdapter(dados,mesesAbertos,this);
        lstComp.setAdapter(mesCompAdapter);
        registerForContextMenu(lstComp);
    }

    public void novaComp(View view){
        Intent it = new Intent(CompList.this,NovaCompra.class);
        startActivityForResult(it,2);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final Compra compraSel = mesCompAdapter.getContextMenuCompra();
        switch (id){
            case ITEM_EDITAR:
                Intent it2 = new Intent(CompList.this, NovaCompra.class);
                it2.putExtra("EDIT_COMPRA",compraSel);
                startActivityForResult(it2,2);
                break;
            case ITEM_DELETAR:
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setTitle("Deletar Compra?");
                dlgDel.setMessage("Tem certeza que deseja deletar a compra?");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        compRep.excluir(compraSel.codigo);
                        prodRep.atualizarProds(compraSel.produtos);
                        Toast.makeText(getApplicationContext(),"Compra Excluida com sucesso",Toast.LENGTH_LONG).show();
//                        setResult(3);
                        compRep.atualizarComprasFuturas();
                        compRep.definirNotificacoes();
                        criarLista();
                    }
                });
                dlgDel.show();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                startActivityForResult(it5, 0);

                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        criarLista();
    }
}
