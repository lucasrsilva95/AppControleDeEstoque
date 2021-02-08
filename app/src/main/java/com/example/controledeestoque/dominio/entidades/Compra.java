package com.example.controledeestoque.dominio.entidades;

import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URI;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Compra implements Serializable {
    public int codigo;
    public String data;
    public List<Produto> produtos;
    public float total;
    public String local;
    public String locMap;
    public File foto;
    public boolean efetivada;

    public Compra() {
        this.local = "";
        this.locMap = "";
        this.produtos = new ArrayList<>();
        this.efetivada = true;
    }

    public String convertProdListParaString(List<Produto> prods){
        String result = "";
        if (prods.size() > 0){
            for(Produto prod:prods){
                if("un".contains(prod.unidade)){
                    prod.quantidade = (int)prod.quantidade;
                }
                result = result.concat(String.format("%d;%s;%s;%s;%.2f;%.2f;%.1f;%s-",prod.codigo,prod.nome,prod.marca,prod.categoria,prod.preço,prod.quantidade,prod.duracao, prod.unidade));
            }
            return result;
        }
        return "";
    }

    public int numItens(){
        int res = 0;
        for(Produto prod:produtos){
            if (prod.unidade.contains("un")) {
                res += prod.quantidade;
            } else if(prod.unidade.contains("Kg")) {
                res += 1;
            }
        }
        return res;
    }

    public List<Produto> convertStringParaProdList(String s){
        List<Produto> prods = new ArrayList<>();
        if("".contentEquals(s)){
            return prods;
        }
        s = s.replace(",",".");
        String[] comp = s.split("-");
        for(int i=0;i<comp.length;i++){
            Produto prod = new Produto();
            String[] cat = comp[i].split(";");
            if (cat.length > 7) {
                prod.codigo = Integer.parseInt(cat[0]);
                prod.nome = cat[1];
                prod.marca = cat[2];
                prod.categoria = cat[3];
                prod.preço = Float.parseFloat(cat[4]);
                prod.quantidade = Float.parseFloat(cat[5]);
                prod.duracao = Float.parseFloat(cat[6]);
                prod.unidade = cat[7];
            } else {
                prod.nome = cat[0];
                prod.marca = cat[1];
                prod.categoria = cat[2];
                prod.preço = Float.parseFloat(cat[3]);
                prod.quantidade = Float.parseFloat(cat[4]);
                prod.duracao = Float.parseFloat(cat[5]);
                prod.unidade = cat[6];
            }
            prods.add(prod);
        }
        return prods;
    }

    public Float totalCompra(){
        float tot = 0.00f;
        for(Produto prod:produtos){
            tot += (prod.quantidade * prod.preço);
        }
        return tot;
    }

    public Produto prodEmCompra(int codigoProd){
        List<Produto> prods = produtos;
        for(Produto p:prods){
            if(p.codigo == codigoProd){
                return p;
            }
        }
        return null;
    }

    public void deletarFoto(){
        foto.delete();
    }
}
