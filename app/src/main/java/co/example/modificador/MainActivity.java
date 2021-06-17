package co.example.modificador;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;

    private Button boton;
    private LinearLayout layoutContenedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // tener en cuenta archivo creado en carperta res/xml
        // tener encuenta midificacion realizada an AndroidManifest.xml
        // tener encuenta los permisos pedidos en AndroidManifest.xml

        boton = findViewById(R.id.btn);
        layoutContenedor = findViewById(R.id.lnlContenedor);

        funBotones();



    }

    private void funBotones() {

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    crearPdf();

                } else {
                    requestStoragePermission();
                }



            }
        });


    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permiso de llamada necesario")
                    .setMessage("para completar esta acccion es necesario otorgar el permiso de llamar")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void crearPdf() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;



        Bitmap bitmapReporte = getBitmapFromView(layoutContenedor, layoutContenedor.getHeight(), layoutContenedor.getWidth());


        createPdfFromView(bitmapReporte, "Nombre PFD", width, height, 1); // para pdf

    }

    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }


    /////////////////////////////
    private void createPdfFromView(Bitmap bitmapReporte, String fileName, int pageWidth, int pageHeight, int pageNumber) {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, fileName.concat(".pdf"));



        if (!file.exists()){
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            file = new File(path, fileName.concat(".pdf"));
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        if (file.exists()) {

            PdfDocument document = new PdfDocument();

            ////////////////////////////////////
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmapReporte.getWidth(), bitmapReporte.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawPaint(paint);
            paint.setColor(Color.BLUE);
            canvas.drawBitmap(bitmapReporte, 0, 0, null);
            document.finishPage(page);



            try {
                Toast.makeText(this, "Generando...", Toast.LENGTH_SHORT).show();
                document.writeTo(fOut);
            } catch (IOException e) {
                Toast.makeText(this, "Failed...", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            document.close();

            Uri contentUri = FileProvider.getUriForFile(this, this.getApplicationContext()
                    .getPackageName() + ".fileprovider", file);

            compartir(contentUri);



        } else {


            Toast.makeText(this, "Carptera Descargas no encontrada favor reportar al desarrollador", Toast.LENGTH_SHORT).show();
        }

    }

    private void compartir(Uri contentUri) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addCategory(Intent.CATEGORY_DEFAULT);
        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setType("application/*");
        startActivityForResult(Intent.createChooser(share, "Compartir con:"), 45);
    }


}
