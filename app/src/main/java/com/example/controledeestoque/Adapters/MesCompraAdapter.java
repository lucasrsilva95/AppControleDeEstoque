package com.example.controledeestoque.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controledeestoque.OperacoesDatas;
import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.util.List;

public class MesCompraAdapter extends RecyclerView.Adapter<MesCompraAdapter.ViewHolderProduto> {

    private List<String> meses,mesesAbertos;
    private List<Compra> compras;
    private List<Compra> comprasMes;
    private OperacoesDatas opDatas;
    private Context context;

    public CompListAdapter comprasAdapter;

    public MesCompraAdapter(List<Compra> compras, List<String> mesesAbertos, Context context) {
        opDatas = new OperacoesDatas(context);
        this.meses = opDatas.mesesDatas(compras);
        this.mesesAbertos = mesesAbertos;
        this.compras = compras;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_mes_compra, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        if ((meses != null) && (meses.size() > 0)){
            String mes = meses.get(position);
            ComprasRepositorio compRep = new ComprasRepositorio(context);
            if (mes != null) {
                holder.txtMes.setText(mes.split("-")[1]);
                comprasMes = opDatas.comprasDoMes(mes, compras);
                holder.txtSomaVendas.setText(String.format("R$%.2f",compRep.somaTotalCompras(comprasMes)));
                if (mesesAbertos.contains(mes)) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    holder.lstVendas.setLayoutManager(linearLayoutManager);
                    comprasAdapter = new CompListAdapter(comprasMes,context);
                    holder.lstVendas.setAdapter(comprasAdapter);
                    holder.lstVendas.setVisibility(View.VISIBLE);
                } else {
                    holder.lstVendas.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return meses.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtMes,txtSomaVendas;
        public RecyclerView lstVendas;
        public ConstraintLayout layoutMes;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtMes = itemView.findViewById(R.id.txtMes);
            txtSomaVendas = itemView.findViewById(R.id.txtSomaVendas);
            lstVendas = itemView.findViewById(R.id.lstVendas);
            layoutMes = itemView.findViewById(R.id.layoutMes);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (meses.size() > 0) {
                        String mes = meses.get(getLayoutPosition());
                        if (!mesesAbertos.contains(mes)) {
                            mesesAbertos.add(mes);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            lstVendas.setLayoutManager(linearLayoutManager);
                            comprasMes = opDatas.comprasDoMes(mes, compras);
                            CompListAdapter comprasAdapter = new CompListAdapter(comprasMes,context);
                            lstVendas.setAdapter(comprasAdapter);
                            lstVendas.setVisibility(View.VISIBLE);
                        } else {
                            mesesAbertos.remove(mes);
                            lstVendas.setVisibility(View.GONE);
                        }
                        salvarMesesAbertos();
                    }
                }
            });
        }
    }

    public List<String> mesesAbertos(){
        return mesesAbertos;
    }

    public void salvarMesesAbertos(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String stringMeses = "";
        for(String mes:mesesAbertos){
            stringMeses += mes + ";";
        }
        prefs.edit().putString("MESES_ABERTOS",stringMeses).apply();
    }

    public Compra getContextMenuCompra(){
        return comprasAdapter.getContextMenuCompra();
    }
}
