# Nacitavanie video suborov
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko aplikacie
[app/src/java/com/parohy/scopedstorage/ui/load/video/private.kt](./private.kt)
Pri privatnom ulozisku je potrebne poznat cestu k suboru. Ak cestu nepozname, stale je mozne dany subor zaujmu najist aj pomocou dokumentacie v [folder](folder/README.md).
Pri privatnom ulozisku nemusime pracovat so ziadnym specialnym frameworkom ani opravneni. Ak by som chcel pracovat s nacitanim privatnym suborom externe mimo aplikacie,
je potrebne pouzit `FileProvider`.

### FileProvider
`FileProvider` je framework, ktory umoznuje zdielat subory s inymi aplikaciami. Tento framework je potrebne nastavit v `AndroidManifest.xml` a vytvorit [`res/xml/provider_paths.xml`](../README.md#FileProvider).
V skratke co `FileProvider` zmeni uri `file://` na `content://`. Tymto sposobom sa zabezpeci, ze subor je zdielatelny s inymi aplikaciami. Zaroven tymto sposobom
vieme menezovat aj prava k tomuto suboru. Ak by sme chceli menit prava k suboru za pomoci file uri `file://`, museli by sme menit na urovni systemu. Tymto ten subor je otvoreny
aj pre ine externe aplikacie nie len pre tu, ktorej chceme subor zdielat.


## Verejne ulozisko

### Movies
[app/src/java/com/parohy/scopedstorage/ui/load/video/videos.kt](./pictures.kt)
Na nacitanie obrazka z PICTURES pouzijeme SAF.
```kotlin
// contract
object OpenVideoFromVideos: ActivityResultContract<Unit, Uri?>() {
  override fun createIntent(context: Context, input: Unit) =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "video/*"
      /*
      * Z mojho testovania, na novsich verziach Androidu, sa neotvoria Videos ale posledne otvorena lokalita
      * Neviem preco to tak funguje, ale je to volovina. Vid dokumentacia DocumentsContract.EXTRA_INITIAL_URI co to na robit.
      * */
      putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}
// usage
private var _onFileResult: ((Uri?) -> Unit)? = null

private val openPictureContract = registerForActivityResult(OpenVideoFromVideos) { uri: Uri? ->
  _onFileResult?.invoke(uri)
  _onFileResult = null
}

fun openVideoSAF(onResult: (Uri?) -> Unit) {
  _onFileResult = onResult
  openPictureContract.launch(Unit)
}
```

### Vlastny priecinok
[app/src/java/com/parohy/scopedstorage/ui/load/video/custom.kt](./custom.kt)
Na nacitanie obrazka pouzijeme SAF.
```kotlin
private var _onFileResult: ((Uri?) -> Unit)? = null

private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

fun openFileSAF(onResult: (Uri?) -> Unit) {
  _onFileResult = onResult
  openFile.launch(arrayOf("video/*"))
}
```

