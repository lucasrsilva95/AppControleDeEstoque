package com.example.controledeestoque;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import com.example.controledeestoque.Adapters.HistProdAdapter;
import com.example.controledeestoque.Adapters.HistProdFutAdapter;
import com.example.controledeestoque.database.BancoOpenHelper;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DetalhesProduto extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;

    public TextView txtNomeProd, txtCategoria, txtDuracao, txtPreco, txtDiasUltComp, lblDuracao,
            txtDataVencimento, lblDataVencimento;
    public RecyclerView lstHistProd,lstProdCompFut;

    public FloatingActionButton fabInicial,fabEdit,fabDel;
    public Animation abrirBotoes, fecharBotoes, rodarDireita, rodarEsquerda;
    public boolean isOpen;

    public int codigo;

    private final int ATUALIZAR = 3;

    public Produto produto;

    public ProdutosRepositorio prodRep;
    public ComprasRepositorio compRep;
    public BancoOpenHelper bancoOpenHelper;
    public SQLiteDatabase conexao;
    public OperacoesDatas opDatas = new OperacoesDatas(this);
    private AlertDialog.Builder dlgDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_produto);
        getSupportActionBar().setTitle("Detalhe do Produto");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        {
            txtNomeProd = (TextView) findViewById(R.id.txtLocalComp);
            txtCategoria = (TextView) findViewById(R.id.txtNumProd);
            txtDuracao = (TextView) findViewById(R.id.txtDataComp);
            txtPreco = (TextView) findViewById(R.id.txtValTot);
            txtDiasUltComp = (TextView) findViewById(R.id.txtDiasUltComp);
            txtDataVencimento = (TextView) findViewById(R.id.txtDataVencimento);
            lblDataVencimento = (TextView) findViewById(R.id.lblDataVencimento);
            lblDuracao = (TextView) findViewById(R.id.lblDuracao);
            lstHistProd = (RecyclerView) findViewById(R.id.lstHistProd);
            lstProdCompFut = (RecyclerView) findViewById(R.id.lstProdCompFut);
        } // Declaração de Variáveis

        criarConexao();
        verificaParametro();
//        try {
//            criarListas();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }


    }

    private void criarConexao() {

        try {

            bancoOpenHelper = new BancoOpenHelper(this);
            conexao = bancoOpenHelper.getWritableDatabase();
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

    private void verificaParametro(){

        Bundle bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("PRODUTO"))){
            produto = (Produto)bundle.getSerializable("PRODUTO");
            definirParametrosProd();
        }
    }

    public void criarListas() throws ParseException {
        lstHistProd.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lstHistProd.setLayoutManager(linearLayoutManager);
        compRep = new ComprasRepositorio(this);
        List<Compra> dados = compRep.ordenarComprasPorDataUp(compRep.comprasComProd(compRep.comprasEfetivadas(),produto, false));
        HistProdAdapter adapter = new HistProdAdapter(dados,produto.nome, this);
        lstHistProd.setAdapter(adapter);
        lstProdCompFut.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        lstProdCompFut.setLayoutManager(linearLayoutManager2);
        List<Compra> datasFut = compRep.comprasFuturas();
        List<Compra> datasFutProd = compRep.comprasComProd(datasFut, produto, false);
        HistProdFutAdapter adapter2 = new HistProdFutAdapter(datasFutProd,produto.nome);
        lstProdCompFut.setAdapter(adapter2);
    }

    public void definirParametrosProd(){
        codigo = produto.codigo;
        produto.duracao = produto.calcDuracao(this);
        txtNomeProd.setText(produto.nome);
        txtCategoria.setText(produto.categoria);
        lblDuracao.setText("Duração/" + produto.unidade);
        if((int)produto.duracao == 1){
            txtDuracao.setText("1 dia");
        }else if(produto.duracao != 0.0f){
            txtDuracao.setText(String.format("%.1f dias",produto.duracao));
        }else{
            txtDuracao.setText("-");
        }
        if (produto.preço != 0.00f) {
            txtPreco.setText(String.format("R$%.2f",produto.preço));
        } else {
            txtPreco.setText("-");
        }
        Compra ultimaComp = compRep.ultimaCompraComProd(produto,false);
        if(compRep.ultimaCompraComProd(produto,false) != null) {
            txtDiasUltComp.setText(subtracaoDatas(dataAtual(),ultimaComp.data) + " dias");
        }else{
            txtDiasUltComp.setText("-");
        }
        if(produto.duracao != 0.0f){
            String dataProxComp = compRep.calcComprasFuturas(produto, 1).get(0);
            txtDataVencimento.setText(dataProxComp);
            if (!opDatas.dataEmDia(dataProxComp)){
                lblDataVencimento.setText("Este produto acabou dia:");
            }
        }else{
            txtDataVencimento.setVisibility(View.GONE);
            lblDataVencimento.setVisibility(View.GONE);
        }
        try {
            criarListas();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

    public String dataAtual(){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy");
        return (formatarData.format(data));
    }

    public boolean eBissexto(int ano){
        boolean resp = false;
        if ((ano%400 == 0) || ((ano%4==0) && !(ano%100==0))){
            resp = true;
        }
        return resp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_detalhe, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                setResult(10);
                finish();
                break;

            case R.id.menuItem_edit:
                Intent it = new Intent(DetalhesProduto.this, NovoProduto.class);
                it.putExtra("PRODUTO", produto);
                startActivityForResult(it,2);
                break;
            case R.id.menuItem_del:
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setMessage("Tem certeza que deseja deletar o produto?");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prodRep.excluir(codigo);
                        Toast.makeText(getApplicationContext(),"Produto Excluido com sucesso",Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
                dlgDel.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        prodRep = new ProdutosRepositorio(this);
        produto = prodRep.buscarprod(codigo);
        definirParametrosProd();
    }
}
