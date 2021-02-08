package com.example.controledeestoque;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.MenuItem;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.controledeestoque.database.BackupBanco;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;
import com.example.controledeestoque.dominio.repositorio.ProdutosRepositorio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {

    private int hora,minuto;
    public static boolean criandoBackup = true, atualizarCompFut = false;

    private final int ATUALIZAR = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

//    public void backup(boolean backup){
//        criandoBackup = backup;
//    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements TimePickerDialog.OnTimeSetListener {
        private ListPreference periodo,diaSemana,diaMes,detPerComp,maxPrevComp;
        private Preference horaNotif, salvar_backup, carregar_backup;

        private ComprasRepositorio compRep;
        private ProdutosRepositorio prodRep;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            periodo = (ListPreference)findPreference("period_comp");
            diaSemana = (ListPreference)findPreference("diaSemana_comp");
            diaMes = (ListPreference)findPreference("diaMes_comp");
            detPerComp = (ListPreference)findPreference("detalhe_period_comp");
            horaNotif = (Preference)findPreference("horaNotif");
            salvar_backup = (Preference)findPreference("salvar_backup");
            carregar_backup = (Preference)findPreference("carregar_backup");
            maxPrevComp = (ListPreference) findPreference("max_comp");

            compRep = new ComprasRepositorio(getContext());
            prodRep = new ProdutosRepositorio(getContext());

//            teste.getSharedPreferences().edit().putString()
            horaNotif.setSummary(horaNotif.getSharedPreferences().getString("horaNotif","09:00"));

            String val = periodo.getValue();
            if("Semanal".contains(val)){
                diaSemana.setVisible(true);
                diaMes.setVisible(false);
                detPerComp.setEntries(R.array.detalhe_Period_Semana_entries);
            }else{
                diaSemana.setVisible(false);
                diaMes.setVisible(true);
                detPerComp.setEntries(R.array.detalhe_Period_Mes_entries);
            }
            periodo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    atualizarCompFut = true;
                    String val = newValue.toString();
                    detPerComp.setValue("1");
                    if("Semanal".contains(val)){
                        diaSemana.setVisible(true);
                        diaMes.setVisible(false);
                        detPerComp.setEntries(R.array.detalhe_Period_Semana_entries);
                    }else{
                        diaSemana.setVisible(false);
                        diaMes.setVisible(true);
                        detPerComp.setEntries(R.array.detalhe_Period_Mes_entries);
                    }
                    return true;
                }
            });
            diaSemana.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    atualizarCompFut = true;
                    return true;
                }
            });
            diaMes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    atualizarCompFut = true;
                    return true;
                }
            });
            detPerComp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    atualizarCompFut = true;
                    return true;
                }
            });

            horaNotif.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    definirHoraNotif();
                    return true;
                }
            });

            salvar_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    criandoBackup = true;
                    Date data = new Date(System.currentTimeMillis());
                    SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy-HH:mm");
                    Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/Controle_De_Estoque/Backups");
                    File pasta = new File(uri.getPath());
                    if(!pasta.exists()){
                        pasta.mkdirs();
                    }
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .setType("application/octet-stream")
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .putExtra("android.provider.extra.INITIAL_URI", uri)
                            .putExtra(Intent.EXTRA_TITLE, "Backup_Controle_De_Estoque_" + formatarData.format(data) + ".db");

                    startActivityForResult(intent, 120);
                    return false;
                }
            });

            carregar_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    criandoBackup = false;
                    Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/Controle_De_Estoque/Backups");
                    File pasta = new File(uri.getPath());
                    if(!pasta.exists()){
                        pasta.mkdirs();
                    }
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .setType("application/octet-stream")
                        .putExtra("android.provider.extra.INITIAL_URI", uri);
                    startActivityForResult(intent, 0);

                    return true;
                }
            });
        }

        public void definirHoraNotif(){
            String horaConfig = horaNotif.getSummary().toString();
            int hora = 0;
            int min = 0;
            if(horaConfig.length() > 3){
                hora = Integer.parseInt(horaConfig.substring(0,2));
                min = Integer.parseInt(horaConfig.substring(3,5));
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), this, hora,min,true);
            timePickerDialog.show();
        }


        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hora = String.format("%02d:%02d", hourOfDay, minute);
            horaNotif.setSummary(hora);
            horaNotif.getSharedPreferences().edit().putString("horaNotif", hora).apply();
            ComprasRepositorio compRep = new ComprasRepositorio(getContext());
            compRep.definirNotificacoes(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                if (atualizarCompFut){
                    ComprasRepositorio compRep = new ComprasRepositorio(getApplicationContext());
                    compRep.atualizarComprasFuturas();
                    setResult(ATUALIZAR);
                }
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BackupBanco backupBanco = new BackupBanco(getApplicationContext());
        if(data != null){
            if (criandoBackup) {
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                    backupBanco.salvarBackup(outputStream);
                    Toast.makeText(getApplicationContext(),"Backups salvos com sucesso",Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    backupBanco.restaurarBackup(inputStream);
                    Toast.makeText(getApplicationContext(),"Backups carregado com sucesso",Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}