package com.example.controledeestoque.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ContextMenu;
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
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.util.List;

public class CompListAdapter extends RecyclerView.Adapter<CompListAdapter.ViewHolderNovaComp> {

    private List<Compra> dados;
    private Compra compra;
    public static Compra compContextMenu;
    private ComprasRepositorio compRep;
    private boolean dataComHora;

    private int codigo;

    public static final int ITEM_EDITAR = 1, ITEM_DELETAR = 2;

    private Context context;

    public CompListAdapter(List<Compra> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public CompListAdapter.ViewHolderNovaComp onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view =layoutInflater.inflate(R.layout.linha_complist, parent, false);

        ViewHolderNovaComp holderCompra = new ViewHolderNovaComp(view,parent.getContext());

        definirConfigs();

        return holderCompra;
    }

    @Override
    public void onBindViewHolder(@NonNull CompListAdapter.ViewHolderNovaComp holder, int position) {

        if ((dados != null) && (dados.size() > 0)){
            Compra compra = dados.get(position);
            holder.txtLocal.setText(compra.local);

            if (dataComHora) {
                holder.txtDataHora.setText(compra.data);
            } else {
                holder.txtDataHora.setText(compra.data.substring(0,10));
            }
            holder.txtValTotal.setText(String.format("R$%.2f",compra.total));
            holder.txtNumItens.setText(Integer.toString(numItens(compra.produtos)));
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

    public void definirConfigs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        dataComHora = prefs.getBoolean("horaMin_switch",true);
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderNovaComp extends RecyclerView.ViewHolder{

        public TextView txtLocal,txtDataHora,txtValTotal,txtNumItens;

        public Compra compraSel;

        public ViewHolderNovaComp(@NonNull View itemView, final Context context) {
            super(itemView);

            txtLocal = itemView.findViewById(R.id.txtLocalComp);
            txtDataHora = itemView.findViewById(R.id.txtDataHora);
            txtValTotal = itemView.findViewById(R.id.txtValTot);
            txtNumItens = itemView.findViewById(R.id.txtNumItens);

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    compContextMenu = dados.get(getLayoutPosition());
                    menu.add(getLayoutPosition(),ITEM_EDITAR,0,"Editar");
                    menu.add(getLayoutPosition(),ITEM_DELETAR,1,"Deletar");
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dados.size() > 0){
                        Compra compra = dados.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesCompra.class);
                        it.putExtra("COMPRA",compra);
                        ((AppCompatActivity)context).startActivityForResult(it,110);
                    }
                }
            });


        }
    }

    public Compra getContextMenuCompra(){
        return compContextMenu;
    }


}
