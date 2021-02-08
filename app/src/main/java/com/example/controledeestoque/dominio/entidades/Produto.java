package com.example.controledeestoque.dominio.entidades;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.controledeestoque.OperacoesDatas;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Produto implements Serializable {
    public int codigo;
    public String nome;
    public String marca;
    public String categoria;
    public String unidade;
    public float preço;
    public float quantidade;
    public float duracao;

    public Produto() {
        this.nome = "";
        this.marca = "";
        this.categoria = "";
        this.unidade = "un";
        this.quantidade = 1.0f;
    }

    public float calcDuracao(Context context){
        ComprasRepositorio compRep = new ComprasRepositorio(context);
        OperacoesDatas opDatas = new OperacoesDatas(context);
        float duracaoMedia = 0.0f;
        int difDatas = 0;
        List<Compra> ultimasCompras = compRep.ordenarComprasPorDataUp(compRep.comprasComProd(compRep.comprasEfetivadas(),this, false));
        if(ultimasCompras.size() > 1) {
            float penultQuant = 0;
            float somaDuracao = 0.0f;
            for (int i=1 ; i < ultimasCompras.size(); i++) {
                Compra ultComp = ultimasCompras.get(i-1);
                Compra penultComp = ultimasCompras.get(i);
                penultQuant = penultComp.prodEmCompra(codigo).quantidade;
                difDatas = opDatas.subtracaoDatas(ultComp.data, penultComp.data);
                duracao = difDatas / penultQuant;
                somaDuracao += duracao;
            }
            duracaoMedia = somaDuracao / (ultimasCompras.size()-1);
        }
        return duracaoMedia;
    }

    public Produto copy(){
        Produto resp = new Produto();
        resp.codigo = codigo;
        resp.nome = nome;
        resp.marca = marca;
        resp.categoria = categoria;
        resp.quantidade = quantidade;
        resp.duracao = duracao;
        resp.unidade = unidade;
        resp.preço = preço;

        return resp;
    }

}
