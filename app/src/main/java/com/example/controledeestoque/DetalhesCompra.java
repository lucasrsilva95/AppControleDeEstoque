package com.example.controledeestoque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.controledeestoque.Adapters.ProdCompAdapter;
import com.example.controledeestoque.database.BancoOpenHelper;
import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetalhesCompra extends AppCompatActivity implements OnMapReadyCallback {

    private String loc;
    private int codigo, horaNotif, minutoNotif;
    private boolean dataComHora, salvarLoc, notif;

    private Bundle bundle;

    private TextView txtLocal,txtNumProd,txtData,txtTotal,txtHora, lblLocal, lblHora;
    private RecyclerView lstProdComp,lstProdCompFut;

    private Button botConfirm;
    private ImageButton botMap, botCamera, botImagem, botPasta;
    private Switch switchSalvLoc;

    private Compra compra;

    private ProdutosRepositorio prodRep;
    private ComprasRepositorio compRep;
    private BancoOpenHelper bancoOpenHelper;
    private SQLiteDatabase conexao;

    private AlertDialog.Builder dlgErro,dlgDel;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int VOLTAR_INICIO = 1, REQUEST_CODE_MAPA = 101, PERMISSAO_REQUEST = 102, CAMERA = 103, REMOVER_LOC_MAPA = 104,
            REQUEST_EXTERNAL_STORAGE = 105, REQUEST_CODE_CAMERA = 106, FOTO_ALTERADA = 107, NOVA_FOTO = 108, ESCOLHER_ARQUIVO = 109
            , FOTO_NAO_ENCONTRADA = 110, LOC_MAPA_ALTERADO = 111;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public LatLng latLng;

    public File imagem;
    private File arquivoFoto = null;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_compra);
        getSupportActionBar().setTitle("Detalhes da Compra");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        {
            txtLocal = (TextView) findViewById(R.id.txtLocalComp);
            txtNumProd = (TextView) findViewById(R.id.txtNumProd);
            txtData = (TextView) findViewById(R.id.txtDataComp);
            txtHora = (TextView) findViewById(R.id.txtHoraComp);
            txtTotal = (TextView) findViewById(R.id.txtValTot);
            lblLocal = (TextView) findViewById(R.id.lblLocal);
            lblHora = (TextView) findViewById(R.id.lblHora);
            botConfirm = (Button) findViewById(R.id.botConfirm);
            lstProdComp = (RecyclerView) findViewById(R.id.lstProdComp);
            lstProdCompFut = (RecyclerView) findViewById(R.id.lstProdCompFut);
            botMap = (ImageButton) findViewById(R.id.botMap);
            botCamera = (ImageButton) findViewById(R.id.botCamera);
            botImagem = (ImageButton) findViewById(R.id.botImagem);
            botPasta = (ImageButton) findViewById(R.id.botPasta);
            switchSalvLoc = (Switch) findViewById(R.id.switchSalvLoc);
        } // Declaração das Variaveis

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        definirConfigs();
        criarConexaoCompra();
        verificaParametro();
        criarListas();

        switchSalvLoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    fetchLastLocation();
                }
            }
        });

        verificarPermissoes();

    }

    private void criarConexaoCompra(){
        // Criando conexão com o banco de dados
        try{
            bancoOpenHelper = new BancoOpenHelper(this);
            conexao = bancoOpenHelper.getWritableDatabase();
            compRep = new ComprasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);

        }catch (SQLiteException ex){ // No caso de não ser possivel criar a conexão
            dlgErro = new AlertDialog.Builder(this);
            dlgErro.setTitle("Erro");
            dlgErro.setMessage(ex.getMessage());
            dlgErro.setNeutralButton("OK",null);
            dlgErro.show();
        }
    }

    public void criarListas(){
        lstProdComp.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lstProdComp.setLayoutManager(linearLayoutManager);
        ProdCompAdapter adapter = new ProdCompAdapter(compra.produtos,this);
        lstProdComp.setAdapter(adapter);
    }

    public void definirConfigs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        salvarLoc = prefs.getBoolean("switch_SalvLoc",true);
        dataComHora = prefs.getBoolean("horaMin_switch",true);
        notif = prefs.getBoolean("switch_notific", true);
        String horaConfig = prefs.getString("horaNotif", "00:00");
        horaNotif = Integer.parseInt(horaConfig.substring(0,2));
        minutoNotif = Integer.parseInt(horaConfig.substring(3,5));

        if(!salvarLoc){
            switchSalvLoc.setVisibility(View.INVISIBLE);
        }
    }

    private void verificaParametro() {

        compra = new Compra();
        bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("COMPRA"))){
            compra = (Compra)bundle.getSerializable("COMPRA");
            codigo = compra.codigo;
            txtLocal.setText(compra.local);
            txtData.setText(compra.data.substring(0,10));
            if (bundle.containsKey("EDIT_COMPRA") || codigo == 0) {
                botConfirm.setVisibility(View.VISIBLE);
            } else {
                botConfirm.setVisibility(View.INVISIBLE);
            }
            if (dataComHora && compra.data.length() > 13) {
                lblHora.setVisibility(View.VISIBLE);
                txtHora.setVisibility(View.VISIBLE);
                txtHora.setText(compra.data.substring(13));
            } else {
                lblHora.setVisibility(View.INVISIBLE);
                txtHora.setVisibility(View.INVISIBLE);
            }
            if ("".contains(compra.locMap) && compRep.obterCoordLocal(compra.local).contentEquals("")){
                botMap.setVisibility(View.INVISIBLE);
                if (botConfirm.getVisibility() == View.VISIBLE && salvarLoc) {
                    switchSalvLoc.setVisibility(View.VISIBLE);
                }
            }else{
                if("".contains(compra.locMap)){
                    compra.locMap = compRep.obterCoordLocal(compra.local);
                }
                botMap.setVisibility(View.VISIBLE);
                switchSalvLoc.setVisibility(View.INVISIBLE);
            }
            txtNumProd.setText(Integer.toString(compra.numItens()));
            txtTotal.setText(String.format("R$%.2f",compra.total));

        }else if((bundle != null && (bundle.containsKey("COMPRAFUTURA"))) || (bundle == null && compRep.compraDeHoje() != null)){

            if (bundle == null && compRep.compraDeHoje() != null) {
                compra = compRep.compraDeHoje();
            } else {
                compra = (Compra)bundle.getSerializable("COMPRAFUTURA");
            }
            codigo = compra.codigo;
            lblLocal.setVisibility(View.INVISIBLE);
            txtLocal.setVisibility(View.INVISIBLE);
            lblHora.setVisibility(View.INVISIBLE);
            txtHora.setVisibility(View.INVISIBLE);
            txtData.setText(compra.data.substring(0,10));
            txtNumProd.setText(Integer.toString(compra.numItens()));
            txtTotal.setText(String.format("R$%.2f",compra.total));
            if (dataAtual().contains(compra.data)) {
                botConfirm.setVisibility(View.VISIBLE);
                botConfirm.setText("Realizar Compra");
            } else {
                botConfirm.setVisibility(View.INVISIBLE);
            }
            if ("".contains(compra.locMap)){
                botMap.setVisibility(View.INVISIBLE);
            }
            getSupportActionBar().setTitle("Detalhes da Compra Futura");

        }

        if(compra.foto == null){
            if (bundle != null && bundle.containsKey("COMPRAFUTURA")) {
                botCamera.setVisibility(View.GONE);
                botPasta.setVisibility(View.GONE);
            } else {
                botCamera.setVisibility(View.VISIBLE);
                botPasta.setVisibility(View.VISIBLE);
            }
            botImagem.setVisibility(View.GONE);
        }else{
            if (compra.foto.exists()) {
                botCamera.setVisibility(View.GONE);
                botPasta.setVisibility(View.GONE);
                botImagem.setVisibility(View.VISIBLE);
            }else{
                botCamera.setVisibility(View.VISIBLE);
                botPasta.setVisibility(View.VISIBLE);
                botImagem.setVisibility(View.GONE);
                compra.foto = null;
            }
        }
    }

    private void verificarPermissoes(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void botSalvarCompra(View view) {
        if (bundle != null ) {
            compra.efetivada = true;
            if(getSupportActionBar().getTitle().toString().contentEquals("Detalhes da Compra Futura")){
                Intent it2 = new Intent(DetalhesCompra.this, NovaCompra.class);
                it2.putExtra("EDIT_COMPRA",compra);
                startActivity(it2);
                finish();
                return;
            }
            if (salvarLoc && "".contains(compra.locMap) && switchSalvLoc.isChecked()) {
                compra.locMap = loc;
            }
//            Bundle bundle = getIntent().getExtras();
            if((bundle.containsKey("EDIT_COMPRA")) && !bundle.containsKey("EDITCOMPFUT")){
                compRep.alterar(compra);
                setResult(VOLTAR_INICIO);
                Toast.makeText(this, "Compra Editada com Sucesso", Toast.LENGTH_LONG).show();
            }else {
                compRep.inserir(compra);
                setResult(VOLTAR_INICIO);
                Toast.makeText(this, "Compra Salva com Sucesso", Toast.LENGTH_LONG).show();
            }
            prodRep.atualizarProds(compra.produtos);
            compRep.atualizarComprasFuturas();
            compRep.definirNotificacoes();
//            Intent it = new Intent(DetalhesCompra.this, CompList.class);
//            startActivity(it);
            finish();
        }else {
            Intent it = new Intent(DetalhesCompra.this, NovaCompra.class);
            it.putExtra("EDIT_COMPRA", compra);
            startActivityForResult(it, 10);
        }
    }

    public void botMap(View view){
        Intent it3 = new Intent(DetalhesCompra.this, Mapa.class);
//        compra.locMap = "-22.31197674;-49.05736148";
        it3.putExtra("COMPRA",compra);
        startActivityForResult(it3, 105);
    }

    public String dataAtual(){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        return (formatarData.format(data));
    }

    private File criarArquivo(){

//        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());

        String nomeArquivo;
        if (!"".contains(compra.local)) {
            nomeArquivo = compra.local + "_" + compra.data.replaceAll("/","_");
        } else {
            nomeArquivo = "JPG_" + compra.data;
        }

        File pasta = new File("/sdcard/Pictures/Notas");
        if(!pasta.exists()){
            pasta.mkdir();
        }
        imagem = new File(pasta.getAbsolutePath() + File.separator + nomeArquivo + ".jpg");

        return imagem;
    }

    public void captureImage(View view){
        tirarFoto();
    }
    public void tirarFoto(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA
            );
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(cameraIntent.resolveActivity(getPackageManager()) != null){

            arquivoFoto = criarArquivo();

            if(arquivoFoto != null){
                photoURI = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".fileprovider", arquivoFoto);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA);
            }

        }
    }

    public void escolherFoto(){
        escolherFoto(null);
    }
    public void escolherFoto(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, ESCOLHER_ARQUIVO);
    }

    public void displayImage(View view){

        Intent it = new Intent(this, DisplayImage.class);
        it.putExtra("COMPRA", compra);
        startActivityForResult(it, 111);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if ((botConfirm.getVisibility() == View.INVISIBLE) && ("Detalhes da Compra".contains(getSupportActionBar().getTitle()))) {
            inflater.inflate(R.menu.menu_detalhe, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                if (bundle == null) {
                    Intent it = new Intent(DetalhesCompra.this, ComprasFuturas.class);
                    it.putExtra("NOTIFIC", true);
                    startActivity(it);
                }
                finish();
                break;

            case R.id.menuItem_edit:
                Intent it2 = new Intent(DetalhesCompra.this, NovaCompra.class);
                it2.putExtra("EDIT_COMPRA",compra);
                startActivityForResult(it2,2);
                break;
            case R.id.menuItem_del:
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setTitle("Deletar Compra?");
                dlgDel.setMessage("Tem certeza que deseja deletar a compra?");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        compRep.excluir(compra.codigo);
                        prodRep.atualizarProds(compra.produtos);
                        Toast.makeText(getApplicationContext(),"Compra Excluida com sucesso",Toast.LENGTH_LONG).show();
//                        setResult(3);
                        compRep.atualizarComprasFuturas();
                        compRep.definirNotificacoes();
                        finish();
                    }
                });
                dlgDel.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_MAPA);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    loc = String.format("%f;%f",currentLocation.getLatitude(),currentLocation.getLongitude());
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_MAPA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                }
                break;
            case REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permissão concedida
                    tirarFoto();
                }
                break;
            case PERMISSAO_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permissão concedida
                }else{
                    //Permissao negada
                }
                return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case VOLTAR_INICIO:
                setResult(VOLTAR_INICIO);
                finish();
                break;
            case REMOVER_LOC_MAPA:
                compra.locMap = "";
                botMap.setVisibility(View.INVISIBLE);
                if (botConfirm.getVisibility() == View.VISIBLE && salvarLoc) {
                    switchSalvLoc.setVisibility(View.VISIBLE);
                }
                break;
            case LOC_MAPA_ALTERADO:
                compra.locMap = data.getStringExtra("Coordenadas");
                if(compra.codigo != 0){
                    compRep.alterar(compra);
                }
                break;
            case FOTO_ALTERADA:
                compra = compRep.buscarCompra(compra.codigo);
                if(compra.foto == null){
                    botCamera.setVisibility(View.VISIBLE);
                    botPasta.setVisibility(View.VISIBLE);
                    botImagem.setVisibility(View.GONE);
                }else{
                    if (compra.foto.exists()) {
                        botCamera.setVisibility(View.GONE);
                        botPasta.setVisibility(View.GONE);
                        botImagem.setVisibility(View.VISIBLE);
                    }else{
                        botCamera.setVisibility(View.VISIBLE);
                        botPasta.setVisibility(View.VISIBLE);
                        botImagem.setVisibility(View.GONE);
                        compra.foto = null;
                    }
                }
                break;
            case NOVA_FOTO:
                tirarFoto();
                break;
            case FOTO_NAO_ENCONTRADA:
                botCamera.setVisibility(View.VISIBLE);
                botPasta.setVisibility(View.VISIBLE);
                botImagem.setVisibility(View.GONE);
                compra.foto = null;
                break;
        }
        if(requestCode == CAMERA && resultCode == RESULT_OK){
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(arquivoFoto)));
            Toast.makeText(this, "Foto Salva com Sucesso", Toast.LENGTH_LONG).show();
            if (compra.foto != null) {
                compra.deletarFoto();
            }
            compra.foto = imagem;
            botCamera.setVisibility(View.GONE);
            botPasta.setVisibility(View.GONE);
            botImagem.setVisibility(View.VISIBLE);
            if(compra.codigo != 0){
                compRep.alterar(compra);
            }
        }

        if(requestCode == ESCOLHER_ARQUIVO && resultCode == RESULT_OK && data != null){

            Uri imageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                File foto = criarArquivo();
                foto.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = null;
                fos = new FileOutputStream(foto);
                fos.write(bitmapdata);

                fos.flush();
                fos.close();

                compra.foto = foto;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            String path = getPath(this, data.getData());
            if(!compra.foto.exists()){
                compra.foto = null;
                Toast.makeText(this, "Imagem inválida",Toast.LENGTH_LONG).show();
                escolherFoto();
                return;
            }
            botCamera.setVisibility(View.GONE);
            botPasta.setVisibility(View.GONE);
            botImagem.setVisibility(View.VISIBLE);
            if(compra.codigo != 0){
                compRep.alterar(compra);
            }
            Toast.makeText(this, "Foto Escolhida com Sucesso", Toast.LENGTH_LONG).show();
        }
    }
}
