package com.example.controledeestoque;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NovoProduto extends AppCompatActivity{

    private int codigo;
    private boolean atualizar;

    private EditText edtNome,edtCategoria;
    private Spinner spinnerUnidade,spinnerCategoria;
    private Button botSalvar;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;

    private AlertDialog.Builder dlgErro,dlgAlert,dlgSubs;

    private ProdutosRepositorio prodRep;
    private SQLiteDatabase conexao;

    private Produto produto;

    private final int ATUALIZAR = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novoprod);
        getSupportActionBar().setTitle("Novo Produto");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtNome = (EditText) findViewById(R.id.edtNome);
//        InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(edtNome, InputMethodManager.SHOW_IMPLICIT);
        edtCategoria = (EditText) findViewById(R.id.edtCategoria);
        botSalvar = (Button) findViewById(R.id.botaoSalvar);
        spinnerUnidade = (Spinner) findViewById(R.id.spinnerUnidade);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,unidades());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidade.setAdapter(adapter);

        criarConexaoProduto();

        spinnerCategoria = (Spinner) findViewById(R.id.spinnerCategorias);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categorias());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter2);

        atualizar = false;

        verificaParametro();

    }

    public void botSalvarProduto(final View view) {
        if (!validaCampos()) {
            final Produto prod = new Produto();
            prod.nome = edtNome.getText().toString();
            if (edtCategoria.getVisibility() == View.VISIBLE) {
                prod.categoria = edtCategoria.getText().toString();
            } else {
                prod.categoria = spinnerCategoria.getSelectedItem().toString();
            }
            prod.unidade = spinnerUnidade.getSelectedItem().toString();

            prodRep = new ProdutosRepositorio(this);
            if (prodRep.prodJaExiste(prod) != 0 && !"Editar Produto".contains(getSupportActionBar().getTitle())){
                dlgSubs = new AlertDialog.Builder(this);
                dlgSubs.setTitle("Produto Já Existe");
                dlgSubs.setMessage("Já existe um produto com esse nome e com essa Marca, deseja substituílo?");
                dlgSubs.setNegativeButton("Não", null);
                dlgSubs.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        codigo = prodRep.prodJaExiste(prod);
                        inserirAtualizarProd(prod, true);
                    }
                });
                dlgSubs.show();
            }else{
                inserirAtualizarProd(prod, atualizar);
            }
        }

    }

    public void inserirAtualizarProd(Produto prod, boolean atualizar){
        if (atualizar) {
            try {
                Produto prodAntigo = prodRep.buscarprod(codigo);
                prod.codigo = codigo;
                prod.duracao = prodAntigo.duracao;
                prod.preço = prodAntigo.preço;

                prodRep.alterar(prod);

            } catch (SQLiteException ex) { // No caso de não ser possivel criar a conexão
                dlgErro.setTitle("Erro");
                dlgErro.setMessage(ex.getMessage());
                dlgErro.setNeutralButton("OK", null);
                dlgErro.show();
            } finally {
                Toast.makeText(this, "Produto atualizado com sucesso", Toast.LENGTH_LONG).show();
                atualizar = false;
                setResult(ATUALIZAR);
                finish();
            }
        } else {
            try {
                prodRep.inserir(prod);

            } catch (SQLiteException ex) { // No caso de não ser possivel criar a conexão
                dlgErro.setTitle("Erro");
                dlgErro.setMessage(ex.getMessage());
                dlgErro.setNeutralButton("OK", null);
                dlgErro.show();
            } finally {
                Toast.makeText(this, "Produto inserido com sucesso", Toast.LENGTH_LONG).show();
                setResult(ATUALIZAR);
                finish();
            }
        }
    }
    public void botLimpar(View view){
        edtNome.setText("");
        edtCategoria.setText("");
        edtNome.requestFocus();
    }

    public void botNovaCateg(View view){
        if(spinnerCategoria.getVisibility() == View.VISIBLE){
            spinnerCategoria.setVisibility(View.INVISIBLE);
            edtCategoria.setVisibility(View.VISIBLE);
            abrirTeclado(edtCategoria);
            edtCategoria.requestFocus();
        }else{
            edtCategoria.clearFocus();
            spinnerCategoria.setVisibility(View.VISIBLE);
            edtCategoria.setVisibility(View.INVISIBLE);
            fecharTeclado(edtCategoria);
        }
    }

    private void verificaParametro(){

        produto = new Produto();
        Bundle bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("PRODUTO"))){
            produto = (Produto)bundle.getSerializable("PRODUTO");
            codigo = produto.codigo;
            edtNome.setText(produto.nome);
            edtCategoria.setText(produto.categoria);
            spinnerCategoria.setSelection(categorias().indexOf(produto.categoria));
            atualizar = true;
            getSupportActionBar().setTitle("Editar Produto");
            botSalvar.setText("ATUALIZAR");
            for(int i = 0; i < spinnerUnidade.getAdapter().getCount(); i++){
                if(produto.unidade.contains(spinnerUnidade.getItemAtPosition(i).toString())){
                    spinnerUnidade.setSelection(i);
                }
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtNome.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
            edtNome.clearFocus();
        }else{
            edtNome.requestFocus();
            abrirTeclado(edtNome);
        }
    }

    private void criarConexaoProduto(){
        // Criando conexão com o banco de dados
        try{
            prodRep = new ProdutosRepositorio(this);

        }catch (SQLiteException ex){ // No caso de não ser possivel criar a conexão
            dlgErro = new AlertDialog.Builder(this);
            dlgErro.setTitle("Erro");
            dlgErro.setMessage(ex.getMessage());
            dlgErro.setNeutralButton("OK",null);
            dlgErro.show();
        }
    }

    public boolean validaCampos() {
        boolean res = false;
        String campVaz = "";
        String exemplo = "";
        String nomeProd = edtNome.getText().toString();

        {
            if (res = isCampoVazio(nomeProd)) {
                edtNome.requestFocus();
                campVaz = "Nome";
                exemplo = edtNome.getHint().toString();

                if (res) {
                    dlgAlert = new AlertDialog.Builder(this);
                    dlgAlert.setTitle("Aviso");
                    dlgAlert.setMessage(String.format("O campo %s não está preenchido corretamente \n\n %s", campVaz, exemplo));
                    dlgAlert.setNeutralButton("Ok", null);
                    dlgAlert.show();
                }

            }

            return res;
        }
    }

    private boolean isCampoVazio(String valor){
        boolean result = (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
        return result;
    }

    private List<String> unidades(){
        List<String> unidades = new ArrayList<>();
        unidades.add("un");
        unidades.add("Kg");
        return unidades;
    }
    private List<String> categorias(){
        List<String> cats = new ArrayList<>();
        List<Produto> prods= prodRep.buscarTodos();
        for(Produto p:prods){
            if(!cats.contains(p.categoria) && !"".contains(p.categoria)){
                cats.add(p.categoria);
            }
        }
        Collections.sort(cats, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return cats;
    }

    public void abrirTeclado(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void fecharTeclado(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
