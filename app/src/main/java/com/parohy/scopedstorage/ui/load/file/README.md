# Nacitavanie suboru
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko aplikacie
[app/src/java/com/parohy/scopedstorage/ui/load/file/private.kt](./private.kt)
Pri privatnom ulozisku je potrebne poznat cestu k suboru. Ak cestu nepozname, stale je mozne dany subor zaujmu najist aj pomocou dokumentacie v [folder](folder/README.md).
Pri privatnom ulozisku nemusime pracovat so ziadnym specialnym frameworkom ani opravneni. Ak by som chcel pracovat s nacitanim privatnym suborom externe mimo aplikacie,
je potrebne pouzit `FileProvider`.

### FileProvider
`FileProvider` je framework, ktory umoznuje zdielat subory s inymi aplikaciami. Tento framework je potrebne nastavit v `AndroidManifest.xml` a vytvorit [`res/xml/provider_paths.xml`](../README.md#FileProvider).
V skratke co `FileProvider` zmeni uri `file://` na `content://`. Tymto sposobom sa zabezpeci, ze subor je zdielatelny s inymi aplikaciami. Zaroven tymto sposobom
vieme menezovat aj prava k tomuto suboru. Ak by sme chceli menit prava k suboru za pomoci file uri `file://`, museli by sme menit na urovni systemu. Tymto ten subor je otvoreny
aj pre ine externe aplikacie nie len pre tu, ktorej chceme subor zdielat.


## Verejne ulozisko
[app/src/java/com/parohy/scopedstorage/ui/load/file/public.kt](./public.kt)
Na nacitanie suboru pouzijeme SAF.
```kotlin
private var _onFileResult: ((Uri?) -> Unit)? = null

private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

fun openFileSAF(onResult: (Uri?) -> Unit) {
  _onFileResult = onResult
  openFile.launch(arrayOf("*/*"))
}
```