# Ukladanie suboru
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko
[app/src/java/com/parohy/scopedstorage/ui/save/file/private.kt](./private.kt)
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
Ak chceme vkladat do vlastneho priecinka, mozme pouzit rovnaky postup ako pri Documents a Downloads pre vsetky otatne media [napr pictures](../picture/README.md#vlastny_priecinok).
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
  private val createFile = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
    _onFileCreated?.invoke(uri)
    _onFileCreated = null
  }

  fun createCustomFile(onResult: (Uri?) -> Unit) {
    _onFileCreated = onResult
    createFile.launch("Custom_SS_${System.currentTimeMillis()}")
  }
```
