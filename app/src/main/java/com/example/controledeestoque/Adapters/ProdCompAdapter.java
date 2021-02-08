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

import com.example.controledeestoque.DetalhesProduto;
import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.util.List;

public class ProdCompAdapter extends RecyclerView.Adapter<ProdCompAdapter.ViewHolderProduto> {

    private List<Produto> dados;
    private ComprasRepositorio compRep;
    private Context context;

    public ProdCompAdapter(List<Produto> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public ProdCompAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_prodscomp, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(@NonNull ProdCompAdapter.ViewHolderProduto holder, int position) {
        compRep = new ComprasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            Produto produto = dados.get(position);
            holder.txtNome.setText(produto.nome);
            holder.txtMarca.setText(produto.marca);
            holder.txtPrecoUni.setText(String.format("R$%.2f",produto.preço));
            holder.lblUnidade.setText(produto.unidade);

            if ("un".contains(produto.unidade)) {
                holder.txtQuant.setText(String.format("%.0f",produto.quantidade));
                holder.lblValTotKg.setText("Preço/un:");
            } else {
                holder.txtQuant.setText(String.format("%.2f",produto.quantidade));
                holder.lblValTotKg.setText("Preço/Kg:");
            }
            holder.txtTot.setText(String.format("R$%.2f",(produto.quantidade * produto.preço)));
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNome,txtMarca,txtQuant,txtPrecoUni,txtTot, lblUnidade, lblValTotKg;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeProdComp);
            txtMarca = itemView.findViewById(R.id.txtMarcaProd);
            txtQuant = itemView.findViewById(R.id.txtQuantProdComp);
            txtPrecoUni = itemView.findViewById(R.id.txtPrecoUniComp);
            txtTot = itemView.findViewById(R.id.txtTotProdComp);
            lblUnidade = itemView.findViewById(R.id.lblUnidade);
            lblValTotKg = itemView.findViewById(R.id.lblValTotValKg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0){
                        Produto produto = dados.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesProduto.class);
                        it.putExtra("PRODUTO", produto);
                        ((AppCompatActivity)context).startActivityForResult(it,2);
                    }
                }
            });
        }
    }
}
