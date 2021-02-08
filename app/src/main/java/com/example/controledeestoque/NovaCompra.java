package com.example.controledeestoque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.controledeestoque.Adapters.CategProdCompraAdapter;
import com.example.controledeestoque.Adapters.CompraAdapter;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NovaCompra extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private List<String> categAbertas;
    private EditText edtLocal;
    private TextView txtTotComp;

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView lstCompra;
    private RecyclerView.LayoutManager layoutManager;

    private CategProdCompraAdapter categAdapter;
    private ProdutosRepositorio prodRep;
    private Compra compra;
    private List<Produto> selecionados;
    private ComprasRepositorio compRep;
    private SQLiteDatabase conexao;

    private Spinner spinnerLocais;

    private AlertDialog.Builder dlgErro;

    private Button botCalendario;
    private String data;

    private Bundle bundle;

    private int codigo;

    private final int VOLTAR_INICIO = 1, ATUALIZAR_LISTA = 3;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_compra);
        getSupportActionBar().setTitle("Nova Compra");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lstCompra = (RecyclerView) findViewById(R.id.lstCompra);
        edtLocal = (EditText) findViewById(R.id.edtLocal);
        txtTotComp = (TextView) findViewById(R.id.txtTotComp);
        botCalendario = (Button) findViewById(R.id.botCalendario);
        botCalendario.setText(dataAtual().substring(0,10));
        selecionados = new ArrayList<>();

        compra = new Compra();
        criarConexao();

        categAbertas = new ArrayList<>();

        spinnerLocais = (Spinner) findViewById(R.id.spinnerLocais2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,locais());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocais.setAdapter(adapter);

        verificaParametro();
        criarLista();



    }

    private void criarConexao() {

        try {

            compRep = new ComprasRepositorio(this);

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    public void criarLista(){
        lstCompra.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(this);
        lstCompra.setLayoutManager(layoutManager);
        prodRep = new ProdutosRepositorio(this);
        List<Produto> dados = prodRep.buscarTodos();
        categAdapter = new CategProdCompraAdapter(categAbertas,selecionados,txtTotComp,this);
        lstCompra.setAdapter(categAdapter);
    }

    private void verificaParametro(){

        bundle = getIntent().getExtras();

        if((bundle != null) && ((bundle.containsKey("EDIT_COMPRA")) || bundle.containsKey("EDITCOMPFUT"))){
            if (bundle.containsKey("EDIT_COMPRA")) {
                compra = (Compra)bundle.getSerializable("EDIT_COMPRA");
            } else {
                compra = (Compra)bundle.getSerializable("EDITCOMPFUT");
            }
            codigo = compra.codigo;
            data = compra.data;
            botCalendario.setText(data.substring(0,10));
            for (int i=0; i < locais().size(); i++) {
                if (locais().get(i).contentEquals(compra.local)) {
                    spinnerLocais.setSelection(i);
                }
            }
            selecionados.addAll(compra.produtos);
            getSupportActionBar().setTitle("Editar Compra");
        }
    }

    public void novoProd(View view){
        selecionados = categAdapter.prodsSelecionados;
        Intent it = new Intent(NovaCompra.this, NovoProduto.class);
        startActivityForResult(it,0);
    }

    public List<String> locais(){
        List<String> locais = new ArrayList<>();
        if(compRep != null){
            List<Compra> compras = compRep.ordenarComprasPorDataUp(compRep.comprasEfetivadas());
            for(Compra comp:compras){
                if(!locais.contains(comp.local)){
                    locais.add(comp.local);
                }
            }
        }
        if(locais.size() == 0){
            spinnerLocais.setVisibility(View.INVISIBLE);
            edtLocal.setVisibility(View.VISIBLE);
        }
        return locais;
    }

    public void botNovoLocal(View view){
        if (spinnerLocais.getVisibility() == View.VISIBLE) {
            spinnerLocais.setVisibility(View.INVISIBLE);
            edtLocal.setVisibility(View.VISIBLE);
            edtLocal.requestFocus();
            abrirTeclado(edtLocal);
        } else {
            spinnerLocais.setVisibility(View.VISIBLE);
            edtLocal.setVisibility(View.INVISIBLE);
            fecharTeclado(edtLocal);
        }
    }

    public void dataCalendario(View view){
        String[] dataAnalise = new String[]{botCalendario.getText().toString()};
        int dia = Integer.parseInt(dataAnalise[0].substring(0,2));
        int mes = Integer.parseInt(dataAnalise[0].substring(3,5));
        int ano = Integer.parseInt(dataAnalise[0].substring(6,10));
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                ano, mes-1, dia
        );
        datePickerDialog.show();
    }

    public String dataAtual(){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        return (formatarData.format(data));
    }

    public void salvarParametros(){
//        compra = new Compra();
        compra.produtos = categAdapter.prodsSelecionados;
        if (data == null) {
            compra.data = dataAtual();
        } else {
            compra.data = data;
        }
        if(spinnerLocais.getVisibility() == View.VISIBLE) {
            if (spinnerLocais.getSelectedItem() != null) {
                compra.local = spinnerLocais.getSelectedItem().toString();
            } else {
                compra.local = "";
            }
        }else{
            compra.local = edtLocal.getText().toString();
        }
        compra.total = compra.totalCompra();
        compra.codigo = codigo;
    }

    public int subtracaoDatas(String data1, String data2) {
        int dias = 0;
        int diaAtual = Integer.parseInt(data1.substring(0, 2));
        int mesAtual = Integer.parseInt(data1.substring(3, 5));
        int anoAtual = Integer.parseInt(data1.substring(6, 10));
        int dia = Integer.parseInt(data2.substring(0, 2));
        int mes = Integer.parseInt(data2.substring(3, 5));
        int ano = Integer.parseInt(data2.substring(6, 10));

        int diasMeses = 0;
        int diasAno = 0;
        int mesMenor,mesMaior;
        if(mes < mesAtual){
            mesMenor = mes;
            mesMaior = mesAtual;
        }else{
            mesMenor = mesAtual;
            mesMaior = mes;
        }
        for (int i = mesMenor; i < mesMaior; i++) {
            if (("469").contains(Integer.toString(i)) || (i == 11)) {
                diasMeses += 30;
            } else if (i == 2) {
                if (eBissexto(anoAtual)){
                    diasMeses += 29;
                }else{
                    diasMeses += 28;
                }
            }else{
                diasMeses += 31;
            }
        }
        if(mes > mesAtual){
            diasMeses = -diasMeses;
        }
        for(int i = ano; i < anoAtual; i++){
            if (eBissexto(i)){
                diasAno += 366;
            }else{
                diasAno += 365;
            }
        }
        int diaRes = diaAtual - dia;

        dias = diasAno + diasMeses + diaRes;

        return dias;
    }

    public boolean eBissexto(int ano){
        boolean resp = false;
        if ((ano%400 == 0) || ((ano%4==0) && !(ano%100==0))){
            resp = true;
        }
        return resp;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menunovacomp, menu);

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
            case R.id.botSalvComp:
                salvarParametros();
                Intent it = new Intent(NovaCompra.this,DetalhesCompra.class);
                it.putExtra("COMPRA",compra);
                if("Editar Compra".contentEquals(getSupportActionBar().getTitle())){
                    if (bundle.containsKey("EDIT_COMPRA")) {
                        it.putExtra("EDIT_COMPRA",true);
                        if (bundle.containsKey("EDITCOMPFUT")) {
                            it.putExtra("EDITCOMPFUT",true);
                        }
                    }
                }
                startActivityForResult(it, 2);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case VOLTAR_INICIO:
                setResult(VOLTAR_INICIO);
                finish();
                break;
//            case 2:
//                setResult(2);
//                finish();
//                break;
            case ATUALIZAR_LISTA:
                criarLista();
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        data = String.format("%02d/%02d/%d",dayOfMonth,month+1,year);
        botCalendario.setText(data);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        selecionados = categAdapter.prodsSelecionados;
        criarLista();
        super.onConfigurationChanged(newConfig);
    }
}
