package com.example.controledeestoque;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

public class BootService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            ComprasRepositorio compRep = new ComprasRepositorio(context);
            compRep.definirNotificacoes();
        }
    }
}
