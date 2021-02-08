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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.controledeestoque.Adapters.CategProdEstoqueAdapter;
import com.example.controledeestoque.Adapters.CategProdListAdapter;
import com.example.controledeestoque.Adapters.ProdListAdapter;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import static com.example.controledeestoque.Adapters.ProdListAdapter.ITEM_DELETAR;
import static com.example.controledeestoque.Adapters.ProdListAdapter.ITEM_EDITAR;

public class ProdList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView lstProd;
    private List<Produto> dados;
    private List<String> categAbertas;

    private ProdListAdapter prodAdapter;
    private ProdutosRepositorio prodRep;

    private ComprasRepositorio compRep;

    private SQLiteDatabase conexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_prod);
        NavigationView navView = (NavigationView) findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setTitle("Produtos Cadastrados");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lstProd = (RecyclerView) findViewById(R.id.lstProd);

        categAbertas = new ArrayList<>();

        criarConexao();
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

    public void criarLista(){
//        lstProd.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstProd.setLayoutManager(linearLayoutManager);
//        dados = prodRep.buscarTodos();
        CategProdListAdapter adapterCateg = new CategProdListAdapter(categAbertas, getApplicationContext());
        lstProd.setAdapter(adapterCateg);
    }

    public void novoProd(View view){
        Intent it = new Intent(ProdList.this,NovoProduto.class);
        startActivityForResult(it,3);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final Produto produto = prodAdapter.getContextMenuProduto();
        switch (id){
            case ITEM_EDITAR:
                Intent it = new Intent(ProdList.this, NovoProduto.class);
                it.putExtra("PRODUTO", produto);
                startActivityForResult(it,2);
                break;
            case ITEM_DELETAR:
                AlertDialog.Builder dlgDel = new AlertDialog.Builder(this);
                dlgDel.setMessage("Tem certeza que deseja deletar o produto?");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prodRep.excluir(produto.codigo);
                        Toast.makeText(getApplicationContext(),"Produto Excluido com sucesso",Toast.LENGTH_LONG).show();
                        criarLista();
                    }
                });
                dlgDel.show();
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
                startActivity(it5);

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
