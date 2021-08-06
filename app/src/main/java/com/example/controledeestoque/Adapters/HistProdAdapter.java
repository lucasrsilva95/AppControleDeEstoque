package com.example.controledeestoque.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controledeestoque.DetalhesCompra;
import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.entidades.Produto;

import java.util.List;

public class HistProdAdapter extends RecyclerView.Adapter<HistProdAdapter.ViewHolderHistProd> {

    private List<Compra> dados;
    private String produto;
    private boolean dataComHora;
    private Context context;

    public HistProdAdapter(List<Compra> dados, String produto, Context context) {
        this.dados = dados;
        this.produto = produto;
        this.context = context;
    }

    @NonNull
    @Override
    public HistProdAdapter.ViewHolderHistProd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view =layoutInflater.inflate(R.layout.linha_historicoprod, parent, false);

        ViewHolderHistProd holderHistProd = new ViewHolderHistProd(view,parent.getContext());

        return holderHistProd;
    }

    @Override
    public void onBindViewHolder(@NonNull HistProdAdapter.ViewHolderHistProd holder, int position) {


        if ((dados != null) && (dados.size() > 0)){
            Compra compra = dados.get(position);
            holder.txtLocal.setText(compra.local);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            dataComHora = prefs.getBoolean("horaMin_switch",true);
            if (dataComHora) {
                holder.txtData.setText(compra.data);
            } else {
                holder.txtData.setText(compra.data.substring(0,10));
            }
            Produto prod = prodEmCompra(compra, produto);
            holder.txtMarca.setText(prod.marca);
            holder.txtPrecoUni.setText(String.format("R$%.2f",prod.preço));
            holder.lblUnidade.setText(prod.unidade);
            if ("un".contains(prod.unidade)) {
                holder.txtQuant.setText(String.format("%.0f",prod.quantidade));
                holder.lblValUniKg.setText("Preço/un:");
            } else {
                holder.txtQuant.setText(String.format("%.2f",prod.quantidade));
                holder.lblValUniKg.setText("Preço/Kg:");
            }
            holder.txtValTotProd.setText(String.format("R$%.2f",(prod.quantidade * prod.preço)));
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderHistProd extends RecyclerView.ViewHolder{

        public TextView txtLocal,txtData,txtPrecoUni,txtQuant, txtValTotProd, lblUnidade, lblValUniKg, txtMarca;

        public ViewHolderHistProd(@NonNull View itemView, final Context context) {
            super(itemView);

            txtLocal = itemView.findViewById(R.id.txtLocalComp);
            txtMarca = itemView.findViewById(R.id.txtMarca);
            txtData = itemView.findViewById(R.id.txtDataHora);
            txtPrecoUni = itemView.findViewById(R.id.txtPrecoUni);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtValTotProd = itemView.findViewById(R.id.txtValTotProd);
            lblUnidade = itemView.findViewById(R.id.lblUniHistProd);
            lblValUniKg = (TextView) itemView.findViewById(R.id.lblValUniKg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0){
                        Compra compra = dados.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesCompra.class);
                        it.putExtra("COMPRA", compra);
                        ((AppCompatActivity)context).startActivityForResult(it,2);
                    }
                }
            });
        }
    }

    public Produto prodEmCompra(Compra compra, String nomeProd){
        Produto prod = new Produto();
        List<Produto> prods = compra.produtos;
        for(Produto p:prods){
            if(p.nome.contentEquals(nomeProd)){
                if ("".contains(prod.nome)) {
                    prod = p;
                } else {
                    prod.quantidade += p.quantidade;
                }
            }
        }
        return prod;
    }
}
