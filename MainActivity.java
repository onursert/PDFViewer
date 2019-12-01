import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    Context context;

    //TODO: Make Dynamic
    String FILENAME = "sample.pdf";

    ParcelFileDescriptor parcelFileDescriptor;
    PdfRenderer pdfRenderer;
    PdfRenderer.Page pdfRendererPage;

    ImageView imageView;
    Button buttonPrevious;
    Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        imageView = (ImageView) findViewById(R.id.image);

        buttonPrevious = (Button) findViewById(R.id.previous);
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrevious();
            }
        });

        buttonNext = (Button) findViewById(R.id.next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNext();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            openPdfRenderer();
            showPage(0);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to open - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openPdfRenderer() throws IOException {
        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            InputStream asset = context.getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    @Override
    public void onStop() {
        try {
            closePdfRenderer();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to close - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        super.onStop();
    }

    public void closePdfRenderer() throws IOException {
        if (pdfRendererPage != null) {
            pdfRendererPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        if (parcelFileDescriptor != null) {
            parcelFileDescriptor.close();
        }
    }

    public void showPrevious() {
        if (pdfRenderer == null || pdfRendererPage == null) {
            return;
        }
        final int index = pdfRendererPage.getIndex();
        if (index > 0) {
            showPage(index - 1);
        }
    }

    public void showNext() {
        if (pdfRenderer == null || pdfRendererPage == null) {
            return;
        }
        final int index = pdfRendererPage.getIndex();
        if (index + 1 < pdfRenderer.getPageCount()) {
            showPage(index + 1);
        }
    }

    public void showPage(int index) {
        if (pdfRendererPage != null) {
            pdfRendererPage.close();
        }
        pdfRendererPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(pdfRendererPage.getWidth(), pdfRendererPage.getHeight(), Bitmap.Config.ARGB_8888);
        pdfRendererPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imageView.setImageBitmap(bitmap);

        final int count = pdfRenderer.getPageCount();
        buttonPrevious.setEnabled(index > 0);
        buttonNext.setEnabled(index + 1 < count);
        //getSupportActionBar().setTitle("Letter " + (index + 1) + " / " + count);
    }
}
