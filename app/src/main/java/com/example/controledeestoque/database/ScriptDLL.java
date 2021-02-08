package com.example.controledeestoque.database;

public class ScriptDLL {


    public static String getCreateTableProduto(){

        StringBuilder sql = new StringBuilder();

        sql.append("  CREATE TABLE PRODUTOS (");
        sql.append("  CODIGO INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sql.append("  NOME STRING NOT NULL, ");
        sql.append("  MARCA STRING NOT NULL, ");
        sql.append("  UNIDADE STRING NOT NULL, ");
        sql.append("  CATEGORIA STRING NOT NULL, ");
        sql.append("  PRECO REAL NOT NULL, ");
        sql.append("  QUANTIDADE REAL NOT NULL,  ");
        sql.append("  DURACAO REAL NOT NULL )  ");

        return sql.toString();
    }

    public static String getCreateTableCompras(){

        StringBuilder sql = new StringBuilder();

        sql.append(" CREATE TABLE COMPRAS (");
        sql.append(" CODIGO integer primary key autoincrement, ");
        sql.append(" DATA STRING, ");
        sql.append(" TOTAL REAL NOT NULL, ");
        sql.append(" PRODUTOS STRING, ");
        sql.append(" LOCAL STRING, ");
        sql.append(" EFETIVADA BOOLEAN NOT NULL, ");
        sql.append(" LOC_IMAGEM STRING, ");
        sql.append(" LOCMAP STRING ) ");

        return sql.toString();

    }
}
