package com.parohy.scopedstorage.ui.save.document

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.parohy.scopedstorage.R
import java.util.Calendar

fun Context.createPdfFile(destinationUri: Uri) =
  with(PdfDocument()) {
    try {
      val bitmap = BitmapFactory.decodeResource(resources, R.drawable.gr_blog)
      val scale = Bitmap.createScaledBitmap(bitmap, 178, 252, false)

      val pageInfo = PdfDocument.PageInfo.Builder(792, 1120, 1).create()
      val page = startPage(pageInfo)

      page.canvas.drawBitmap(scale, 56f, 40f, Paint())

      val titlePaint = Paint().apply {
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.BLACK
      }

      val textPaint = Paint().apply {
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        color = Color.BLACK
      }

      page.canvas.drawText("This is a PDF file", 16f, 16f, titlePaint)
      page.canvas.drawText("Time: ${Calendar.getInstance().time}", 16f, 40f, textPaint)

      finishPage(page)

      contentResolver.openOutputStream(destinationUri)?.run {
        writeTo(this) // PdfDocument.writeTo(OutputStream)

        // close OutputStream
        close()
      }

      // close PdfDocument
      close()
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      close()
    }
  }