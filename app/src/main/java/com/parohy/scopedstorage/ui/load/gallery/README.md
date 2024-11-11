# Nacitavanie galerie
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko aplikacie
[app/src/java/com/parohy/scopedstorage/ui/load/gallery/private.kt](./private.kt)
Pri privatnom ulozisku je potrebne poznat cestu k suboru. Ak cestu nepozname, stale je mozne dany subor zaujmu najist aj pomocou dokumentacie v [folder](folder/README.md).
Pri privatnom ulozisku nemusime pracovat so ziadnym specialnym frameworkom ani opravneni. Ak by som chcel pracovat s nacitanim privatnym suborom externe mimo aplikacie,
je potrebne pouzit `FileProvider`.

### FileProvider
`FileProvider` je framework, ktory umoznuje zdielat subory s inymi aplikaciami. Tento framework je potrebne nastavit v `AndroidManifest.xml` a vytvorit [`res/xml/provider_paths.xml`](../README.md#FileProvider).
V skratke co `FileProvider` zmeni uri `file://` na `content://`. Tymto sposobom sa zabezpeci, ze subor je zdielatelny s inymi aplikaciami. Zaroven tymto sposobom
vieme menezovat aj prava k tomuto suboru. Ak by sme chceli menit prava k suboru za pomoci file uri `file://`, museli by sme menit na urovni systemu. Tymto ten subor je otvoreny
aj pre ine externe aplikacie nie len pre tu, ktorej chceme subor zdielat.


## Verejne ulozisko

### Obrazky
[app/src/java/com/parohy/scopedstorage/ui/load/gallery/pictures.kt](./pictures.kt)
Na nacitanie obrazkov z pouzijeme `ContentResolver` s `MediaStore`.
```kotlin
  fun loadPicturesFromGallery(onResult: (List<Uri>) -> Unit) {
    val block: () -> List<Uri> = {
      buildList {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = contentResolver.query(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          projection,
          null,
          null,
          null
        )

        if (cursor != null && cursor.count > 0) {
          cursor.moveToFirst()
          Log.i("MediaScanner", "Found ${cursor.count} images")
          do {
            val imageId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)
            add(imageUri)
          } while (cursor.moveToNext())
          cursor.close()
        }
      }
    }

    //TODO: Pre 14+ treba handlovat READ_MEDIA_VISUAL_USER_SELECTED
    // Pre Android 13+ je potrebne READ_MEDIA_IMAGES ak chcem nacitat obrazky, ktore som nevytvoril z tejto aplikacie
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
      if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil uplny pristup ku fotkam*/
        onResult(block())
      }
      else if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil ciastocny pristup, to znamena, ze manualne volil media, ktore dovoluje zdielat s aplikaciou. Malo by to byt non-blocking,
        * cize odoslem vysledok ale popytam si opat pristup. Dodatocne pytanie si pristupu by malo byt UI/UX oddelene od hlavnej akcie, napriklad extra tlacidlo,
        * ktore by vyvolal systemovy permission na READ_MEDIA_IMAGES*/
        onResult(block())
        /*TODO: Tento call by mal byt volany dodatocne cez ine tlacidlo akciu, nie hlavnu. Ale toto je len example, ale takto to nekopiruj!*/
        requestPermission(android.Manifest.permission.READ_MEDIA_IMAGES) {
          onResult(block())
        }
      }
      else // Ak este nemam pristup, tak si ho poprosim
        requestPermission(android.Manifest.permission.READ_MEDIA_IMAGES) { // READ_MEDIA_IMAGES alebo READ_MEDIA_VIDEO ma rovnaky vysledok
          onResult(block())
        }
    // Pre Android 12 a nizsie staci READ_EXTERNAL_STORAGE
    else
      if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)
        onResult(block())
      else
        requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
          onResult(block())
        }
  }
```

### Multimedia
[app/src/java/com/parohy/scopedstorage/ui/load/gallery/multimedia.kt](./multimedia.kt)
```kotlin
  fun loadMultimediaFromGallery(onResult: (List<Uri>) -> Unit) {
    val block: () -> List<Uri> = {
      // Sucasne viem nacitat iba jednu kolekciu, musim preto pre kazdy MediaStore volat query zvlast
      val images = queryMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      val videos = queryMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI).map(::createVideoThumbnail)

      (images + videos).sortedBy(MediaInfo::dateTaken).map(MediaInfo::uri)
    }

    //TODO: Pre 14+ treba handlovat READ_MEDIA_VISUAL_USER_SELECTED
    // Pre Android 13+ je potrebne READ_MEDIA_IMAGES a READ_MEDIA_VIDEO ak chcem nacitat obrazky aj videa, ktore som nevytvoril z tejto aplikacie
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
      if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil uplny pristup ku fotkam a videam. Staci, ak povoli len jedno z nich "Allow all" a ma automaticky full pristup aj k druhemu. Preto je v podmienke OR*/
        onResult(block())
      } else if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil ciastocny pristup, to znamena, ze manualne volil media, ktore dovoluje zdielat s aplikaciou. Malo by to byt non-blocking,
        * cize odoslem vysledok ale popytam si opat pristup. Dodatocne pytanie si pristupu by malo byt UI/UX oddelene od hlavnej akcie, napriklad extra tlacidlo,
        * ktore by vyvolal systemovy permission na READ_MEDIA_IMAGES a READ_MEDIA_VIDEO*/
        onResult(block())
        /*TODO: Tento call by mal byt volany dodatocne cez ine tlacidlo akciu, nie hlavnu. Ale toto je len example, ale takto to nekopiruj!*/
        requestMultiplePermissions(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) {
          onResult(block())
        }
      }
      else // Ak este nemam pristup, tak si ho poprosim
        requestMultiplePermissions(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) {
          onResult(block())
        }
    // Pre Android 12 a nizsie staci READ_EXTERNAL_STORAGE
    else
      if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)
        onResult(block())
      else
        requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
          onResult(block())
        }
  }
```
**queryMedia**
Sucasne vieme robit query len nad jednou tabulkou. Cize ak chceme ziskat obrazky aj videa sucasne, musime pre iba kolekcie robit query zvlast a nasledne ich spojit do jedneho vysledku.
```kotlin
private fun queryMedia(collectionUri: Uri): List<MediaInfo> =
    buildList {
      val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATE_TAKEN, MediaStore.MediaColumns.MIME_TYPE)
      val cursor = contentResolver.query(
        collectionUri,
        projection,
        null,
        null,
        null
      )

      if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        Log.i("MediaScanner", "Found ${cursor.count} media in $collectionUri")
        do {
          try {
            add(
              MediaInfo(
                uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))),
                dateTaken = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)),
                mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
              )
            )
          } catch (e: Exception) {
            e.printStackTrace()
          }

        } while (cursor.moveToNext())
        cursor.close()
      }
    }
```

**createVideoThumbnail**
Vytvori mini obrazok z videa, aby sme vedeli zobrazit grafiku v UI.

### Vlastny priecinok
[app/src/java/com/parohy/scopedstorage/ui/load/gallery/custom.kt](./custom.kt)
Tu je potrebne pouzit `ActivityResultContracts.OpenDocumentTree` na ziskanie prav na zvoleny priecinok. Kedze pracujeme so SAF, prava na citanie obsahu neriesime.
```kotlin
private var _onFileResult: ((Uri?) -> Unit)? = null

private val openDocumentTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { collectionUri: Uri? ->
    _onFileResult?.invoke(collectionUri)
    _onFileResult = null
  }

  fun loadMediaFromCustomDirectory(onResult: (List<Uri>) -> Unit) {
    _onFileResult = { documentTreeUri ->
      if (documentTreeUri != null) {
        /*Ak by som chcel si pamatat zvoleny priecinok, URI by stratilo read a write prava po zavreti aplikacie,
        * user by musel cely proces zopakovat.
        *
        * Touto funkciou nastavit prava na persistujuce, takze po zavreti aplikacie, prava nezmiznu.
        * contentResolver.takePersistableUriPermission(documentTreeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        * Taketo uri viem prepouzivat v aplikacii bez znovu vyziaddania prav.
        * */

        val list = queryCustomMedia(documentTreeUri).map(::createVideoThumbnail)
        onResult(list.map(MediaInfo::uri))
      } else
        onResult(emptyList())
    }
    openDocumentTree.launch(null)
  }
```

**queryCustomMedia**
Praca so SAF vracia iny typ `Uri`. S takymto `Uri` nas `ContentResolver` nevie pracovat. My si ho za pomoci `DocumentsContract` prevedieme na `Uri`, s ktorym vieme pracovat.
Nasledne kazdy nacitany child `Uri` v kolekcii, tiez je potrebne prekonvertovat na content `Uri`.
```kotlin
/*Tato funkcia je velmi podobna queryMedia, len tato pouziva ine stlpce. Kedze sa nepouziva priamo ScopedStorage,
  * ale SAF, vracia nam to inu "databazu" aj ked zvolim priamo priecinok Pictures.
  * 1. Musime si vytvorit kolekiu pomocou DocumentsContract.buildChildDocumentsUriUsingTree
  * 2. Querickujeme DocumentsContract.COLUMN_DOCUMENT_ID a DocumentsContract.COLUMN_MIME_TYPE
  * 3. Kazdy subor vo vysledku je dalsi documentTreeUri, cize musime pre kazdy pouzit DocumentsContract.buildChildDocumentsUriUsingTree
  * */
private fun queryCustomMedia(documentTreeUri: Uri): List<MediaInfo> =
    buildList {
      require(DocumentsContract.isTreeUri(documentTreeUri)) { "documentTreeUri must be a DocumentTree uri" }
      val collectionUri = DocumentsContract.buildChildDocumentsUriUsingTree(documentTreeUri, DocumentsContract.getTreeDocumentId(documentTreeUri))
      val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE)
      val cursor = contentResolver.query(
        collectionUri,
        projection,
        null,
        null,
        null
      )

      if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        Log.i("MediaScanner", "Found ${cursor.count} media in $collectionUri")
        do {
          val documentId = cursor.getString(0)
          val documentUri = DocumentsContract.buildChildDocumentsUriUsingTree(collectionUri, documentId)
          add(
            MediaInfo(
              uri = documentUri,
              dateTaken = "0", // DocumentsContract neobsahuje sltpec DATE_TAKEN, len DATE_MODIFIED
              mimeType = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
            )
          )
        } while (cursor.moveToNext())
        cursor.close()
      }
    }
```

### Image picker
[app/src/java/com/parohy/scopedstorage/ui/load/gallery/picker.kt](./picker.kt)
```kotlin
_TODO: Este to nemam_
