# Nacitavanie dokumentov
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko aplikacie
[app/src/java/com/parohy/scopedstorage/ui/load/document/private.kt](./private.kt)
Pri privatnom ulozisku je potrebne poznat cestu k suboru. Ak cestu nepozname, stale je mozne dany subor zaujmu najist aj pomocou dokumentacie v [folder](folder/README.md).
Pri privatnom ulozisku nemusime pracovat so ziadnym specialnym frameworkom ani opravneni. Ak by som chcel pracovat s nacitanim privatnym suborom externe mimo aplikacie,
je potrebne pouzit `FileProvider`.

### FileProvider
`FileProvider` je framework, ktory umoznuje zdielat subory s inymi aplikaciami. Tento framework je potrebne nastavit v `AndroidManifest.xml` a vytvorit [`res/xml/provider_paths.xml`](../README.md#FileProvider).
V skratke co `FileProvider` zmeni uri `file://` na `content://`. Tymto sposobom sa zabezpeci, ze subor je zdielatelny s inymi aplikaciami. Zaroven tymto sposobom
vieme menezovat aj prava k tomuto suboru. Ak by sme chceli menit prava k suboru za pomoci file uri `file://`, museli by sme menit na urovni systemu. Tymto ten subor je otvoreny
aj pre ine externe aplikacie nie len pre tu, ktorej chceme subor zdielat.


## Verejne ulozisko

### Documents
[app/src/java/com/parohy/scopedstorage/ui/load/document/documents.kt](./documents.kt)
Na nacitanie dokumentu z DOCUMENTS pouzijeme SAF.

```kotlin
// contract
object OpenPdfDocumentFromDocuments: ActivityResultContract<Unit, Uri?>() {
  override fun createIntent(context: Context, input: Unit) =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/pdf"
      /*
      * Z mojho testovania, na novsich verziach Androidu, sa neotvoria Documents ale posledne otvorena lokalita
      * Neviem preco to tak funguje, ale je to volovina. Vid dokumentacia DocumentsContract.EXTRA_INITIAL_URI co to na robit.
      * */
      putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Files.getContentUri("external"))
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}

// usage
private var _onFileResult: ((Uri?) -> Unit)? = null

private val openDocumentContract = registerForActivityResult(OpenPdfDocumentFromDocuments) { uri: Uri? ->
  _onFileResult?.invoke(uri)
  _onFileResult = null
}

fun openDocumentSAF(onResult: (Uri?) -> Unit) {
  _onFileResult = onResult
  openDocumentContract.launch(Unit)
}
```

### Downloads
[app/src/java/com/parohy/scopedstorage/ui/load/document/downloads.kt](./downloads.kt)
Na nacitanie dokumentu z DOWNLOADS pouzijeme SAF.
```kotlin
// contract
object OpenDocumentFromDownloads: ActivityResultContract<Unit, Uri?>() {
  override fun createIntent(context: Context, input: Unit) =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/pdf"
      /*
      * Z mojho testovania, na novsich verziach Androidu, sa neotvoria Pictures ale posledne otvorena lokalita
      * Neviem preco to tak funguje, ale je to volovina. Vid dokumentacia DocumentsContract.EXTRA_INITIAL_URI co to na robit.
      * */
      val startFrom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        MediaStore.Downloads.EXTERNAL_CONTENT_URI
      else
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toUri()

      putExtra(DocumentsContract.EXTRA_INITIAL_URI, startFrom)
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}
// usage
private var _onFileResult: ((Uri?) -> Unit)? = null

private val openDocumentContract = registerForActivityResult(OpenDocumentFromDownloads) { uri: Uri? ->
  _onFileResult?.invoke(uri)
  _onFileResult = null
}

fun openDocumentSAF(onResult: (Uri?) -> Unit) {
  _onFileResult = onResult
  openDocumentContract.launch(Unit)
}
```

### Vlastny priecinok
[app/src/java/com/parohy/scopedstorage/ui/load/document/custom.kt](./custom.kt)
Na nacitanie dokumentu pouzijeme SAF.
```kotlin
private var _onFileResult: ((Uri?) -> Unit)? = null

private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

fun openFileSAF(onResult: (Uri?) -> Unit) {
  _onFileResult = onResult
  openFile.launch(arrayOf("application/pdf"))
}
```
