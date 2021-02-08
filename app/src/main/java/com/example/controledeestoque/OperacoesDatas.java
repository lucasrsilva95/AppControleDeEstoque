package com.example.controledeestoque;

import android.content.Context;

import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OperacoesDatas {

    public Context context;

    public OperacoesDatas(Context context) {
        this.context = context;
    }


    public List<String> ordenarDatasDown(List<String> datas) {
        List<String> datasOrd = datas;

        for (int i1 = datas.size() - 1; i1 >= 1; i1--) {
            for (int i2 = i1 - 1; i2 >= 0; i2--) {
                String data1 = datas.get(i1).split("=")[0];
                String data2 = datas.get(i2).split("=")[0];
                int ano1 = Integer.parseInt(data1.substring(6,10));
                int mes1 = Integer.parseInt(data1.substring(3, 5));
                int dia1 = Integer.parseInt(data1.substring(0, 2));
                int ano2 = Integer.parseInt(data2.substring(6, 10));
                int mes2 = Integer.parseInt(data2.substring(3, 5));
                int dia2 = Integer.parseInt(data2.substring(0, 2));
                int hora1 = 0;
                int minuto1 = 0;
                int hora2 = 0;
                int minuto2 = 0;
                if (data1.length() > 13 && data2.length() > 13) {
                    hora1 = Integer.parseInt(data1.substring(13, 15));
                    minuto1 = Integer.parseInt(data1.substring(16, 18));
                    hora2 = Integer.parseInt(data2.substring(13, 15));
                    minuto2 = Integer.parseInt(data2.substring(16, 18));
                }
                if (ano1 < ano2) {
                    datasOrd.add(i1 + 1, datasOrd.get(i2));
                    datasOrd.remove(i2);
                } else if (ano1 == ano2) {
                    if (mes1 < mes2) {
                        datasOrd.add(i1 + 1, datasOrd.get(i2));
                        datasOrd.remove(i2);
                    } else if (mes1 == mes2) {
                        if (dia1 < dia2) {
                            datasOrd.add(i1 + 1, datasOrd.get(i2));
                            datasOrd.remove(i2);
                        } else if (dia1 == dia2) {
                            if (hora1 < hora2) {
                                datasOrd.add(i1 + 1, datasOrd.get(i2));
                                datasOrd.remove(i2);
                            } else if (hora1 == hora2) {
                                if (minuto1 < minuto2) {
                                    datasOrd.add(i1 + 1, datasOrd.get(i2));
                                    datasOrd.remove(i2);
                                }
                            }
                        }
                    }
                }
            }
        }
        return datasOrd;
    }

    public int subtracaoDatas(String stringData1, String stringData2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date data1 = null;
        Date data2 = null;
        long dif = 0;
        try {
            data1 = dateFormat.parse(stringData1);
            data2 = dateFormat.parse(stringData2);
            dif = data1.getTime() - data2.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int difResultado = (int) (dif / 1000 / 60/60/24);

        return difResultado;
    }

    public String somaDataDia(String data, int dias){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date data1 = null;
        try {
            data1 = dateFormat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diasMili = (long)dias*24*60*60*1000;

        long result = data1.getTime() + diasMili;

        Date dateResult = new Date(result);
        return dateFormat.format(dateResult);

    }

    public List<String> mesesDatas(List<Compra> compras){
        List<String> meses = new ArrayList<>();
        ComprasRepositorio vendRep = new ComprasRepositorio(context);
        compras = vendRep.ordenarComprasPorDataUp(compras);
        for(Compra compra:compras){
            if (!meses.contains(formatMesAno(compra.data))) {
                meses.add(formatMesAno(compra.data));
            }
        }
        return meses;
    }

    public List<Compra> comprasDoMes(String mes, List<Compra> compras){
        List<Compra> comprasMes = new ArrayList<>();
        ComprasRepositorio compRep = new ComprasRepositorio(context);
        for(Compra compra:compras){
            if(compra.data.contains(mes.split("-")[0])){
                comprasMes.add(compra);
            }
        }
        return compRep.ordenarComprasPorDataUp(comprasMes);
    }

    public boolean dataEmDia(String data){
        return (subtracaoDatas(data,dataAtual()) >= 0);
    }

    public boolean eNotifFutura(String data, int horaNotif, int minNotif){
        if(subtracaoDatas(data,dataAtual()) > 0){
            return true;
        }else if(subtracaoDatas(data,dataAtual()) == 0){
            int horaAtual = Integer.parseInt(horaMinAtual().split(":")[0]);
            int minAtual = Integer.parseInt(horaMinAtual().split(":")[1]);
            if(horaAtual < horaNotif){
                return true;
            }else if(horaAtual == horaNotif && minAtual < minNotif){
                return true;
            }
        }
        return false;
    }

    public String formatMesAno(String data){
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Date dataMes = null;
        try {
            dataMes = formato.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMMM", Locale.getDefault());
        String mes = simpleDateFormat.format(dataMes);

        return data.substring(3,10) + "-" + mes;
    }


    public boolean eBissexto(int ano){
        boolean resp = false;
        if ((ano%400 == 0) || ((ano%4==0) && !(ano%100==0))){
            resp = true;
        }
        return resp;
    }

    public String dataAtual(){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy");
        return (formatarData.format(data));
    }

    public String horaMinAtual(){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData;
        formatarData = new SimpleDateFormat("HH:mm");
        return (formatarData.format(data));
    }
}
