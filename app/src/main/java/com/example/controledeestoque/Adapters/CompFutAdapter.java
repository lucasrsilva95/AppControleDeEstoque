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
import com.example.controledeestoque.OperacoesDatas;
import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.entidades.Produto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CompFutAdapter extends RecyclerView.Adapter<CompFutAdapter.ViewHolderProduto> {

    private List<Compra> compras;
    private Context context;

    public CompFutAdapter(List<Compra> dados, Context context) {
        this.compras = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public CompFutAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view =layoutInflater.inflate(R.layout.linha_compfut, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(@NonNull CompFutAdapter.ViewHolderProduto holder, int position) {

        OperacoesDatas opDatas = new OperacoesDatas(context);

        if ((compras != null) && (compras.size() > 0)){
            Compra comp = compras.get(position);
            holder.txtDataCompFut.setText(comp.data);
            holder.txtValTot.setText(String.format("R$%.2f",comp.total));
            holder.txtNumItens.setText(Integer.toString(numItens(comp.produtos)));

            if(comp.data.contentEquals(opDatas.dataAtual())){
                holder.txtHoje.setVisibility(View.VISIBLE);
            }
        }
    }

    public int numItens(List<Produto> prods){
        int res = 0;
        for(Produto prod:prods){
            if (prod.unidade.contains("un")) {
                res += prod.quantidade;
            } else if(prod.unidade.contains("Kg")) {
                res += 1;
            }
        }
        return res;
    }

    @Override
    public int getItemCount() {
        return compras.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtDataCompFut,txtValTot,txtNumItens, txtHoje;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtDataCompFut = itemView.findViewById(R.id.txtDataCompFut);
            txtValTot = itemView.findViewById(R.id.txtValTotCompFut);
            txtNumItens = itemView.findViewById(R.id.txtNumItensCompFut);
            txtHoje = itemView.findViewById(R.id.txtHoje);
            txtHoje.setVisibility(View.INVISIBLE);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (compras.size() > 0){
                        Compra compra = compras.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesCompra.class);
                        it.putExtra("COMPRAFUTURA", compra);
                        ((AppCompatActivity)context).startActivityForResult(it,2);
                    }
                }
            });
        }
    }


}
