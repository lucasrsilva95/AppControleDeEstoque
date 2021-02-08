package com.example.controledeestoque.dominio.repositorio;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.controledeestoque.OperacoesDatas;
import com.example.controledeestoque.Receiver;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static android.content.Context.*;

public class ComprasRepositorio {

    public SQLiteDatabase conexao;
    public BancoOpenHelper openH;
    public ProdutosRepositorio prodRep;
    public BackupManager backupManager;

    public OperacoesDatas opDatas;
    public Context context;

    public int periodComp,maxDiasComp,detPeriodComp, cont, horaNotif, minutoNotif, maxDiasAnalise;
    public String diaDaComp, tipoPeriod;
    public boolean dataComHora, notif, efetivada;

    public ComprasRepositorio(Context context) {
        this.conexao = conexao;
        this.context = context;
        openH = new BancoOpenHelper(context);
        prodRep = new ProdutosRepositorio(context);
        backupManager = new BackupManager(context);
        opDatas = new OperacoesDatas(context);
        definirConfigs(context);
    }

    public void inserir(Compra compra){

        conexao = openH.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("DATA",compra.data);
        cv.put("PRODUTOS",compra.convertProdListParaString(compra.produtos));
        cv.put("TOTAL",compra.total);
        cv.put("LOCAL",compra.local);
        if(compra.locMap == null){
            compra.locMap = "";
        }
        cv.put("LOCMAP",compra.locMap);
        if (compra.foto == null) {
            cv.put("LOC_IMAGEM", "");
        } else {
            cv.put("LOC_IMAGEM", compra.foto.getAbsolutePath());
        }
        cv.put("EFETIVADA",compra.efetivada);

//        conexao = openH.getWritableDatabase();
        conexao.insertOrThrow("COMPRAS",null,cv);
        conexao.close();
        backupManager.dataChanged();
    }

    public void alterar(Compra compra){

        conexao = openH.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("DATA",compra.data);
        cv.put("PRODUTOS",compra.convertProdListParaString(compra.produtos));
        cv.put("TOTAL",compra.total);
        cv.put("LOCAL",compra.local);
        cv.put("LOCMAP",compra.locMap);
        cv.put("EFETIVADA",compra.efetivada);
        if (compra.foto == null) {
            cv.put("LOC_IMAGEM", "");
        } else {
            cv.put("LOC_IMAGEM", compra.foto.getAbsolutePath());
        }

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(compra.codigo);

        conexao.update("COMPRAS",cv,"CODIGO = ?",parametros);
        conexao.close();

        backupManager.dataChanged();
    }

    public void excluir(int codigo){
        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);
        conexao.delete("COMPRAS","CODIGO = ?",parametros);
        conexao.close();

        backupManager.dataChanged();
    }

    public List<Compra> buscarTodos(){

        conexao = openH.getReadableDatabase();

        List<Compra> compras= new ArrayList<Compra>();

        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT CODIGO,DATA,TOTAL,PRODUTOS,LOCAL,LOCMAP,LOC_IMAGEM,EFETIVADA");
        sql.append(" FROM COMPRAS");
        Cursor resultado = conexao.rawQuery(sql.toString(),null);
        try {
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();
                do {
                    Compra compra = new Compra();

                    compra.codigo = resultado.getInt(resultado.getColumnIndexOrThrow("CODIGO"));
                    compra.data = resultado.getString(resultado.getColumnIndexOrThrow("DATA"));
                    compra.produtos = compra.convertStringParaProdList(resultado.getString(resultado.getColumnIndexOrThrow("PRODUTOS")));
                    compra.total = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL"));
                    compra.local = resultado.getString(resultado.getColumnIndexOrThrow("LOCAL"));
                    compra.locMap = resultado.getString(resultado.getColumnIndexOrThrow("LOCMAP"));
                    compra.foto = new File(resultado.getString(resultado.getColumnIndexOrThrow("LOC_IMAGEM")));
                    if ("".contains(compra.foto.getPath())) {
                        compra.foto = null;
                    }
                    if (compra.locMap == null){
                        compra.locMap = "";
                        alterar(compra);
                    }
                    compra.efetivada = resultado.getInt(resultado.getColumnIndexOrThrow("EFETIVADA")) > 0;

                    compras.add(compra);

                } while (resultado.moveToNext());
            }
        } catch (java.lang.RuntimeException e) {
            e.printStackTrace();
        }
        conexao.close();

        return compras;
    }


    public Compra buscarCompra(int codigo){

        conexao = openH.getReadableDatabase();

        Compra compra = new Compra();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT CODIGO,DATA,PRODUTOS,TOTAL,LOCAL,LOCMAP,LOC_IMAGEM,EFETIVADA");
        sql.append("  FROM COMPRAS");
        sql.append("  WHERE CODIGO = ?");

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);

        Cursor resultado = conexao.rawQuery(sql.toString(),parametros);

        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            compra.codigo = resultado.getInt(resultado.getColumnIndexOrThrow("CODIGO"));
            compra.data = resultado.getString(resultado.getColumnIndexOrThrow("DATA"));
            compra.produtos = compra.convertStringParaProdList(resultado.getString(resultado.getColumnIndexOrThrow("PRODUTOS")));
            compra.total = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL"));
            compra.local = resultado.getString(resultado.getColumnIndexOrThrow("LOCAL"));
            compra.locMap = resultado.getString(resultado.getColumnIndexOrThrow("LOCMAP"));
            compra.foto = new File(resultado.getString(resultado.getColumnIndexOrThrow("LOC_IMAGEM")));
            if ("".contains(compra.foto.getPath())) {
                compra.foto = null;
            }
            compra.efetivada = resultado.getInt(resultado.getColumnIndexOrThrow("EFETIVADA")) > 0;

            conexao.close();
            return compra;
        }
        conexao.close();
        return null;
    }

    public void limparLista(){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        conexao.delete("COMPRAS",null,null);

        conexao.close();
    }

    public void novaLista(List<Compra> compras){
        limparLista();
        for (Compra comp:compras){
            inserir(comp);
        }
    }

    public List<Compra> comprasComProd(List<Compra> compras,Produto produto, boolean analisarMarca){
        List<Compra> resp = new ArrayList<>();
        for(Compra comp:compras){
            for(Produto prod:comp.produtos){
                if (!analisarMarca) {
                    if(prod.nome.contentEquals(produto.nome)){
                        resp.add(comp);
                        break;
                    }
                } else {
                    if(prod.codigo == produto.codigo){
                        resp.add(comp);
                        break;
                    }
                }
            }
        }
        return resp;
    }

    public Compra ultimaCompraComProd(Produto produto, boolean analisarMarca){
        Compra ultComp = null;
        List<Compra> compras = comprasEfetivadas();
        if (compras.size() != 0) {
            List<Compra> compProd = ordenarComprasPorDataUp(comprasComProd(compras, produto, analisarMarca));
            if (compProd.size() != 0) {
                ultComp = compProd.get(0);
            }
        }
        return ultComp;
    }

    /**
     * Retorna uma lista com as datas das proximas compras de um determinado produto
     * @param produto Produto que sera analisado
     * @param numDatas Numero de datas de compras futuras que será retornado
     */
    public List<String> calcComprasFuturas(Produto produto, int numDatas){
        Produto prod = produto.copy();
        List<String> resp = new ArrayList<>();
        if(ultimaCompraComProd(prod, false) != null) {
            Compra ultComp = ultimaCompraComProd(prod, false);
            Produto produt = ultComp.prodEmCompra(prod.codigo);
            String ultData = ultComp.data;
            if (prod.quantidade == 0.0f){       // Consertar a quantidade estiver errada
                prod.quantidade = 1.0f;
            }
            if (prod.duracao != 0.0f) {
//                int numDatas = (int) ((maxDiasComp + 30) / (prod.duracao));
                ultData = opDatas.somaDataDia(ultData.substring(0, 10), (int) (prod.duracao * produt.quantidade));
                resp.add(ultData);
                for (int i = 2; i <= numDatas; i++) {
                    ultData = opDatas.somaDataDia(ultData.substring(0, 10), (int) (prod.duracao));
                    resp.add(ultData);
                }
            }
        }
        return resp;
    }

    public void atualizarComprasFuturas() {
        prodRep = new ProdutosRepositorio(context);
        List<Produto> prods = prodRep.buscarTodos();
        excluirComprasFuturas();
        String proxComp = diaComp();

        List<String> comprasCod = new ArrayList<>();
        for(Produto prod:prodRep.buscarTodos()){
            List<String> datasProd = calcComprasFuturas(prod,(int)(maxDiasAnalise/prod.duracao));
            for(String dataComp:datasProd){
                comprasCod.add(String.format("%s - %d",dataComp,prod.codigo));
            }
        }
        int numComp = 0, multData = -1;
        if(comprasCod.size() == 0){
            return;
        }
        if (periodComp != 0) {
            while (numComp < 10 && multData <= maxDiasAnalise) {
                Compra comp = new Compra();
                comp.efetivada = false;
                if (multData >= 0) {
                    comp.data = opDatas.somaDataDia(proxComp, multData * periodComp);
                }
                for(String compCod:comprasCod){
                    String data = compCod.split("-")[0];
                    int cod = Integer.parseInt(compCod.split("-")[1].trim());
                    if(multData < 0){
                        if (opDatas.subtracaoDatas(data,proxComp) < 0) {
                            if (comp.data == null) {
                                if (opDatas.subtracaoDatas(data,opDatas.dataAtual()) > 0) {
                                    comp.data = data;
                                } else {
                                    comp.data = opDatas.dataAtual();
                                }
                            }
                            if (comp.prodEmCompra(cod) == null) {
                                Produto p = prodRep.buscarprod(cod);
                                p.quantidade = 1;
                                comp.produtos.add(p);
                            } else {
                                float quant = comp.prodEmCompra(cod).quantidade;
                                comp.produtos.remove(comp.prodEmCompra(cod));
                                Produto produt = prodRep.buscarprod(cod);
                                produt.quantidade = quant + 1;
                                comp.produtos.add(produt);
                            }
                        }
                    }else{
                        int difDatas = opDatas.subtracaoDatas(data, comp.data);
                        if(difDatas >= 0 && difDatas < periodComp){ // data > dataCompra e data < dataCompra + period
                            if (comp.prodEmCompra(cod) == null) {
                                Produto p = prodRep.buscarprod(cod);
                                p.quantidade = 1;
                                comp.produtos.add(p);
                            } else {
                                float quant = comp.prodEmCompra(cod).quantidade;
                                comp.produtos.remove(comp.prodEmCompra(cod));
                                Produto produt = prodRep.buscarprod(cod);
                                produt.quantidade = quant + 1;
                                comp.produtos.add(produt);
                            }
                        }
                    }
                }
                if (comp.produtos.size() != 0) {
                    comp.total = comp.totalCompra();
                    if(compraDeHoje() != null && comp.data.contentEquals(opDatas.dataAtual())){
                        comp = inserirProdutosComp1EmComp2(compraDeHoje(), comp, false);
                        excluir(compraDeHoje().codigo);
                    }
                    inserir(comp);
                    numComp++;
                }
                multData++;
            }
        }
        else {
//            String dataInicial = dataAtual(false);
            while (numComp < 10 && opDatas.subtracaoDatas(proxComp,opDatas.dataAtual()) <= 365) {
                Compra comp = new Compra();
                comp.efetivada = false;
                if (multData >= 0) {
                    comp.data = new String(proxComp);
                    proxComp = definirProxComp(opDatas.somaDataDia(proxComp, 1));
                }
                for (String compCod : comprasCod) {
                    String data = compCod.split("-")[0];
                    int cod = Integer.parseInt(compCod.split("-")[1].trim());
                    if (multData < 0) {
                        if (opDatas.subtracaoDatas(data, proxComp) < 0) {
                            if (comp.data == null) {
                                if (opDatas.subtracaoDatas(data,opDatas.dataAtual()) > 0) {
                                    comp.data = data;
                                } else {
                                    comp.data = opDatas.dataAtual();
                                }
                            }
                            if (comp.prodEmCompra(cod) == null) {
                                Produto p = prodRep.buscarprod(cod);
                                p.quantidade = 1;
                                comp.produtos.add(p);
                            } else {
                                float quant = comp.prodEmCompra(cod).quantidade;
                                comp.produtos.remove(comp.prodEmCompra(cod));
                                Produto produt = prodRep.buscarprod(cod);
                                produt.quantidade = quant + 1;
                                comp.produtos.add(produt);
                            }
                        }
                    } else {
                        if (opDatas.subtracaoDatas(data, comp.data) >= 0 && opDatas.subtracaoDatas(data, proxComp) < 0) { // data > dataCompra e data < dataCompra + period
                            if (comp.prodEmCompra(cod) == null) {
                                Produto p = prodRep.buscarprod(cod);
                                p.quantidade = 1;
                                comp.produtos.add(p);
                            } else {
                                float quant = comp.prodEmCompra(cod).quantidade;
                                comp.produtos.remove(comp.prodEmCompra(cod));
                                Produto produt = prodRep.buscarprod(cod);
                                produt.quantidade = quant + 1;
                                comp.produtos.add(produt);
                            }
                        }
                    }
                }
                if (comp.produtos.size() != 0) {
                    comp.total = comp.totalCompra();
                    if(compraDeHoje() != null && comp.data.contentEquals(opDatas.dataAtual())){
                        comp = inserirProdutosComp1EmComp2(compraDeHoje(), comp, false);
                        excluir(compraDeHoje().codigo);
                    }
                    inserir(comp);
                    numComp++;
                }
                multData++;
            }
        }
    }

    /**
     *
     * @return Retorna uma lista com todas as compras futuras previstas
     */
    public List<Compra> comprasFuturas(){
        List<Compra> compFut = new ArrayList<>();
        for(Compra comp:buscarTodos()){
            if(!comp.efetivada){
                if (comp.data != null && opDatas.subtracaoDatas(comp.data,dataAtual(false)) >= 0) {
                    compFut.add(comp);
                } else {
                    excluir(comp.codigo);
                }
            }
        }
        return ordenarComprasPorDataDown(compFut);
    }

    public Compra inserirProdutosComp1EmComp2(Compra comp1, Compra comp2, boolean aumentarQuantEmProdsIguais){
        List<Produto> prodsNovos = new ArrayList<>(comp1.produtos);
        prodsNovos.addAll(comp2.produtos);
        for(Produto prod1:comp1.produtos){
            for(Produto prod2:comp2.produtos){
                if(prod1.codigo == prod2.codigo){
                    prodsNovos.remove(prod1);
                    if (aumentarQuantEmProdsIguais) {
                        prodsNovos.remove(prod2);
                        prod2.quantidade += prod1.quantidade;
                        prodsNovos.add(prod2);
                    }
                }
            }
        }
        comp2.produtos.clear();
        comp2.produtos.addAll(prodsNovos);
        return comp2;
    }

    public void excluirComprasFuturas(){
        for(Compra comp:comprasFuturas()){
            excluir(comp.codigo);
        }
    }


    public List<Compra> comprasEfetivadas(){
        List<Compra> compras = new ArrayList<>();
        for(Compra comp:buscarTodos()){
            if(comp.efetivada){
                compras.add(comp);
            }
        }
        return compras;
    }

    public List<Produto> estoqueAtual(){
        List<Produto> estoqueComDuracao = new ArrayList<>();
        List<Produto> estoqueSemDuracao = new ArrayList<>();
        List<Produto> estoque = new ArrayList<>();
        for(Produto p:prodRep.buscarTodos()){
            Compra ultComp = ultimaCompraComProd(p,false);
            if (ultComp != null) {
                if (calcComprasFuturas(p,1).size() > 0) {         // Produto comprado mais de uma vez
                    String dataProxComp = calcComprasFuturas(p,1).get(0);
                    int diasAteProxComp = opDatas.subtracaoDatas(dataProxComp, dataAtual(false));
                    int diasDesdeUltComp = opDatas.subtracaoDatas(dataAtual(false), ultComp.data);
                    if (diasAteProxComp > 0) {                          // Produto em estoque
                        p.quantidade = ultComp.prodEmCompra(p.codigo).quantidade - ((1/p.duracao)*diasDesdeUltComp);
                    } else {                                    //Produto Vencido
                        p.quantidade = 0.0f;
                    }
                    estoqueComDuracao.add(p);
                }else {
                    estoqueSemDuracao.add(p);
                }
            }
        }
        Collections.sort(estoqueComDuracao, new Comparator<Produto>() {
            @Override
            public int compare(Produto o1, Produto o2) {
                return Float.compare(o1.quantidade, o2.quantidade);
            }
        });
        estoque.addAll(estoqueComDuracao);
        estoque.addAll(estoqueSemDuracao);
        int size = estoque.size();
        for(int i = 0; i < size ; i++){
            Produto prod1 = estoque.get(i);
            for(int j = i+1; j < size ; j++){
                Produto prod2 = estoque.get(j);
                if(prod1.nome.contentEquals(prod2.nome) && !prod1.marca.contentEquals(prod2.marca) && (ultimaCompraComProd(prod1,true) != null) && (ultimaCompraComProd(prod2,true) != null)){
                    int dif = opDatas.subtracaoDatas(ultimaCompraComProd(prod1,true).data,ultimaCompraComProd(prod2,true).data);
                    if (dif != 0) {
                        if(dif > 0){
                            estoque.remove(prod2);
                        }else{
                            estoque.remove(prod1);
                        }
                        size--;
                    }
                }
            }
        }

        return estoque;
    }

    /**
     * Retorna a proxima compra que está prevista com determinado produto.
     * @param produto produto a ser analisado
     * @return retorna a proxima compra prevista com o produto, se esta compra ainda não estiver programada ou se o produto ainda não possui duração, retorna null
     */
    public Compra proxCompraComProd(Produto produto){
        if(comprasComProd(comprasFuturas(),produto,false).size() == 0){
            return null;
        }
        return comprasComProd(comprasFuturas(),produto,false).get(0);
    }

    public String dataAtual(boolean horaMin){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        if (horaMin) {
            return (formatarData.format(data));
        } else {
            return (formatarData.format(data).substring(0,10));
        }
    }

    public String diaDaSemana(String data) {
        String diaSem = "";
        data = data.substring(0,10);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        int diaDaSemana = gc.get(GregorianCalendar.DAY_OF_WEEK);
        switch (diaDaSemana){
            case 1:
                diaSem = "Domingo";
                break;
            case 2:
                diaSem = "Segunda-Feira";
                break;
            case 3:
                diaSem = "Terça-Feira";
                break;
            case 4:
                diaSem = "Quarta-Feira";
                break;
            case 5:
                diaSem = "Quinta-Feira";
                break;
            case 6:
                diaSem = "Sexta-Feira";
                break;
            case 7:
                diaSem = "Sábado";
                break;
        }

        return diaSem;
    }

    public String diaComp() {
        String data = dataAtual(false);
        if("Semanal".contains(tipoPeriod)){
            while(!diaDaComp.contains(diaDaSemana(data))) {
                data = opDatas.somaDataDia(data, 1);
            }
        }else{
            while(!diaDaComp.contains(data.substring(0,2))) {
                data = opDatas.somaDataDia(data, 1);
            }
        }
        return data;
    }

    public float somaTotalCompras(List<Compra> compras){
        float soma = 0.0f;
        for(Compra comp:compras){
            soma += comp.total;
        }
        return soma;
    }

//    public String opDatas.somaDataDia(String data, int dias){
//        int dia = Integer.parseInt(data.substring(0, 2));
//        int mes = Integer.parseInt(data.substring(3, 5));
//        int ano = Integer.parseInt(data.substring(6, 10));
//        while(dias > 0){
//            if(!(((dia + dias) < 28) || (((dia + dias) <= 30) && (("469".contains(Integer.toString(mes)) || mes == 11))) || (((dia + dias) <= 31) && (("13578".contains(Integer.toString(mes)) || mes == 12 || mes == 10))) || ((dia + dias) == 29 && mes == 2 && eBissexto(ano)))){
//                if(mes == 2){
//                    if (eBissexto(ano)) {
//                        dias -= 29;
//                    } else {
//                        dias -= 28;
//                    }
//                }else if(("13578".contains(Integer.toString(mes)) || mes == 12 || mes == 10)){
//                    dias -= 31;
//
//                } else{
//                    dias -= 30;
//                }
//                mes++;
//                if(mes > 12){
//                    mes = 1;
//                    ano++;
//                }
//
//            }else{
//                break;
//            }
//
//        }
//        dia += dias;
//        String dataRes = String.format("%02d/%02d/%02d%s",dia,mes,ano,data.substring(10));
//        return dataRes;
//    }

    public boolean eBissexto(int ano){
        boolean resp = false;
        if ((ano%400 == 0) || ((ano%4==0) && !(ano%100==0))){
            resp = true;
        }
        return resp;
    }

    public List<Compra> ordenarComprasPorDataDown(List<Compra> compras) {
        List<Compra> compOrd = compras;

        for (int i1 = compras.size() - 1; i1 >= 1; i1--) {
            for (int i2 = i1 - 1; i2 >= 0; i2--) {
                int ano1 = Integer.parseInt(compras.get(i1).data.substring(6,10));
                int mes1 = Integer.parseInt(compras.get(i1).data.substring(3, 5));
                int dia1 = Integer.parseInt(compras.get(i1).data.substring(0, 2));
                int ano2 = Integer.parseInt(compras.get(i2).data.substring(6, 10));
                int mes2 = Integer.parseInt(compras.get(i2).data.substring(3, 5));
                int dia2 = Integer.parseInt(compras.get(i2).data.substring(0, 2));
                int hora1 = 0;
                int minuto1 = 0;
                int hora2 = 0;
                int minuto2 = 0;
                if (compras.get(i1).data.length() > 13 && compras.get(i2).data.length() > 13) {
                    hora1 = Integer.parseInt(compras.get(i1).data.substring(13, 15));
                    minuto1 = Integer.parseInt(compras.get(i1).data.substring(16, 18));
                    hora2 = Integer.parseInt(compras.get(i2).data.substring(13, 15));
                    minuto2 = Integer.parseInt(compras.get(i2).data.substring(16, 18));
                }
                if (ano1 < ano2) {
                    compOrd.add(i1 + 1, compOrd.get(i2));
                    compOrd.remove(i2);
                } else if (ano1 == ano2) {
                    if (mes1 < mes2) {
                        compOrd.add(i1 + 1, compOrd.get(i2));
                        compOrd.remove(i2);
                    } else if (mes1 == mes2) {
                        if (dia1 < dia2) {
                            compOrd.add(i1 + 1, compOrd.get(i2));
                            compOrd.remove(i2);
                        } else if (dia1 == dia2) {
                            if (hora1 < hora2) {
                                compOrd.add(i1 + 1, compOrd.get(i2));
                                compOrd.remove(i2);
                            } else if (hora1 == hora2) {
                                if (minuto1 < minuto2) {
                                    compOrd.add(i1 + 1, compOrd.get(i2));
                                    compOrd.remove(i2);
                                }
                            }
                        }
                    }
                }
            }
        }
        return compOrd;
    }

    public List<Compra> ordenarComprasPorDataUp(List<Compra> compras) {
        List<Compra> dados = ordenarComprasPorDataDown(compras);
        List<Compra> compOrdUp = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            compOrdUp.add(dados.get(dados.size() - 1 - i));
        }
        return compOrdUp;
    }

    public void definirNotificacoes(boolean notifCompHoje){

        createNotificationChannel();

        OperacoesDatas opDatas = new OperacoesDatas(context);
        int codeExcluir = 0;
        if (comprasFuturas().size() > 0) {

            List<Compra> comprasfut = comprasFuturas();
            int code = 1;
            for (Compra compfut:comprasfut) {
                String data = compfut.data;
                if ((notifCompHoje) || (!notifCompHoje && opDatas.eNotifFutura(data, horaNotif, minutoNotif))) {

                    int dia = Integer.parseInt(data.substring(0, 2));
                    int mes = Integer.parseInt(data.substring(3, 5));
                    int ano = Integer.parseInt(data.substring(6, 10));

                    Calendar dataNotif = Calendar.getInstance();

                    dataNotif.set(ano, mes - 1, dia, horaNotif, minutoNotif, 0);

                    Intent intent = new Intent(context, Receiver.class);
                    intent.putExtra("COMPFUT", compfut);
                    PendingIntent pendingIntent;
                    while (codeExcluir <= 90) {
                        pendingIntent = PendingIntent.getBroadcast(context, codeExcluir, intent, PendingIntent.FLAG_NO_CREATE);
                        if (pendingIntent != null) {
                            pendingIntent.cancel();
                        }
                        codeExcluir++;
                    }
                    pendingIntent = PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    alarm.set(AlarmManager.RTC_WAKEUP, dataNotif.getTimeInMillis(), pendingIntent);
                    code++;
                }
            }
        }
    }

    public void definirNotificacoes(){
        this.definirNotificacoes(false);
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Compras do dia";
            String description = "Channel for Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyCompra",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public String definirProxComp(String dataInicial){
        String dataAnalise = dataInicial;
        for (int i = 0; i < detPeriodComp; i++) {
            if(i != 0){
                dataAnalise = opDatas.somaDataDia(dataAnalise,1);
            }
            while(!diaDaComp.contentEquals(dataAnalise.substring(0,2))){
                dataAnalise = opDatas.somaDataDia(dataAnalise,1);
            }
        }
        return dataAnalise;
    }

    public void definirConfigs(Context BaseContext){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        tipoPeriod = prefs.getString("period_comp","Semanal");
        detPeriodComp = Integer.parseInt(prefs.getString("detalhe_period_comp", "1"));
        if ("Semanal".contains(tipoPeriod)) {
            periodComp = 7 * detPeriodComp;
            diaDaComp = prefs.getString("diaSemana_comp", "Domingo");
            maxDiasAnalise = periodComp*11;
        }else{
            periodComp = 0;
            int dia = Integer.parseInt(prefs.getString("diaMes_comp", "1"));
            diaDaComp = String.format("%02d",dia);
            maxDiasAnalise = 31 * detPeriodComp * 11;
        }

        dataComHora = prefs.getBoolean("horaMin_switch",true);
        maxDiasComp = Integer.parseInt(prefs.getString("max_comp", "60"));
        notif = prefs.getBoolean("switch_notific", true);
        String horaConfig = prefs.getString("horaNotif", "09:00");
        horaNotif = Integer.parseInt(horaConfig.substring(0,2));
        minutoNotif = Integer.parseInt(horaConfig.substring(3,5));
    }

    public List<Produto> addCategorias(List<Produto> prods){
        if (prods.size() != 0) {
            List<Produto> semCat = new ArrayList<>();
            List<Produto> comCat = new ArrayList<>();
            Collections.sort(prods, new Comparator<Produto>() {
                @Override
                public int compare(Produto o1, Produto o2) {
                    return o1.nome.compareTo(o2.nome);
                }
            });
            for(Produto p:prods){
                if("".contains(p.categoria)){
                    p.categoria = "Sem Categoria";
                    semCat.add(p);
                }else{
                   comCat.add(p);
                }
            }
            Collections.sort(comCat, new Comparator<Produto>() {
                @Override
                public int compare(Produto o1, Produto o2) {
                    return o1.categoria.compareTo(o2.categoria);
                }
            });
            comCat.addAll(semCat);
            prods = comCat;
            Produto p = new Produto();
            p.categoria = prods.get(0).categoria;
            prods.add(0, p);
            for(int i = 1; i < prods.size(); i++){
                if(!(prods.get(i).categoria.contains(prods.get(i-1).categoria))){
                    Produto p2 = new Produto();
                    p2.categoria = prods.get(i).categoria;
                    prods.add(i, p2);
                }
            }
        }

        return prods;
    }

    public Compra compraDeHoje(){
        List<Compra> comprasFut = comprasFuturas();
        for(Compra comp:comprasFut){
            if(comp.data.contains(dataAtual(false))){
                return comp;
            }
        }
        return null;
    }

    public void consertarCodigoProdsNasCompras(){
        ProdutosRepositorio prodRep = new ProdutosRepositorio(context);
        for(Compra compra:buscarTodos()){
            for(Produto prod:compra.produtos){
                if(prod.codigo == 0){
                    prod.codigo = prodRep.buscarCodProd(prod);
                }
            }
            alterar(compra);
        }
    }

    public String obterCoordLocal(String local){
        for(Compra comp:buscarTodos()){
            if(comp.local.contentEquals(local) && comp.locMap != null && !comp.locMap.contentEquals("") ){
                return comp.locMap;
            }
        }
        return "";
    }


}
