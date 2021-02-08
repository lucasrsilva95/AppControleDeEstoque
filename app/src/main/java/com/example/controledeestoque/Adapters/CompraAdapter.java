package com.example.controledeestoque.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controledeestoque.R;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.entidades.Produto;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.util.ArrayList;
import java.util.List;

public class CompraAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private TextView txtTotComp;

    private List<Produto> dados, selecionados;
    private Compra compra;
    private ComprasRepositorio compRep;
    private ViewHolderCompra holderComp;

    private int codigo;

    private Context context;

    public CompraAdapter(List<Produto> dados, List<Produto> selecionados, TextView txtTotComp, Context context) {
        this.dados = dados;
        this.selecionados = selecionados;
        this.txtTotComp = txtTotComp;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if("".contains(dados.get(position).nome)){
            return 1;
        }else{
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            View view = layoutInflater.inflate(R.layout.linha_compra, parent, false);
            ViewHolderCompra holderCompra = new ViewHolderCompra(view, parent.getContext());
            return holderCompra;
        } else {
            View view = layoutInflater.inflate(R.layout.linha_categ, parent, false);
            ViewHolderCateg holderCateg = new ViewHolderCateg(view);
            return holderCateg;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if ((dados != null) && (dados.size() > 0)) {
            Produto produto = dados.get(position);
//            formatCateg(holder, produto);
            Log.d("COMPRA", "onCreateViewHolder - " + produto.nome);
            switch(holder.getItemViewType()) {
                case 1:
                    ViewHolderCateg holderCateg = (ViewHolderCateg) holder;
                    holderCateg.txtCateg.setText(produto.categoria);
                    break;
                case 0:
                    ViewHolderCompra holderCompra = (ViewHolderCompra) holder;
                    holderCompra.txtNome.setText(produto.nome);
                    holderCompra.txtMarca.setText(produto.marca);
                    if (produto.preço != 0.00f) {
                        holderCompra.txtUltPreco.setText(String.format("R$%.2f",produto.preço));
                    } else {
                        holderCompra.txtUltPreco.setText("-");
                    }
                    holderCompra.lblQuant.setText(produto.unidade);

                    ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, quants());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holderCompra.spinnerQuant.setAdapter(adapter);
                    for (Produto prod : selecionados) {
                        if (prod.nome.contentEquals(produto.nome) && prod.marca.contentEquals(produto.marca)) {
                            holderCompra.select.setChecked(true);
                            holderCompra.lblUltpreco.setVisibility(View.VISIBLE);
                            holderCompra.lblValTotProd.setVisibility(View.VISIBLE);
                            holderCompra.edtPreco.setVisibility(View.VISIBLE);
                            holderCompra.lblUltpreco.setVisibility(View.VISIBLE);
                            holderCompra.txtSifrao.setVisibility(View.VISIBLE);
                            holderCompra.txtUltPreco.setVisibility(View.VISIBLE);
                            if ("un".contains(prod.unidade)) {
                                holderCompra.spinnerQuant.setVisibility(View.VISIBLE);
                                holderCompra.edtQuant.setVisibility(View.INVISIBLE);
                                holderCompra.spinnerQuant.setSelection((int) prod.quantidade - 1);
                            } else {
                                holderCompra.spinnerQuant.setVisibility(View.INVISIBLE);
                                holderCompra.edtQuant.setVisibility(View.VISIBLE);
                                if (prod.quantidade != 0.0f) {
                                    holderCompra.edtQuant.setText(Float.toString(prod.quantidade));
                                }
                            }
                            if (prod.preço != 0.0f) {
                                holderCompra.edtPreco.setText(Float.toString(prod.preço));
                            }
                            holderCompra.txtValTotProd.setVisibility(View.VISIBLE);
                            holderCompra.lblValTotProd.setVisibility(View.VISIBLE);
                            float tot = prod.quantidade * prod.preço;
                            holderCompra.txtValTotProd.setText(String.format("R$%.2f", tot));
                            break;
                        } else {
                            holderCompra.select.setChecked(false);
                            holderCompra.lblUltpreco.setVisibility(View.INVISIBLE);
                            holderCompra.lblValTotProd.setVisibility(View.INVISIBLE);
                            holderCompra.edtPreco.setVisibility(View.INVISIBLE);
                            holderCompra.edtQuant.setVisibility(View.INVISIBLE);
                            holderCompra.spinnerQuant.setVisibility(View.INVISIBLE);
                            holderCompra.txtSifrao.setVisibility(View.INVISIBLE);
                            holderCompra.txtUltPreco.setVisibility(View.INVISIBLE);
                            holderCompra.txtValTotProd.setVisibility(View.INVISIBLE);
                            holderCompra.lblQuant.setVisibility(View.INVISIBLE);
                        }
                    }
                    atualizarTotComp();
                    break;
                }
            }
        }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderCompra extends RecyclerView.ViewHolder {

        public TextView txtNome, txtMarca, txtSifrao, txtUltPreco, lblUltpreco, lblQuant, txtValTotProd, lblValTotProd, lblMarca, txtCateg, lblCateg;
        public EditText edtPreco, edtQuant;
        public CheckBox select;
        public Spinner spinnerQuant;
        public ConstraintLayout layoutGeralComp;
        public LinearLayout linLayout;

        public ViewHolderCompra(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNome);
            txtMarca = itemView.findViewById(R.id.txtMarca);
            lblMarca = itemView.findViewById(R.id.lblMarca);
            txtSifrao = itemView.findViewById(R.id.txtSifrao);
            edtPreco = itemView.findViewById(R.id.edtPreco);
            edtQuant = itemView.findViewById(R.id.edtQuant);
            txtUltPreco = itemView.findViewById(R.id.txtNumItens);
            lblUltpreco = itemView.findViewById(R.id.lblUltPreco);
            lblQuant = itemView.findViewById(R.id.lblQuant);
            txtValTotProd = itemView.findViewById(R.id.txtValTotProd);
            lblValTotProd = itemView.findViewById(R.id.lblValTotProd);
            select = (CheckBox) itemView.findViewById(R.id.select);
            spinnerQuant = (Spinner) itemView.findViewById(R.id.spinnerQuant);
            layoutGeralComp = (ConstraintLayout) itemView.findViewById(R.id.layoutGeralComp);
            linLayout = (LinearLayout) itemView.findViewById(R.id.linLayout);
            preçoInvisivel();
            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dados.size() > 0) {
                        Produto produto = dados.get(getLayoutPosition());
                        if (prodIndexNaLista(selecionados, produto) == -1) {
                            selecionados.add(produto);
                            if ("".contains(edtQuant.getText())) {
                                produto.quantidade = 1.0f;
                            }
                            if ("".contains(edtPreco.getText())) {
                                produto.preço = 0.0f;
                            }
                            select.setChecked(true);
                            preçoVisivel();
                            abrirTeclado(edtPreco);
                            if ("un".contains(produto.unidade)) {
                                edtQuant.setVisibility(View.INVISIBLE);
                                spinnerQuant.setVisibility(View.VISIBLE);
                            } else {
                                edtQuant.setVisibility(View.VISIBLE);
                                spinnerQuant.setVisibility(View.INVISIBLE);
                            }

                        } else {
                            selecionados.remove(produto);
                            select.setChecked(false);
                            preçoInvisivel();
                            fecharTeclado(edtPreco);
                        }
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dados.size() > 0) {
                        Produto produto = dados.get(getLayoutPosition());
                        if (!"".contains(produto.nome)) {
                            if (prodIndexNaLista(selecionados, produto) == -1) {
                                selecionados.add(produto);
                                if ("".contains(edtQuant.getText())) {
                                    produto.quantidade = 1.0f;
                                }
                                if ("".contains(edtPreco.getText())) {
                                    produto.preço = 0.0f;
                                }
                                edtPreco.setText("");
                                select.setChecked(true);
                                preçoVisivel();
                                abrirTeclado(edtPreco);
                                if ("un".contains(produto.unidade)) {
                                    edtQuant.setVisibility(View.INVISIBLE);
                                    spinnerQuant.setVisibility(View.VISIBLE);
                                    spinnerQuant.setSelection(0);
                                } else {
                                    edtQuant.setVisibility(View.VISIBLE);
                                    spinnerQuant.setVisibility(View.INVISIBLE);
                                    edtQuant.setText("");
                                }

                            } else {
                                selecionados.remove(produto);
                                preçoInvisivel();
                                fecharTeclado(edtPreco);
                            }
                            atualizarTotComp();
                        }
                    }
                }
            });
            edtPreco.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Produto produto = dados.get(getLayoutPosition());
                    while (prodIndexNaLista(selecionados, produto) != -1) {
                        selecionados.remove(prodIndexNaLista(selecionados, produto));
                    }
                    if (!" 0.".contains(s.toString())) {
                        produto.preço = Float.parseFloat(s.toString().replace(",", "."));
                    } else {
                        produto.preço = 0.0f;
                        if (".".contains(s.toString()) && !"".contains(s.toString())) {
                            edtPreco.setText("0.");
                        } else {
                            edtPreco.setSelection(s.length());
                        }
                    }
                    selecionados.add(produto);
                    atualizarTotComp();
                    if (produto.quantidade == 0.0f) {
                        produto.quantidade = 1.0f;
                    }
                    float tot = (produto.quantidade * produto.preço);
                    txtValTotProd.setText(String.format("R$%.2f", tot));
                }
            });

            edtQuant.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Produto produto = dados.get(getLayoutPosition());
                    while (prodIndexNaLista(selecionados, produto) != -1) {
                        selecionados.remove(prodIndexNaLista(selecionados, produto));
                    }
                    if (!" 0.".contains(s.toString())) {
                        produto.quantidade = Float.parseFloat(s.toString().replace(",", "."));
                    } else {
                        produto.quantidade = 1.0f;
                        if (".".contains(s.toString()) && !"".contains(s.toString())) {
                            edtQuant.setText("0.");
                        } else {
                            edtQuant.setSelection(s.length());
                        }
                    }
                    selecionados.add(produto);
                    atualizarTotComp();
                    if (produto.quantidade == 0.0f) {
                        produto.quantidade = 1.0f;
                    }
                    float tot = (produto.quantidade * produto.preço);
                    txtValTotProd.setText(String.format("R$%.2f", tot));
                }
            });

            spinnerQuant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (select.isChecked() && spinnerQuant.getVisibility() == View.VISIBLE) {
                        Produto produto = dados.get(getLayoutPosition());
                        while (prodIndexNaLista(selecionados, produto) != -1) {
                            selecionados.remove(prodIndexNaLista(selecionados, produto));
                        }
                        produto.quantidade = Float.parseFloat(spinnerQuant.getSelectedItem().toString());
                        selecionados.add(produto);
                        atualizarTotComp();
                        float tot = (produto.quantidade * produto.preço);
                        txtValTotProd.setText(String.format("R$%.2f", tot));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        public void preçoVisivel() {
            select.setChecked(true);
            lblUltpreco.setVisibility(View.VISIBLE);
            lblValTotProd.setVisibility(View.VISIBLE);
            edtPreco.setVisibility(View.VISIBLE);
            edtQuant.setVisibility(View.VISIBLE);
            lblUltpreco.setVisibility(View.VISIBLE);
            spinnerQuant.setVisibility(View.VISIBLE);
            txtSifrao.setVisibility(View.VISIBLE);
            txtUltPreco.setVisibility(View.VISIBLE);
            txtValTotProd.setVisibility(View.VISIBLE);
            lblQuant.setVisibility(View.VISIBLE);
            edtPreco.requestFocus();
        }

        public void preçoInvisivel() {
            select.setChecked(false);
            lblUltpreco.setVisibility(View.INVISIBLE);
            lblValTotProd.setVisibility(View.INVISIBLE);
            edtPreco.setVisibility(View.INVISIBLE);
            edtQuant.setVisibility(View.INVISIBLE);
            spinnerQuant.setVisibility(View.INVISIBLE);
            txtSifrao.setVisibility(View.INVISIBLE);
            txtUltPreco.setVisibility(View.INVISIBLE);
            txtValTotProd.setVisibility(View.INVISIBLE);
            lblValTotProd.setVisibility(View.INVISIBLE);
            lblQuant.setVisibility(View.INVISIBLE);

        }
    }

    public class ViewHolderCateg extends RecyclerView.ViewHolder{

        public TextView lblCateg, txtCateg;

        public ViewHolderCateg(@NonNull View itemView) {
            super(itemView);

            lblCateg = (TextView) itemView.findViewById(R.id.lblCateg2);
            txtCateg = (TextView) itemView.findViewById(R.id.txtCateg2);
        }
    }

    public List<Produto> produtosSelecionados() {
        return selecionados;
    }

    public Produto acharProdPeloCodigo(List<Produto> prods, int codigo) {
        Produto result = new Produto();
        for (Produto p : prods) {
            if (p.codigo == codigo) {
                result = p;
            }
        }
        return result;
    }

    public int prodIndexNaLista(List<Produto> prods, Produto prod){
        for (Produto p : prods) {
            if (p.nome.contentEquals(prod.nome) && p.marca.contentEquals(prod.marca)) {
                return prods.indexOf(p);
            }
        }
        return -1;
    }

    public void atualizarTotComp() {
        double tot = 0.0;
        for (Produto prod : selecionados) {
            if (prod.quantidade == 0.0) {
                prod.quantidade = 1.0f;
            }
            tot += (prod.quantidade * prod.preço);
        }
        txtTotComp.setText(String.format("R$%.2f", tot));
    }

    public List<Integer> quants() {
        List<Integer> numeros = new ArrayList<>();
        int numMax = 50;
        for (int i = 1; i <= numMax; i++) {
            numeros.add(i);
        }
        return numeros;
    }

    public void abrirTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void fecharTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


//    public void formatCateg(CompraAdapter.ViewHolderCompra holder, Produto prod) {
//            if ("".contains(prod.nome)) {
//                holder.select.setVisibility(View.GONE);
//                holder.txtNome.setVisibility(View.GONE);
//                holder.txtMarca.setVisibility(View.GONE);
//                holder.lblMarca.setVisibility(View.GONE);
//                holder.lblUltpreco.setVisibility(View.GONE);
//                holder.spinnerQuant.setVisibility(View.GONE);
//                holder.txtUltPreco.setVisibility(View.GONE);
//                holder.txtSifrao.setVisibility(View.GONE);
//                holder.lblQuant.setVisibility(View.GONE);
//                holder.txtValTotProd.setVisibility(View.GONE);
//                holder.lblValTotProd.setVisibility(View.GONE);
//                holder.edtQuant.setVisibility(View.GONE);
//                holder.edtPreco.setVisibility(View.GONE);
//                holder.lblCateg.setVisibility(View.VISIBLE);
//                holder.txtCateg.setVisibility(View.VISIBLE);
//                holder.txtCateg.setText(prod.categoria);
//                holder.layoutGeralComp.setBackgroundColor(ContextCompat.getColor(context, R.color.corCategoriaCompra));
//            } else {
//                holder.select.setVisibility(View.VISIBLE);
//                holder.txtNome.setVisibility(View.VISIBLE);
//                holder.txtMarca.setVisibility(View.VISIBLE);
//                holder.lblMarca.setVisibility(View.VISIBLE);
//                holder.lblUltpreco.setVisibility(View.VISIBLE);
//                holder.spinnerQuant.setVisibility(View.VISIBLE);
//                holder.txtUltPreco.setVisibility(View.VISIBLE);
//                holder.txtSifrao.setVisibility(View.VISIBLE);
//                holder.lblQuant.setVisibility(View.VISIBLE);
//                holder.txtValTotProd.setVisibility(View.VISIBLE);
//                holder.lblValTotProd.setVisibility(View.VISIBLE);
//                holder.edtQuant.setVisibility(View.VISIBLE);
//                holder.edtPreco.setVisibility(View.VISIBLE);
//                holder.lblCateg.setVisibility(View.GONE);
//                holder.txtCateg.setVisibility(View.GONE);
//                holder.layoutGeralComp.setBackgroundColor(Color.WHITE);
//                holder.preçoInvisivel();
//            }
//    }
}
