# Ukladanie dokumentov
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko
[app/src/java/com/parohy/scopedstorage/ui/save/video/private.kt](./private.kt)
Pri praci s privatnym uloziskom viem, len ak viem kam idem co ukladat. Ak by sme chceli dat moznost pouzivatelovi vybrat si kam chce ulozit subor, musime pouzit kniznicu alebo vytvorit logiku sami, ktora by nam umoznila prehladavat priecinky v privatnom ulozisku.
Z logickeho hladiska, toto by vobec nedavalo zmysel. Osobne som sa nestretol este s usecasom, kde by mal user moznost si vybrat kam do privatneho uloziska chce nieoc ulozit...
Ak by som chcel zaroven pracovat s privatnym suborom externe mimo aplikacie, je potrebne pouzit `FileProvider`.

### FileProvider
`FileProvider` je framework, ktory umoznuje zdielat subory s inymi aplikaciami. Tento framework je potrebne nastavit v `AndroidManifest.xml` a vytvorit `res/xml/provider_paths.xml`.
V skratke co `FileProvider` zmeni uri `file://` na `content://`. Tymto sposobom sa zabezpeci, ze subor je zdielatelny s inymi aplikaciami. Zaroven tymto sposobom
vieme menezovat aj prava k tomuto suboru. Ak by sme chceli menit prava k suboru za pomoci file uri `file://`, museli by sme menit na urovni systemu. Tymto ten subor je otvoreny
aj pre ine externe aplikacie nie len pre tu, ktorej chceme subor zdielat.

**provider_paths.xml**
V tomto xml subore definujeme cesty k priecinkom, ktore `FileProvider` moze zdielat. Zaroven to zabezpecuje aj istu anonimitu pre subory o ich lokacii.
Priklad:
`/0/storage/data/com.parohy.scopedstorage/files/obrazky/obrazok.jpg`
Ak to chcem zdielat cez `FileProvider`, potrebujeme mu zadefinovat pristupy:
`<files-path name="obrazocky" path="/obrazky" />`
- `files-path` nam poukazuje na `filesDir`
- `name` je alias, pod ktorym sa bude dana cesta skryvat
- `path` je relativna cesta k priecinku, ktory chceme zdielat
    - mozme pouzit aj `.` cim davame pristup k celemu `filesDir`

Ak si vypiseme taketo `Uri` do konzoly, dostaneme nieco take:
`content://com.parohy.scopedstorage.fileprovider/obrazocky/obrazok.jpg`

## Verejne ulozisko

### Movies
[app/src/java/com/parohy/scopedstorage/ui/save/video/movies.kt](./pictures.kt)
Na vlozenie suboru do Documents pouzijeme `ContentResolver` a `MediaStore`([docs](https://developer.android.com/training/data-storage/shared/media)).
Pre **Android 9 a nizsie** je potrebne si pytat `WRITE_EXTERNAL_STORAGE` opravnenie.
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
  block()
else
  if (isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    block()
  else
    requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
      block()
    }
```

Vkladanie suboru do Movies:
```kotlin
  private fun Context.videoUriInsideMovies(fileName: String): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
  }

  val collection: Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/")
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    } else {
      val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

      // POZOR! Je potrebne skontrolovat ci existuje
      if (!dir.exists())
        dir.mkdirs()

      val filePath = "${dir.absolutePath}/$fileName"

      contentValues.put(MediaStore.Video.Media.DATA, filePath)
      MediaStore.Video.Media.getContentUri("external")
    }

  return contentResolver.insert(collection, contentValues)
}
```

### Downloads
[app/src/java/com/parohy/scopedstorage/ui/save/video/downloads.kt](./downloads.kt)
Na vlozenie suboru do Documents pouzijeme `ContentResolver` a `MediaStore`([docs](https://developer.android.com/training/data-storage/shared/media)).
Pre **Android 9 a nizsie** je potrebne si pytat `WRITE_EXTERNAL_STORAGE` opravnenie.
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
  block()
else
  if (isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    block()
  else
    requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
      block()
    }
```

Vkladanie suboru do Documents:
```kotlin
  private fun videoUriInsideDownloads(fileName: String): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
  }

  val collection: Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
      MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
      val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

      // POZOR! Je potrebne skontrolovat ci existuje
      if (!dir.exists())
        dir.mkdirs()

      val filePath = "${dir.absolutePath}/$fileName"

      contentValues.put(MediaStore.Downloads.DATA, filePath)
      MediaStore.Files.getContentUri("external")
    }

  return contentResolver.insert(collection, contentValues)
}
 ```

### Vlastny priecinok
[app/src/java/com/parohy/scopedstorage/ui/save/video/custom.kt](./custom.kt)
Ak chceme vkladat do vlastneho priecinka, mozme pouzit rovnaky postup ako pri Documents a Downloads:
Pozor, treba si pytat `WRITE_EXTERNAL_STORAGE` pre **Android 9 a nizsie**.
Do parametra, kam uvadzame `MediaStore.Files.FileColumns.RELATIVE_PATH` alebo `MediaStore.Files.FileColumns.DATA` vkladame cestu naseho custom priecinka.
```kotlin
    ...
val collection: Uri =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, "/MojPriecinok")
    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
  } else {
    val storage = Environment.getExternalStorageDirectory()
    val dir = File("$storage/MojPriecinok")

    // POZOR! Je potrebne skontrolovat ci existuje
    if (!dir.exists())
      dir.mkdirs()

    val filePath = "${dir.absolutePath}/$fileName"

    contentValues.put(MediaStore.Files.FileColumns.DATA, filePath)
    MediaStore.Files.getContentUri("external")
  }
    ...
```
**Alebo** pouzijeme SAF a manualne si zvolime priecinok. Tu si pytat permission nemusime.
```kotlin
private var _onFileCreated: ((Uri?) -> Unit)? = null

/*Preco ma kazdy typ vlastny CreateDocument? Pozri si @Deprecated koment pre triedu CreateDocument...*/
private val createVideoDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri: Uri? ->
  _onFileCreated?.invoke(uri)
  _onFileCreated = null
}

fun captureVideoAndStoreToCustom(onResult: (Uri?) -> Unit) {
  val block = {
    _onFileCreated = { uri ->
      if (uri != null) // TODO: Handluj ak sa nepodari vytvorit
        captureVideo(uri, onResult)
    }
    /*
    * Na vytvaranie suborov na konkretnych miestach pouzivame SAF. Tento uz ma integrovany Scoped Storage
    * To znamena, ze nepotrebujeme WRITE_EXTERNAL_STORAGE permission
    * */
    createVideoDocument.launch("Custom_SS_${System.currentTimeMillis()}.mp4")
  }

  if (isGranted(android.Manifest.permission.CAMERA))
    block()
  else
    requestPermission(android.Manifest.permission.CAMERA) {
      block()
    }
}
```
