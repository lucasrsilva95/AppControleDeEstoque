package com.example.controledeestoque.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class BancoOpenHelper extends SQLiteOpenHelper {
    public BancoOpenHelper(Context context) {
        super(context, "BANCO", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScriptDLL.getCreateTableCompras());
        db.execSQL(ScriptDLL.getCreateTableProduto());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists COMPRAS;");
        db.execSQL("drop table if exists PRODUTOS;");
        onCreate(db);
    }
}
