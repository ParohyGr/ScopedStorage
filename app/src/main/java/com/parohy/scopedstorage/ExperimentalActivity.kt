package com.parohy.scopedstorage

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.parohy.scopedstorage.ui.pdfFilePages
import com.parohy.scopedstorage.ui.pdfRendererState
import com.parohy.scopedstorage.ui.theme.ScopedStorageTheme
import kotlinx.coroutines.sync.Mutex
import java.util.Calendar
import kotlin.math.sqrt

class ExperimentalActivity : ComponentActivity() {
  private val fileUri: MutableState<Uri?> = mutableStateOf(null)
  
  //SAF
  private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    fileUri.value = uri
  }

  //SAF
  private var onCreateDocument: ((Uri?) -> Unit)? = null
  private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
    onCreateDocument?.invoke(uri) // Handle null
    onCreateDocument = null
  }

  private val isSuccess: MutableState<Boolean?> = mutableStateOf(null)
  private var onPermissionGranted: (() -> Unit)? = null
  private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) // Handle false
      onPermissionGranted?.invoke()
    onPermissionGranted = null
  }

  private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
    isSuccess.value = success
  }
  private val takeVideo = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
    isSuccess.value = success
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ScopedStorageTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Surface(modifier = Modifier.padding(innerPadding)) {
//            1. Vyberem obrazok co som odfotil kamerou
            VyberemObrazok(fileUri.value) {
              openFile.launch(arrayOf("image/*"))
            }

//            2. Odfotim obrazok v aplikaci a ulozim do obrazkov
//            OdfotimObrazok(isSuccess = isSuccess.value) { uri ->
//              uri?.also {
//                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                  takePicture.launch(it)
//                } else {
//                  onPermissionGranted = {
//                    takePicture.launch(it)
//                  }
//                  requestPermission.launch(android.Manifest.permission.CAMERA)
//                }
//              } ?: Toast.makeText(this, "Uri is null", Toast.LENGTH_SHORT).show()
//            }

//            3. Vyberiem video co som natocil kamerou
//            VyberiemVideo(fileUri.value) {
//              openFile.launch(arrayOf("video/*"))
//            }

//            4. Natocim video
//            NatocimVideo(isSuccess = isSuccess.value) { uri ->
//              uri?.also {
//                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                  takeVideo.launch(it)
//                } else {
//                  onPermissionGranted = {
//                    takeVideo.launch(it)
//                  }
//                  requestPermission.launch(android.Manifest.permission.CAMERA)
//                }
//              } ?: Toast.makeText(this, "Uri is null", Toast.LENGTH_SHORT).show()
//            }

//            5. Otvorim dokument
//              OtvorimDokument(fileUri = fileUri.value) {
//                openFile.launch(arrayOf("application/pdf"))
//              }

//            6. Vytvorim dokument DOCUMENTS
//            VytvorimDokument_DOCUMENTS(fileUri = fileUri.value) {
//              if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                onPermissionGranted = {
//                  fileUri.value = createPdfDocumentDOCUMENTS()
//                }
//                requestPermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//              } else
//                fileUri.value = createPdfDocumentDOCUMENTS()
//            }

//            7. Vytvorim dokument CUSTOM
//            VytvorimDokument_CUSTOM(fileUri = fileUri.value) {
//              onCreateDocument = { uri ->
//                uri?.also {
//                  justCreatePdfDocument(it)
//                } ?: Toast.makeText(this, "Uri is null", Toast.LENGTH_SHORT).show()
//
//                fileUri.value = uri
//              }
//
//              createDocument.launch("PDF_${System.currentTimeMillis()}.pdf")
//            }
          }
        }
      }
    }
  }
}

/*region Vyberem obrazok co som odfotil kamerou*/
@Composable
fun VyberemObrazok(imageUri: Uri?, onClick: () -> Unit) {
  val context = LocalContext.current
  val bitmap = remember { mutableStateOf<Bitmap?>(null) }

  LaunchedEffect(imageUri) {
    if (imageUri != null) {
      val inputStream = context.contentResolver.openInputStream(imageUri)
      bitmap.value = BitmapFactory.decodeStream(inputStream)
    }
  }

  Column {
    bitmap.value?.also {
      Image(
        modifier = Modifier.sizeIn(100.dp, 100.dp).clickable { onClick() },
        bitmap = it.asImageBitmap(),
        contentDescription = "Obrazok")
    } ?: run {
      Button(onClick = onClick) {
        Text("Vyber obrazok")
      }
    }
  }
}
/*endregion*/

/*region Odfotim obrazok v aplikaci a ulozim do obrazkov*/
@Composable
fun OdfotimObrazok(isSuccess: Boolean?, onClick: (Uri?) -> Unit) {
  val context = LocalContext.current
  val imageUri = remember { mutableStateOf<Uri?>(null) }
  val bitmap = remember { mutableStateOf<Bitmap?>(null) }

  val onClickFun = {
    val uri = context.createImageUri()
    imageUri.value = uri
    onClick(uri)
  }

  LaunchedEffect(imageUri, isSuccess) {
    val uri = imageUri.value
    if (uri != null && isSuccess == true) {
      val inputStream = context.contentResolver.openInputStream(uri)
      bitmap.value = BitmapFactory.decodeStream(inputStream)
    }
  }

  Column {
    isSuccess?.also {
      Text(text = if (it) "Odfotenie bolo uspesne" else "Odfotenie bolo neuspesne")
    }
    bitmap.value?.also {
      Image(
        modifier = Modifier.sizeIn(100.dp, 100.dp).clickable { onClickFun() },
        bitmap = it.asImageBitmap(),
        contentDescription = "Obrazok")
    } ?: run {
      Button(onClick = onClickFun) {
        Text("Odfotim obrazok")
      }
    }
  }
}

private fun Context.createImageUri(): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
  }

  return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}
/*endregion*/

/*region vyberiem video*/
@Composable
fun VyberiemVideo(videoUri: Uri?, onClick: () -> Unit) {
  val context = LocalContext.current
  val exoPlayer = ExoPlayer.Builder(context).build()

  val mediaSource = remember(videoUri) {
    videoUri?.let { MediaItem.fromUri(it) }
  }

  LaunchedEffect(mediaSource) {
    if (mediaSource != null) {
      exoPlayer.setMediaItem(mediaSource)
      exoPlayer.prepare()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      exoPlayer.release()
    }
  }

  Column {
    if (mediaSource != null)
      AndroidView(
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(450.dp)
      )

    Button(onClick = onClick) {
      Text("Vyber video")
    }
  }
}
/*endregion*/

/*region Natocim video v aplikacii a ulozim do videii*/
@Composable
fun NatocimVideo(isSuccess: Boolean?, onClick: (Uri?) -> Unit) {
  val context = LocalContext.current
  val exoPlayer = ExoPlayer.Builder(context).build()

  val videoUri = remember { mutableStateOf<Uri?>(null) }

  LaunchedEffect(videoUri.value, isSuccess) {
    val uri = videoUri.value
    if (uri != null && isSuccess == true) {
      val mediaItem = MediaItem.fromUri(uri)
      exoPlayer.setMediaItem(mediaItem)
      exoPlayer.prepare()
    }
  }

  val onClickFun = {
    val uri = context.createVideoUri()
    videoUri.value = uri
    onClick(uri)
  }

  DisposableEffect(Unit) {
    onDispose {
      exoPlayer.release()
    }
  }

  Column {
    if (isSuccess == false)
      Text("Natocenie bolo neuspesne")

    if (isSuccess == true)
      AndroidView(
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(450.dp)
      )

    Button(onClick = onClickFun) {
      Text("Natoc video")
    }
  }
}
private fun Context.createVideoUri(): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, "VIDEO_${System.currentTimeMillis()}.mp4")
    put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
  }

  return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
}
/*endregion*/

/*Preco sa pouziva MediaStore a nepracujem s Enviroment.getExternalStoragePublicDirectory ?
* 1. Enviroment vracia priamo cestu k suboru.
*   - praca priamo so subormi
*   - mikromanazment
*   - Android 10+ obmedzene prava na cesty
*   - potrebujete pytat prava alebo aj MANAGE_EXTERNAL_STORAGE
* 2. MediaStore vracia Uri na priecinok s pozadovanym mediom
*   - konzistenica napriec android zariadeniami - jedna cesta
*   - spolurapcuje s media scanner
*   - MediaStore je scoped storage
* */

/*region Otvorim dokument*/
@Composable
fun OtvorimDokument(fileUri: Uri?, onClick: () -> Unit) {
  if (fileUri != null) {
    BoxWithConstraints {
      val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
      val height = (width * sqrt(2f)).toInt()

      val mutex = remember { Mutex() }
      val renderer by pdfRendererState(fileUri, mutex) { /*onError*/ }
      val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
      LazyColumn(Modifier.fillMaxSize()) {
        pdfFilePages(fileUri, width, height, pageCount, mutex, renderer)
      }
    }
  } else {
    Button(onClick = onClick) {
      Text("Otvor dokument")
    }
  }
}
/*endregion*/

/*region Vytvorim dokument*/
@Composable
fun VytvorimDokument_DOCUMENTS(fileUri: Uri?, onClick: () -> Unit) {
  if (fileUri != null) {
    BoxWithConstraints {
      val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
      val height = (width * sqrt(2f)).toInt()

      val mutex = remember { Mutex() }
      val renderer by pdfRendererState(fileUri, mutex) { /*onError*/ }
      val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
      LazyColumn(Modifier.fillMaxSize()) {
        pdfFilePages(fileUri, width, height, pageCount, mutex, renderer)
      }
    }
  } else {
    Button(onClick = onClick) {
      Text("Vytvor dokument DOCUMENTS")
    }
  }
}

@Composable
fun VytvorimDokument_CUSTOM(fileUri: Uri?, onClick: () -> Unit) {
  if (fileUri != null) {
    BoxWithConstraints {
      val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
      val height = (width * sqrt(2f)).toInt()

      val mutex = remember { Mutex() }
      val renderer by pdfRendererState(fileUri, mutex) { /*onError*/ }
      val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
      LazyColumn(Modifier.fillMaxSize()) {
        pdfFilePages(fileUri, width, height, pageCount, mutex, renderer)
      }
    }
  } else {
    Button(onClick = onClick) {
      Text("Vytvor dokument CUSTOM")
    }
  }
}

private fun Context.createPdfDocumentDOCUMENTS(): Uri? =
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

      val fileUri = documentUriDocuments() //todo handle null

      contentResolver.openOutputStream(fileUri!!)?.run {
        writeTo(this) // PdfDocument.writeTo(OutputStream)

        // close OutputStream
        close()
      }

      // close PdfDocument
      close()

      fileUri
    } catch (e: Exception) {
      e.printStackTrace()
      null
    } finally {
      close()
    }
  }

private fun Context.documentUriDocuments(): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Files.FileColumns.DISPLAY_NAME, "PDF_${System.currentTimeMillis()}.pdf")
    put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
  }

  val collection: Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
      MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
      val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

      // POZOR! Je potrebne skontrolovat ci existuje
      if (!dir.exists())
        dir.mkdirs()

      val filePath = dir.absolutePath + "/PDF_${System.currentTimeMillis()}.pdf"

      contentValues.put(MediaStore.Files.FileColumns.DATA, filePath)
      MediaStore.Files.getContentUri("external")
    }

  return contentResolver.insert(collection, contentValues)
}

private fun Context.justCreatePdfDocument(destinationUri: Uri) =
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
/*endregion*/

/*Kedy pouzit MediaStore a kedy SAF
* MediaStore
*  - praca s media subormi: Obrazky, Video, Audio a Dokumenty
*  - integorvany ScopedStorage - pri pouziti NETREBA riesit WRITE_EXTERNAL_STORAGE
*  - kompatabilita so starsou verziou Androidu
*  - automaticke skenovanie medii
* SAF
*  - vhodne na pracu so subormi, ktore neukladame na vseobecne miesta
*  - integorvany ScopedStorage - pri pouziti TREBA riesit WRITE_EXTERNAL_STORAGE
*  - kompatabilita so starsou verziou Androidu
*  - moznost pristupu k suborom na vzdialenych uloziskach
*  - persistencia read/write prav uri suboru
* */

/*region Nacitaj obrazky do aplikacie*/
@Composable
fun NacitajObrazky() {

}
/*endregion*/