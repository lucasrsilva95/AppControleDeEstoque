package com.example.controledeestoque.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;

import java.util.List;

public class CategProdCompraAdapter extends RecyclerView.Adapter<CategProdCompraAdapter.ViewHolderProduto> {

    private TextView txtTotComp;
    private List<String> categorias, categAbertas;
    public List<Produto> prodsCateg, prodsSelecionados;
    private ComprasRepositorio compRep;
    private ProdutosRepositorio prodRep;
    private Context context;
    private boolean dataComHora;

    public CategProdCompraAdapter(List<String> categAbertas, List<Produto> prodsSelecionados, TextView txtTotComp, Context context) {
        this.context = context;
        prodRep = new ProdutosRepositorio(context);
        this.categorias = prodRep.categoriasProds();
        this.categAbertas = categAbertas;
        this.prodsSelecionados = prodsSelecionados;
        this.txtTotComp = txtTotComp;
        txtTotComp.setText(String.format("R$%.2f", prodRep.somaTotalProdutos(prodsSelecionados)));
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG", "onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.linha_categ_prod_compra, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view, parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        prodRep = new ProdutosRepositorio(context);
        if ((categorias != null) && (categorias.size() > 0)){
            String categ = categorias.get(position);
            if (categ != null) {
                holder.txtCateg.setText(categ);
                if (categAbertas.contains(categ)) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    holder.lstProds.setLayoutManager(linearLayoutManager);
                    prodsCateg = prodRep.produtosDaCateg(categ);
                    CompraAdapter compraAdapter = new CompraAdapter(prodsCateg, prodsSelecionados,txtTotComp,context);
                    holder.lstProds.setAdapter(compraAdapter);
                    holder.lstProds.setVisibility(View.VISIBLE);
                } else {
                    holder.lstProds.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtCateg;
        public RecyclerView lstProds;
        public ConstraintLayout layoutCateg;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtCateg = itemView.findViewById(R.id.txtCateg2);
            layoutCateg = itemView.findViewById(R.id.layoutCateg);
            lstProds = itemView.findViewById(R.id.lstProdsCateg);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (categorias.size() > 0) {
                        String categ = categorias.get(getLayoutPosition());
                        if (!categAbertas.contains(categ)) {
                            categAbertas.add(categ);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            lstProds.setLayoutManager(linearLayoutManager);
                            prodsCateg = prodRep.produtosDaCateg(categ);
                            CompraAdapter compraAdapter = new CompraAdapter(prodsCateg, prodsSelecionados,txtTotComp,context);
                            lstProds.setAdapter(compraAdapter);
                            lstProds.setVisibility(View.VISIBLE);
                        } else {
                            categAbertas.remove(categ);
                            lstProds.setVisibility(View.GONE);
                        }
                    }
                }
            });


        }
    }
    public List<String> categoriasAbertas(){
        return categAbertas;
    }

//    public List<Produto> produtosSelecionados(){
//        return produtosSelecionados();
//    }
}