package com.example.controledeestoque;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.controledeestoque.dominio.entidades.Compra;
import com.example.controledeestoque.dominio.repositorio.ComprasRepositorio;

import java.text.ParseException;

public class Receiver extends BroadcastReceiver {

    Compra comprafut;
    ComprasRepositorio compRep;
    Bundle bundle;


    @Override
    public void onReceive(Context context, Intent intent) {
        compRep = new ComprasRepositorio(context);
        comprafut = new Compra();
        comprafut = compRep.compraDeHoje();

        Intent it = new Intent(context, DetalhesCompra.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,it,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyCompra")
                .setContentTitle("Compra programada para Hoje")
                .setContentText(String.format("Nº de itens: %d  Valor: R$%.2f", comprafut.numItens(), comprafut.total))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_shopping_cart_black_24dp)
                .setColor(Color.argb(100,93,64,55))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(201, builder.build());
    }
}
