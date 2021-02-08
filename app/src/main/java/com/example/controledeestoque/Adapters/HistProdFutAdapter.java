package com.example.controledeestoque.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controledeestoque.DetalhesCompra;
import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.entidades.Produto;

import java.util.List;

public class HistProdFutAdapter extends RecyclerView.Adapter<HistProdFutAdapter.ViewHolderProduto> {

    private List<Compra> dados;
    private String produto;

    public HistProdFutAdapter(List<Compra> dados, String produto) {
        this.dados = dados;
        this.produto = produto;
    }

    @NonNull
    @Override
    public HistProdFutAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view =layoutInflater.inflate(R.layout.linha_compfutprod, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(@NonNull HistProdFutAdapter.ViewHolderProduto holder, int position) {

        if ((dados != null) && (dados.size() > 0)){
            Compra compra = dados.get(position);
            holder.txtData.setText(compra.data);
            Produto prod = prodEmCompra(compra, produto);
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

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtData,txtPrecoUni,txtQuant, txtValTotProd, lblUnidade, lblValUniKg;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtData = itemView.findViewById(R.id.txtDataCompFut);
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
                        it.putExtra("COMPRAFUTURA", compra);
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
            if(p.nome.contains(nomeProd)){
                prod = p;
            }
        }
        return prod;
    }
}
