# Nacitavanie obsahu priecinka
### Disclaimer
Tieto priklady maju pomoct implementovat zakladny "happy path" pre ukladanie suborov do privatneho ale aj do externeho uloziska. Vzdy je dolezite si uvedomit, ze v praci je potrebny error handling a dokladnejsi edge case handling. Tieto priklady su len na ukazku a nie na pouzitie v produkcnom kode.

## Privatne ulozisko aplikacie
_TODO: Este to nemam_

## Verejne ulozisko
```kotlin
private var _onFileResult: ((Uri?) -> Unit)? = null

fun loadFilesFromCustomDirectory(onResult: (List<FileInfo>) -> Unit) {
    _onFileResult = { documentTreeUri ->
      if (documentTreeUri != null) {
        /*Ak by som chcel si pamatat zvoleny priecinok, URI by stratilo read a write prava po zavreti aplikacie,
        * user by musel cely proces zopakovat.
        *
        * Touto funkciou nastavit prava na persistujuce, takze po zavreti aplikacie, prava nezmiznu.
        * contentResolver.takePersistableUriPermission(documentTreeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        * Taketo uri viem prepouzivat v aplikacii bez znovu vyziaddania prav.
        * */

        val list = queryFiles(documentTreeUri)
        onResult(list)
      } else
        onResult(emptyList())
    }
    openDocumentTree.launch(null)
  }

  data class FileInfo(val uri: Uri, val name: String, val mimeType: String)
  private fun queryFiles(documentTreeUri: Uri): List<FileInfo> =
    buildList {
      require(DocumentsContract.isTreeUri(documentTreeUri)) { "documentTreeUri must be a DocumentTree uri" }
      val collectionUri = DocumentsContract.buildChildDocumentsUriUsingTree(documentTreeUri, DocumentsContract.getTreeDocumentId(documentTreeUri))
      val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
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
            FileInfo(
              uri = documentUri,
              name = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)),
              mimeType = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
            )
          )
        } while (cursor.moveToNext())
        cursor.close()
      }
    }
```
