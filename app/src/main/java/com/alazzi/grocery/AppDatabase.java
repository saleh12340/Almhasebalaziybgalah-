package com.alazzi.grocery;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class AppDatabase extends SQLiteOpenHelper {
    private static final String NAME = "alazzi_grocery.db";
    private static final int VERSION = 1;
    private static AppDatabase instance;

    public static synchronized AppDatabase get(Context c) {
        if (instance == null) instance = new AppDatabase(c.getApplicationContext());
        return instance;
    }

    private AppDatabase(Context c) { super(c, NAME, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,phone TEXT DEFAULT '',balance REAL DEFAULT 0)");
        db.execSQL("CREATE TABLE suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,phone TEXT DEFAULT '',balance REAL DEFAULT 0)");
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,barcode TEXT DEFAULT '',unit TEXT DEFAULT '',buy_price REAL DEFAULT 0,sell_price REAL DEFAULT 0,stock REAL DEFAULT 0)");
        db.execSQL("CREATE TABLE warehouses(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");
        db.execSQL("CREATE TABLE currencies(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,symbol TEXT DEFAULT '',rate REAL DEFAULT 1)");
        db.execSQL("CREATE TABLE invoices(id INTEGER PRIMARY KEY AUTOINCREMENT,number INTEGER NOT NULL UNIQUE,type TEXT NOT NULL,party_id INTEGER,party_name TEXT DEFAULT '',total REAL DEFAULT 0,paid REAL DEFAULT 0,remaining REAL DEFAULT 0,date TEXT NOT NULL)");
        db.execSQL("CREATE TABLE invoice_items(id INTEGER PRIMARY KEY AUTOINCREMENT,invoice_id INTEGER NOT NULL,product_id INTEGER,product_name TEXT NOT NULL,qty REAL DEFAULT 0,unit_price REAL DEFAULT 0,total REAL DEFAULT 0)");
        db.execSQL("CREATE TABLE operations(id INTEGER PRIMARY KEY AUTOINCREMENT,party_type TEXT NOT NULL,party_id INTEGER,details TEXT DEFAULT '',debit REAL DEFAULT 0,credit REAL DEFAULT 0,balance REAL DEFAULT 0,date TEXT NOT NULL)");
        db.execSQL("CREATE TABLE transfers(id INTEGER PRIMARY KEY AUTOINCREMENT,sender TEXT DEFAULT '',receiver TEXT DEFAULT '',phone TEXT DEFAULT '',amount REAL DEFAULT 0,details TEXT DEFAULT '',date TEXT NOT NULL)");
        db.execSQL("CREATE TABLE notes(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT DEFAULT '',body TEXT DEFAULT '',date TEXT NOT NULL)");
        db.execSQL("CREATE TABLE settings(key TEXT PRIMARY KEY,value TEXT DEFAULT '')");
    }

    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { }
}
