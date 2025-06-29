package com.example.examTracker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfRendererHelper {
    public static List<Bitmap> renderPdf(Context context, File pdfFile) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<>();

        ParcelFileDescriptor fileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

        for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
            PdfRenderer.Page page = pdfRenderer.openPage(i);

            Bitmap bitmap = Bitmap.createBitmap(
                    page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            bitmaps.add(bitmap);
            page.close();
        }

        pdfRenderer.close();
        fileDescriptor.close();

        return bitmaps;
    }
}
