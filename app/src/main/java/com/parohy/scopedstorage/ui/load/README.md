# Nacitavanie suborov
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko
Pri privatnom ulozisku je potrebne poznat cestu k suboru. Ak cestu nepozname, stale je mozne dany subor zaujmu najist aj pomocou dokumentacie v [folder](folder/README.md).
Pri privatnom ulozisku nemusime pracovat so ziadnym specialnym frameworkom ani opravneni. Ak by som chcel pracovat s nacitanim privatnym suborom externe mimo aplikacie,
je potrebne pouzit `FileProvider`.

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
### Android 9 a nizsie
### Android 10-12
### Android 13 a vyssie
![Diagram](ss_permissions_diagram.png)

## Rozdelenie podla typu mimeType

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

### Priecinok
Dokumentacia je dostupna v [folder](folder/README.md)

### Galeria
Dokumentacia je dostupna v [gallery](gallery/README.md)
