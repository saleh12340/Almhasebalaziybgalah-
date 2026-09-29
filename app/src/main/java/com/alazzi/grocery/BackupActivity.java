package com.alazzi.grocery;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.io.*;

public class BackupActivity extends androidx.appcompat.app.AppCompatActivity {
    private static final int SAVE=301,OPEN=302;
    @Override protected void onCreate(Bundle b){super.onCreate(b);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(12,12,12,12);r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);r.setBackgroundColor(Ui.BG);r.addView(Ui.titleBar(this,"النسخ الاحتياطي والاستعادة",v->finish(),v->{}),new LinearLayout.LayoutParams(-1,64));
        Button a=Ui.button(this,"حفظ نسخة محلية",v->save());Button o=Ui.button(this,"استعادة نسخة",v->open());Button g=Ui.button(this,"Google Drive",v->drive());r.addView(a,new LinearLayout.LayoutParams(-1,56));r.addView(o,new LinearLayout.LayoutParams(-1,56));r.addView(g,new LinearLayout.LayoutParams(-1,56));TextView info=Ui.text(this,"النسخة تتضمن قاعدة البيانات المحلية كاملة؛ استخدمها للاستعادة على جهاز آخر.",14,false,Ui.MUTED);r.addView(info,new LinearLayout.LayoutParams(-1,70));setContentView(r);}
    private void save(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/octet-stream");i.putExtra(Intent.EXTRA_TITLE,"alazzi_grocery_backup.db");startActivityForResult(i,SAVE);}
    private void open(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,OPEN);}
    private void drive(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);startActivityForResult(i,OPEN);}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null)return;try{if(request==SAVE){File src=new File(getCacheDir(),"alazzi_grocery_backup.db");SharePrint.copyDb(this,src);try(FileInputStream in=new FileInputStream(src);OutputStream out=getContentResolver().openOutputStream(data.getData())){byte[]b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}Toast.makeText(this,"تم حفظ النسخة ✓",Toast.LENGTH_SHORT).show();}else{InputStream in=getContentResolver().openInputStream(data.getData());File dest=getDatabasePath("alazzi_grocery.db");AppDatabase.get(this).close();try(OutputStream out=new FileOutputStream(dest)){byte[]b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}AppDatabase.reset(this);Toast.makeText(this,"تمت الاستعادة ✓",Toast.LENGTH_LONG).show();}}catch(Exception e){new AlertDialog.Builder(this).setMessage(e.getMessage()).setPositiveButton("حسنًا",null).show();}}
}
