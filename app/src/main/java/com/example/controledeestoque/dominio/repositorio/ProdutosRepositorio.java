package com.example.controledeestoque.dominio.repositorio;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.controledeestoque.database.BancoOpenHelper;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.entidades.Produto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProdutosRepositorio {

    private SQLiteDatabase conexao;
    private BancoOpenHelper openH;

    private ComprasRepositorio compRep;
    public BackupManager backupManager;

    private Context context;

    public ProdutosRepositorio(Context context) {
        this.context = context;
        openH = new BancoOpenHelper(context);
        backupManager = new BackupManager(context);
    }

    public void inserir(Produto produto){

        conexao = openH.getWritableDatabase();

        ContentValues cVal = new ContentValues();
        cVal.put("NOME", produto.nome);
        cVal.put("MARCA", produto.marca);
        cVal.put("CATEGORIA", produto.categoria);
        cVal.put("UNIDADE", produto.unidade);
        cVal.put("PRECO", produto.preço);
        cVal.put("QUANTIDADE",produto.quantidade);
        cVal.put("DURACAO",produto.duracao);

        conexao.insertOrThrow("PRODUTOS",null,cVal);

        conexao.close();

        backupManager.dataChanged();
    }

    public void alterar(Produto produto){

        conexao = openH.getWritableDatabase();

        ContentValues cVal = new ContentValues();
        cVal.put("NOME", produto.nome);
        cVal.put("MARCA", produto.marca);
        cVal.put("CATEGORIA", produto.categoria);
        cVal.put("UNIDADE", produto.unidade);
        cVal.put("PRECO", produto.preço);
        cVal.put("QUANTIDADE",produto.quantidade);
        cVal.put("DURACAO",produto.duracao);

        Produto prodAntigo = buscarprod(produto.codigo);
        if(!produto.nome.contentEquals(prodAntigo.nome) || !produto.marca.contentEquals(prodAntigo.marca) || !produto.categoria.contentEquals(prodAntigo.categoria)){
            alterarProdNasCompras(produto);
        }
        String[] parametros = new String[1];
        parametros[0] = Integer.toString(produto.codigo);
        conexao.update("PRODUTOS",cVal,"CODIGO = ?",parametros);

        conexao.close();

        backupManager.dataChanged();

    }

    public void alterarProdNasCompras(Produto produto){
        ComprasRepositorio compRep = new ComprasRepositorio(context);
        List<Compra> comprasComProduto = compRep.comprasComProd(compRep.buscarTodos(),produto,true);
        for(Compra compra:comprasComProduto){
            Produto prod = compra.prodEmCompra(produto.codigo);
            compra.produtos.remove(prod);
            prod.nome = produto.nome;
            prod.categoria = produto.categoria;
            compra.produtos.add(prod);
            compRep.alterar(compra);
        }
    }

    public void excluir(int codigo){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);

        conexao.delete("PRODUTOS","CODIGO = ?", parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public List<Produto> buscarTodos(){

        conexao = openH.getReadableDatabase();

        List<Produto> produtos = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("   SELECT CODIGO, NOME, MARCA, CATEGORIA, UNIDADE, PRECO, QUANTIDADE, DURACAO");
        sql.append("   FROM PRODUTOS");
        Cursor resultado = conexao.rawQuery(sql.toString(),null);
        if (resultado.getCount() > 0){
            resultado.moveToFirst();
            do{
                Produto prod = new Produto();
                prod.codigo = resultado.getInt(resultado.getColumnIndexOrThrow("CODIGO"));
                prod.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
                prod.marca = resultado.getString(resultado.getColumnIndexOrThrow("MARCA"));
                prod.categoria = resultado.getString(resultado.getColumnIndexOrThrow("CATEGORIA"));
                prod.unidade = resultado.getString(resultado.getColumnIndexOrThrow("UNIDADE"));
                prod.preço = resultado.getFloat(resultado.getColumnIndexOrThrow("PRECO"));
                prod.quantidade = resultado.getFloat(resultado.getColumnIndexOrThrow("QUANTIDADE"));
                prod.duracao = resultado.getFloat(resultado.getColumnIndexOrThrow("DURACAO"));

                produtos.add(prod);
            }while(resultado.moveToNext());
        }

        conexao.close();

        return produtos;
    }

    public Produto buscarprod(int codigo){

        conexao = openH.getReadableDatabase();

        Produto prod = new Produto();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CODIGO, NOME, MARCA, CATEGORIA, UNIDADE, PRECO, QUANTIDADE, DURACAO");
        sql.append(" FROM PRODUTOS");
        sql.append(" WHERE CODIGO = ?");

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);

        Cursor resultado = conexao.rawQuery(sql.toString(),parametros);

        if (resultado.getCount() > 0){
            resultado.moveToFirst();

            prod.codigo = resultado.getInt(resultado.getColumnIndexOrThrow("CODIGO"));
            prod.marca = resultado.getString(resultado.getColumnIndexOrThrow("MARCA"));
            prod.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
            prod.categoria = resultado.getString(resultado.getColumnIndexOrThrow("CATEGORIA"));
            prod.unidade = resultado.getString(resultado.getColumnIndexOrThrow("UNIDADE"));
            prod.preço = resultado.getFloat(resultado.getColumnIndexOrThrow("PRECO"));
            prod.quantidade = resultado.getFloat(resultado.getColumnIndexOrThrow("QUANTIDADE"));
            prod.duracao = resultado.getFloat(resultado.getColumnIndexOrThrow("DURACAO"));
            return prod;
        }

        conexao.close();

        return null;
    }

    public boolean backupBanco(){
        try{
            InputStream inputStream = new FileInputStream(new File(Environment.getDataDirectory() + "/data/com.example.controledeestoque/databases/PRODUTOS"));

            File pasta = new File(Environment.getExternalStorageDirectory() + "/Controle_De_Estoque/Backups");
            if(!pasta.exists()){
                pasta.mkdirs();
            }
            OutputStream outputStream = new FileOutputStream(new File(pasta + "/PRODUTOS_bkp"));

            byte[] buffer = new byte[1024];
            int comprimento;

            while((comprimento = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, comprimento);
            }
            inputStream.close();
            outputStream.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean restaurarBackupBanco(){
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getExternalStorageDirectory() + "/Controle_De_Estoque/Backups/PRODUTOS_bkp"));

            OutputStream outputStream = new FileOutputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controledeestoque/databases/PRODUTOS"));

            byte[] buffer = new byte[1024];
            int comprimento;

            while((comprimento = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, comprimento);
            }
            inputStream.close();
            outputStream.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int buscarCodProd(Produto prod){
        List<Produto> prods = buscarTodos();
        for(Produto p:prods){
            if(prod.nome.contentEquals(p.nome) && prod.categoria.contentEquals(p.categoria) && prod.marca.contentEquals(p.marca)){
                return p.codigo;
            }
        }
        return -1;
    }

    public void limparLista(){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        conexao.delete("PRODUTOS",null,null);

        conexao.close();
    }

    public void novaLista(List<Produto> produtos){
        limparLista();
        for (Produto prod:produtos){
            inserir(prod);
        }
    }

    public int prodJaExiste(Produto prod){
        List<Produto> list = buscarTodos();
        for(Produto p:list){
            if(p.nome.toUpperCase().contentEquals(prod.nome.toUpperCase())){
                return p.codigo;
            }
        }
        return 0;
    }

    public float somaTotalProdutos(List<Produto> prods) {
        float tot = 0.0f;
        for (Produto prod : prods) {
            if (prod.quantidade == 0.0f) {
                prod.quantidade = 1.0f;
            }
            tot += (prod.quantidade * prod.preço);
        }
        return tot;
    }

    public void atualizarProds(List<Produto> produtos){
        compRep = new ComprasRepositorio(context);
        for(Produto prod:produtos){
            prod.duracao = prod.calcDuracao(context);
            prod.codigo = buscarCodProd(prod);
            Compra ultComp = compRep.ultimaCompraComProd(prod,true);
            if(ultComp == null){
                prod.preço = 0.00f;
            }else{
                prod.preço = ultComp.prodEmCompra(prod.codigo).preço;
            }
            alterar(prod);
        }
    }

    public List<String> categoriasProds(){
        List<String> cats = new ArrayList<>();
        for(Produto p:buscarTodos()){
            p.categoria = p.categoria.trim();
            if("".contains(p.categoria)){
                p.categoria = "Sem Categoria";
            }
            if (!cats.contains(p.categoria)) {
                cats.add(p.categoria);
            }
        }
        Collections.sort(cats, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toUpperCase().compareTo(o2.toUpperCase());
            }
        });
        return cats;
    }

    public List<Produto> produtosDaCateg(String categ){
        List<Produto> prodsDaCateg = new ArrayList<>();
        for(Produto p:buscarTodos()){
            if(p.categoria.contentEquals(categ)){
                prodsDaCateg.add(p);
            }
        }
        return prodsDaCateg;
    }

    public String[] converterStringArray(String s){
        String[] array ;
        array = s.split(",");
        return array;
    }

    public String convertArrayString(String[] ar){
        if (ar != null) {
            if (ar.length > 0) {
                String result = ar[0];
                for (int i = 1; i < ar.length; i++) {
                    result = result.concat("," + ar[i]);
                }
                return result;
            }
        }
        return "";
    }

    public List<Produto> produtosIniciais(){
        List<Produto> prods = new ArrayList<>();
        Produto prod1 = new Produto();
        prod1.nome = "Arroz"; prod1.categoria = "Grãos";prod1.marca = "Empório São João";
        prods.add(prod1);
        Produto prod2 = new Produto();
        prod2.nome = "Leite"; prod2.categoria = "Laticínios";prod2.marca = "Italac";
        prods.add(prod2);
        Produto prod3 = new Produto();
        prod3.nome = "Ração"; prod3.categoria = "Animais"; prod3.marca = "Golden";
        prods.add(prod3);
        Produto prod4 = new Produto();
        prod4.nome = "Feijão"; prod4.categoria = "Grãos"; prod4.marca = "Rosalito";
        prods.add(prod4);
        Produto prod5 = new Produto();
        prod5.nome = "Detergente"; prod5.categoria = "Limpeza"; prod5.marca = "Ype";
        prods.add(prod5);
        Produto prod6 = new Produto();
        prod6.nome = "Shampoo"; prod6.categoria = "Banho"; prod6.marca = "Palmolive";
        prods.add(prod6);
        Produto prod7 = new Produto();
        prod7.nome = "Café"; prod7.categoria = "Matinais"; prod7.marca = "Caboclo";
        prods.add(prod7);
        Produto prod8 = new Produto();
        prod8.nome = "Molho de Tomate"; prod8.categoria = "Molhos"; prod8.marca = "Quero";
        prods.add(prod8);
        Produto prod9 = new Produto();
        prod9.nome = "Creme de Leite"; prod9.categoria = "Laticínios"; prod9.marca = "Italac";
        prods.add(prod9);
        Produto prod10 = new Produto();
        prod10.nome = "Macarrão"; prod10.categoria = "Massas"; prod10.marca = "Dona Benta";
        prods.add(prod10);

        compRep = new ComprasRepositorio(context);
        for(Compra comp:compRep.comprasEfetivadas()){
            for(Produto prod:comp.produtos){
                Produto novoProd = prod;
                for(Produto p:prods){
                    if(p.nome.contentEquals(prod.nome) && p.marca.contentEquals(prod.marca)){
                        prod.duracao = prod.calcDuracao(context);
                        prods.set(prods.indexOf(p),prod);
                        novoProd = null;
                    }
                }
                if(novoProd != null){
                    novoProd.duracao = novoProd.calcDuracao(context);
                    prods.add(novoProd);
                }
            }
        }
        return prods;
    }
}
