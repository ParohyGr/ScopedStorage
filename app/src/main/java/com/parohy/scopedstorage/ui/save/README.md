# Ukladanie suborov
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko
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
Ak chceme od uzivatela, aby si zvolil miesto ulozenia, pouzijeme opat [SAF](https://developer.android.com/guide/topics/providers/document-provider). Takto vytvoreny subor uz ma udele prava na zapis, cize nemusime absolutne riesit zaidne prava.

Ak chceme ukladat subor na konkretne miesto, napriklad ak by sme cheli ulozit obrazok do `Pictures` pouzijeme na to `ContentResolver` s `MediaStore` ([co to je](#MediaStore)).
Preco by som mal pouzivat `ContentResolver` s `MediaStore`:
**MediaStore**
- praca s media subormi: Obrazky, Video, Audio a Dokumenty
- integorvany ScopedStorage - Android 13+ **NETREBA** riesit `WRITE_EXTERNAL_STORAGE`
- kompatabilita so starsou verziou Androidu
- **automaticke skenovanie medii**
  **SAF**
- vhodne na pracu so subormi, ktore neukladame na vseobecne miesta
- kompatabilita so starsou verziou Androidu
- moznost pristupu k suborom na vzdialenych uloziskach
- persistencia read/write prav uri suboru

Subor si vytvorime pomocou `ContentResolver.insert`, ktore nam vrati Uri na novy subor. Do tohto Uri mozme priamo vpisovat data.

### MediaStore
[docs](https://developer.android.com/training/data-storage/shared/media)
Ulozisko Androidu si predstavte ako SQL databazu. Kazdy subor je zaznam v tabulke. Tato tabulka obsahuje rozne stlpce, ktore reprezentuju informacie o subore. Napriklad `MediaStore.Images.Media.DISPLAY_NAME` reprezentuje meno suboru.
Kazda verzia Androidu ma definovane nazvy tychto stlpcov inac, preto je potrebne pouzivat tieto konstanty. Tieto konstanty su odlisne aj podla `mimeType` alebo skor o aky typ media mam zaujem. Zaroven, `MediaStore` uz obsahuje nadstavbu ScopedStorage,
cize ak pouzijem `MediaStore`, uz sa riadim podla opravnenych postupov Androidu.

### Android 9 a nizsie
Pouzijeme `ContentResolver` s `MediaStore`. Specificky use case najdes v jednotlivych dokumnentaciach podla typu suboru.
Ak chces ukladat do priecinka **DOWNLOADS** alebo **DOCUMENTS**, musis si pytat `WRITE_EXTERNAL_STORAGE` len pre Android 9 a nizsie
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

### Android 10-12
Pouzijeme `ContentResolver` s `MediaStore`. Specificky use case najdes v jednotlivych dokumnentaciach podla typu suboru.
Tu sa to trosku komplikuje ak to pride na `WRITE_EXTERNAL_STORAGE`.

### Android 13 a vyssie
Pouzijeme `ContentResolver` s `MediaStore`. Specificky use case najdes v jednotlivych dokumnentaciach podla typu suboru.
Ak chces ukladat do **media** priecinkov, musis si pytat `WRITE_EXTERNAL_STORAGE` len pre Android 12 a nizsie
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
  block()
else
  if (isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    block()
  else
    requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
      block()
    }
```

# Rozdelenie podla typu mimeType

### Audio sobory
<sub>**Poznamka:** TODO</sub>
Dokumentacia je dostupna v [audio](audio/README.md)

### Dokumenty
Dokumentacia je dostupna v [document](document/README.md)

### Obrazky
Dokumentacia je dostupna v [picture](picture/README.md)

### Video
Dokumentacia je dostupna v [video](video/README.md)

### File
Dokumentacia je dostupna v [file](file/README.md)