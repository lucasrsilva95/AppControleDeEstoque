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
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.controledeestoque.Adapters.CategProdEstoqueAdapter;
import com.example.controledeestoque.Adapters.ProdEstoqueAdapter;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class Estoque extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;

    public ComprasRepositorio compRep;
    public ProdutosRepositorio prodRep;

    public ProdEstoqueAdapter estoqueAdapter;

    private RecyclerView lstEstoque;
    private List<Produto> dados;

    private List<String> categAbertas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque);
        NavigationView navView = (NavigationView) findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setTitle("Estoque Atual");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lstEstoque = (RecyclerView) findViewById(R.id.lstEstoque);

        categAbertas = new ArrayList<>();

        criarConexao();
        compRep.atualizarComprasFuturas();
        criarLista();
    }

    private void criarConexao() {

        try {
            compRep = new ComprasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

//    public List<Produto> ordenarProdutos(List<Produto> prods){
//        switch (ordem){
//            case ORD_UN_VENDIDA:
//                return prodRep.ordenarProdutosPorUnidadesVendidasUp(prods);
//            case ORD_ULT_VENDA_DOWN:
//                return prodRep.ordenarProdutosPorUltimaVendaDown(prods);
//            case ORD_ULT_VENDA_UP:
//                return prodRep.ordenarProdutosPorUltimaVendaUp(prods);
//        }
//        return prodRep.buscarTodos();
//    }

    public void criarLista(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstEstoque.setLayoutManager(linearLayoutManager);
        dados = compRep.estoqueAtual();
        ProdEstoqueAdapter adapter = new ProdEstoqueAdapter(dados, getApplicationContext());
        lstEstoque.setAdapter(adapter);
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
//            case R.id.estoque:
//                Intent it4 = new Intent(this, Estoque.class);
//                startActivity(it4);
//                break;
            case R.id.config:
                Intent it5 = new Intent(this, SettingsActivity.class);
                startActivityForResult(it5, 3);

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
