package com.example.controledeestoque;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;

public class Mapa extends AppCompatActivity implements OnMapReadyCallback {

    private String locMap;
    private double lat,longi;
    public boolean mostrarMapa;
    public Button btnAtualizarLoc;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101, REMOVER_LOC_MAPA = 104, LOC_MAPA_ALTERADO = 111;

    public LatLng latLng;

    public ComprasRepositorio compRep;
    public Compra compra;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        getSupportActionBar().setTitle("Local da Compra");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btnAtualizarLoc = (Button) findViewById(R.id.btnSalvarNovaLoc);
        btnAtualizarLoc.setVisibility(View.INVISIBLE);
        compRep = new ComprasRepositorio(this);
        Bundle bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("COMPRA"))) {
            compra = (Compra) bundle.getSerializable("COMPRA");
            locMap = compra.locMap;
            locMap = locMap.replace(",",".");
            String[] array = locMap.split(";");
            lat = Double.parseDouble(array[0]);
            longi = Double.parseDouble(array[1]);
        }

        abrirMapa();
    }

    private void abrirMapa() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
//        Task<Location> task = fusedLocationProviderClient.getLastLocation();
//        Toast.makeText(getApplicationContext(), String.format("Latitude: %f\nLongitude: %f",lat,longi), Toast.LENGTH_SHORT).show();
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_maps);
        supportMapFragment.getMapAsync(Mapa.this);
    }

    private void obterLocAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    locMap = String.format("%f;%f",currentLocation.getLatitude(),currentLocation.getLongitude());
                    locMap = locMap.replace(",",".");
                    String[] array = locMap.split(";");
                    lat = Double.parseDouble(array[0]);
                    longi = Double.parseDouble(array[1]);

                    compra.locMap = locMap;

                    abrirMapa();

//                    Toast.makeText(getApplicationContext(), "Local Atualizado com Sucesso",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void salvarNovaLoc(View view){
        compra.locMap = locMap;
        if(compra.codigo != 0){
            compRep.alterar(compra);
        }
        Toast.makeText(this, "Localização Atualizada com Sucesso",Toast.LENGTH_LONG).show();
        Intent it = new Intent();
        it.putExtra("Coordenadas",compra.locMap);
        setResult(LOC_MAPA_ALTERADO, it);
        finish();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        latLng = new LatLng(lat, longi);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Local da Compra");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        googleMap.addMarker(markerOptions);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirMapa();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mapa,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
//                Intent it = new Intent();
//                it.putExtra("Coordenadas",compra.locMap);
//                setResult(LOC_MAPA_ALTERADO, it);
                finish();
                break;
            case R.id.menuItem_del:
                AlertDialog.Builder dlgDel = new AlertDialog.Builder(this)
                        .setTitle("Deletar Localização")
                        .setMessage("Tem certeza que deseja deletar a localização salva?")
                        .setNegativeButton("Não",null)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(REMOVER_LOC_MAPA);
                                finish();
                            }
                        });
                dlgDel.show();
                break;
            case R.id.menuItem_atualizar:
                obterLocAtual();
                btnAtualizarLoc.setVisibility(View.VISIBLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
