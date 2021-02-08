package com.example.controledeestoque.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controledeestoque.DetalhesProduto;
import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.util.List;

public class ProdEstoqueAdapter extends RecyclerView.Adapter<ProdEstoqueAdapter.ViewHolderProduto> {

    private List<Produto> dados;
    private ComprasRepositorio compRep;
    private Context context;
    private boolean dataComHora;

    public ProdEstoqueAdapter(List<Produto> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG", "onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.linha_prod_estoque, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view, parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        compRep = new ComprasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)) {
            Produto produto = dados.get(position);
            Log.d("TAG", "onBindViewHolder - " + produto.nome);
//            formatCateg(holder, produto);
            if (!"".contains(produto.nome)){
                holder.txtNome.setText(produto.nome);
                holder.txtMarca.setText(produto.marca);

                if (produto.duracao == 0) {
                    holder.txtEstoque.setTextColor(Color.DKGRAY);
                    holder.txtEstoque.setText("INDEFINIDO");
                } else if (produto.quantidade == 0) {
                    holder.txtEstoque.setTextColor(Color.RED);
                    holder.txtEstoque.setText("VENCIDO");
                } else {
                    holder.txtEstoque.setTextColor(Color.GREEN);
                    holder.txtEstoque.setText("EM ESTOQUE");
                }
                holder.txtQuant.setText(String.format("%.1f", produto.quantidade));
                if (compRep.proxCompraComProd(produto) != null) {
                    holder.txtProxComp.setText(compRep.proxCompraComProd(produto).data);
                } else {
                    holder.txtProxComp.setText("------");
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder {

        public TextView txtNome, txtMarca, lblProxComp, txtProxComp, txtEstoque, lblCateg, txtCateg, txtQuant;
        public ConstraintLayout layout1, layout2;
        public LinearLayout prodLayout;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNome);
            txtMarca = itemView.findViewById(R.id.txtMarca);
            txtEstoque = itemView.findViewById(R.id.txtEstoque);
            txtProxComp = itemView.findViewById(R.id.txtProxComp);
            lblProxComp = itemView.findViewById(R.id.lblProxComp);
            lblCateg = itemView.findViewById(R.id.lblCateg);
            txtCateg = itemView.findViewById(R.id.txtCateg);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            layout2 = itemView.findViewById(R.id.layout2);
            layout1 = itemView.findViewById(R.id.layout1);
            prodLayout = itemView.findViewById(R.id.prodLayout);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        Produto produto = dados.get(getLayoutPosition());
                        if (!"".contains(produto.nome)) {
                            Intent it = new Intent(context, DetalhesProduto.class);
                            it.putExtra("PRODUTO", produto);
                            ((AppCompatActivity) context).startActivityForResult(it, 2);
                        }
                    }
                }
            });
        }
    }

}
