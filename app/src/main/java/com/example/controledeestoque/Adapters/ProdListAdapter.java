package com.example.controledeestoque.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controledeestoque.DetalhesProduto;
import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.util.List;

public class ProdListAdapter extends RecyclerView.Adapter<ProdListAdapter.ViewHolderProduto> {

    private List<Produto> dados;
    private ComprasRepositorio compRep;
    private Context context;
    private boolean dataComHora;

    public static Produto prodContextMenu;
    public static final int ITEM_EDITAR = 1, ITEM_DELETAR = 2;

    public ProdListAdapter(List<Produto> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_produto, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        compRep = new ComprasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)) {
            Produto produto = dados.get(position);
            Log.d("TAG", "onBindViewHolder - " + produto.nome);
//            formatCateg(holder, produto);
            if (!"".contains(produto.nome)) {
                holder.txtNome.setText(produto.nome);
                if (produto.preço != 0.00f) {
                    holder.txtUltPreco.setText(String.format("R$%.2f", produto.preço));
                } else {
                    holder.txtUltPreco.setText("---");
                }
                if (compRep.proxCompraComProd(produto) != null) {
                    String dataProxComp = compRep.proxCompraComProd(produto).data;
                    holder.txtProxComp.setText(dataProxComp);
                } else {
                    holder.txtProxComp.setVisibility(View.GONE);
                    holder.lblProxComp.setVisibility(View.GONE);
                }
                try {
                    if (compRep.ultimaCompraComProd(produto, true) != null) {
                        holder.txtUltComp.setText(compRep.ultimaCompraComProd(produto, true).data.substring(0, 10));
                    } else {
                        holder.txtUltComp.setText("---");
                    }

                } catch (NullPointerException ex) {
                    holder.txtUltComp.setText("");
                    holder.txtHora.setText("");
                }
            }
        }
    }
    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNome,txtUltPreco,txtUltComp,txtHora,lblProxComp,txtProxComp,txtCateg,lblCateg,lblUltComp;
        public ConstraintLayout layout1,layout2;
        public LinearLayout prodLayout;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtLocalComp);
            txtUltPreco = itemView.findViewById(R.id.txtValTot);
            txtUltComp = itemView.findViewById(R.id.txtUltComp);
            txtProxComp = itemView.findViewById(R.id.txtProxComp);
            lblProxComp = itemView.findViewById(R.id.lblProxComp);
            lblCateg = itemView.findViewById(R.id.lblCateg);
            txtCateg = itemView.findViewById(R.id.txtCateg);
            layout2 = itemView.findViewById(R.id.layout2);
            layout1 = itemView.findViewById(R.id.layout1);
            prodLayout = itemView.findViewById(R.id.prodLayout);

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                prodContextMenu = dados.get(getLayoutPosition());
                menu.add(getLayoutPosition(),ITEM_EDITAR,0,"Editar");
                menu.add(getLayoutPosition(),ITEM_DELETAR,1,"Deletar");
                }
            });

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

    public void formatCateg(ProdListAdapter.ViewHolderProduto holder, Produto prod){
        if ("".contains(prod.nome)) {
            holder.layout2.setVisibility(View.GONE);
            holder.lblCateg.setVisibility(View.VISIBLE);
            holder.txtCateg.setVisibility(View.VISIBLE);
            holder.txtCateg.setText(prod.categoria);
            holder.layout1.setBackgroundColor(ContextCompat.getColor(context, R.color.corCategoriaCompra));
        } else {
            holder.layout2.setVisibility(View.VISIBLE);
            holder.lblCateg.setVisibility(View.GONE);
            holder.txtCateg.setVisibility(View.GONE);
            holder.layout1.setBackgroundColor(Color.WHITE);
        }
    }
    public Produto getContextMenuProduto(){
        return prodContextMenu;
    }
}
