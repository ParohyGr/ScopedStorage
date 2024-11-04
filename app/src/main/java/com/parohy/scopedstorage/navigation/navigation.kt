package com.parohy.scopedstorage.navigation

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.WhichType
import com.parohy.scopedstorage.ui.home.Home
import com.parohy.scopedstorage.ui.load.LoadWhat
import com.parohy.scopedstorage.ui.load.audio.*
import com.parohy.scopedstorage.ui.load.document.*
import com.parohy.scopedstorage.ui.load.file.LoadFilePrivate
import com.parohy.scopedstorage.ui.load.file.LoadFilePublic
import com.parohy.scopedstorage.ui.load.folder.LoadFolderPrivate
import com.parohy.scopedstorage.ui.load.folder.LoadFolderPublic
import com.parohy.scopedstorage.ui.load.gallery.*
import com.parohy.scopedstorage.ui.load.picture.*
import com.parohy.scopedstorage.ui.load.video.*
import com.parohy.scopedstorage.ui.move.*
import com.parohy.scopedstorage.ui.save.SaveWhat
import com.parohy.scopedstorage.ui.save.audio.*
import com.parohy.scopedstorage.ui.save.document.*
import com.parohy.scopedstorage.ui.save.file.SaveFilePrivate
import com.parohy.scopedstorage.ui.save.file.SaveFilePublic
import com.parohy.scopedstorage.ui.save.picture.*
import com.parohy.scopedstorage.ui.save.video.*
import kotlinx.parcelize.Parcelize
import shared.NavController
import shared.navigate

interface Bottom

sealed interface Screen: Parcelable {
  @Parcelize
  data object Home: Screen
  /*region Load File*/
  @Parcelize
  open class LoadFile: Screen {

    /*region Picture*/
    open class Picture: LoadFile() {
      @Parcelize
      data object PrivateStorage: Picture()
      open class PublicStorage: Picture() {
        @Parcelize
        data object Pictures: PublicStorage()
        @Parcelize
        data object Downloads: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Video*/
    open class Video: LoadFile() {
      @Parcelize
      data object PrivateStorage: Video()
      open class PublicStorage: Video() {
        @Parcelize
        data object Videos: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Audio*/
    open class Audio: LoadFile() {
      @Parcelize
      data object PrivateStorage: Audio()
      open class PublicStorage: Audio() {
        @Parcelize
        data object Music: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Document*/
    open class Document: LoadFile() {
      @Parcelize
      data object PrivateStorage: Document()
      open class PublicStorage: Document() {
        @Parcelize
        data object Documents: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Gallery*/
    open class Gallery: LoadFile() {
      @Parcelize
      data object PrivateStorage: Gallery()
      open class PublicStorage: Gallery() {
        @Parcelize
        data object Pictures: PublicStorage()
        @Parcelize
        data object Multimedia: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region File*/
    open class File: LoadFile() {
      @Parcelize
      data object PrivateStorage: File()
      @Parcelize
      data object PublicStorage: File()
    }
    /*endregion*/

    /*region Folder*/
    open class Folder: LoadFile() {
      @Parcelize
      data object PrivateStorage: Folder()
      @Parcelize
      data object PublicStorage: Folder()
    }
    /*endregion*/

  }
  /*endregion*/

  /*region Save File*/
  @Parcelize
  open class SaveFile: Screen {

    /*region Picture*/
    open class Picture: SaveFile() {
      @Parcelize
      data object PrivateStorage: Picture()
      open class PublicStorage: Picture() {
        @Parcelize
        data object Pictures: PublicStorage()
        @Parcelize
        data object Downloads: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Video*/
    open class Video: SaveFile() {
      @Parcelize
      data object PrivateStorage: Video()
      open class PublicStorage: Video() {
        @Parcelize
        data object Videos: PublicStorage()
        @Parcelize
        data object Downloads: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Audio*/
    open class Audio: SaveFile() {
      @Parcelize
      data object PrivateStorage: Audio()
      open class PublicStorage: Audio() {
        @Parcelize
        data object Music: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Document*/
    open class Document: SaveFile() {
      @Parcelize
      data object PrivateStorage: Document()
      open class PublicStorage: Document() {
        @Parcelize
        data object Documents: PublicStorage()
        @Parcelize
        data object Downloads: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region File*/
    open class File: SaveFile() {
      @Parcelize
      data object PrivateStorage: File()
      @Parcelize
      data object PublicStorage: File()
    }
    /*endregion*/
  }
  /*endregion*/

  /*region Delete File*/
  @Parcelize
  open class DeleteFile: Screen {

    /*region Picture*/
    open class Picture: DeleteFile() {
      @Parcelize
      data object PrivateStorage: Picture()
      open class PublicStorage: Picture() {
        @Parcelize
        data object Pictures: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Video*/
    open class Video: DeleteFile() {
      @Parcelize
      data object PrivateStorage: Video()
      open class PublicStorage: Video() {
        @Parcelize
        data object Videos: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Audio*/
    open class Audio: DeleteFile() {
      @Parcelize
      data object PrivateStorage: Audio()
      open class PublicStorage: Audio() {
        @Parcelize
        data object Music: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Document*/
    open class Document: DeleteFile() {
      @Parcelize
      data object PrivateStorage: Document()
      open class PublicStorage: Document() {
        @Parcelize
        data object Documents: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region Multiselect*/
    open class Multiselect: DeleteFile() {
      open class PrivateStorage: Multiselect() {
        @Parcelize
        data object Pictures: PrivateStorage()
        @Parcelize
        data object Multimedia: PrivateStorage()
      }
      open class PublicStorage: Multiselect() {
        @Parcelize
        data object Pictures: PublicStorage()
        @Parcelize
        data object Multimedia: PublicStorage()
        @Parcelize
        data object CustomDirectory: PublicStorage()
      }
    }
    /*endregion*/

    /*region File*/
    open class File: DeleteFile() {
      @Parcelize
      data object PrivateStorage: File()
      @Parcelize
      data object PublicStorage: File()
    }
    /*endregion*/
  }
  /*endregion*/

  /*region Move File*/
  @Parcelize
  open class MoveFile: Screen {

    /*region File*/
    @Parcelize
    data object PrivateStorage: MoveFile()
    @Parcelize
    data object PublicStorage: MoveFile()
    @Parcelize
    data object FromPrivateToPublic: MoveFile()
    @Parcelize
    data object FromPublicToPrivate: MoveFile()
    /*endregion*/

  }
  /*endregion*/
}

@Composable
fun NavController.NavigationRouter(destination: Screen, context: Context) =
  when(destination) {
    is Screen.Home -> Home(
      goToLoad = { navigate(Screen.LoadFile()) },
      goToSave = { navigate(Screen.SaveFile()) },
      goToDelete = { navigate(Screen.DeleteFile()) },
      goToMove = { navigate(Screen.MoveFile()) }
    )
    is Screen.LoadFile -> LoadFileRouter(destination)
    is Screen.SaveFile -> SaveFileRouter(destination)
    is Screen.DeleteFile -> DeleteFileRouter(destination)
    is Screen.MoveFile -> MoveFileRouter(destination)
  }

@Composable
private fun NavController.LoadFileRouter(destination: Screen.LoadFile) =
  when (destination) {
    is Screen.LoadFile.Picture ->
      when (destination) {
        is Screen.LoadFile.Picture.PrivateStorage -> LoadPicturePrivate()
        is Screen.LoadFile.Picture.PublicStorage.Pictures -> LoadFromPublicPictures()
        is Screen.LoadFile.Picture.PublicStorage.Downloads -> LoadFromPublicDownloads()
        is Screen.LoadFile.Picture.PublicStorage.CustomDirectory -> LoadPicturePublicCustom()
        is Screen.LoadFile.Picture.PublicStorage ->
          WhichPicturePublic(
            goToPictures = { navigate(Screen.LoadFile.Picture.PublicStorage.Pictures) },
            goToDownloads = { navigate(Screen.LoadFile.Picture.PublicStorage.Downloads) },
            goToCustom = { navigate(Screen.LoadFile.Picture.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.LoadFile.Picture.PrivateStorage) },
            goToPublic = { navigate(Screen.LoadFile.Picture.PublicStorage()) }
          )
      }
    is Screen.LoadFile.Video ->
      when (destination) {
        is Screen.LoadFile.Video.PrivateStorage -> LoadVideoPrivate()
        is Screen.LoadFile.Video.PublicStorage.Videos -> LoadFromPublicVideos()
        is Screen.LoadFile.Video.PublicStorage.CustomDirectory -> LoadVideoFromPublicCustom()
        is Screen.LoadFile.Video.PublicStorage ->
          WhichVideoPublic(
            goToVideos = { navigate(Screen.LoadFile.Video.PublicStorage.Videos) },
            goToDownloads = { /*navigate(Screen.LoadFile.Video.PublicStorage.Downloads)*/ },
            goToCustom = { navigate(Screen.LoadFile.Video.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.LoadFile.Video.PrivateStorage) },
            goToPublic = { navigate(Screen.LoadFile.Video.PublicStorage()) }
          )
      }
    is Screen.LoadFile.Audio ->
      when (destination) {
        is Screen.LoadFile.Audio.PrivateStorage -> LoadAudioPrivate()
        is Screen.LoadFile.Audio.PublicStorage.Music -> LoadFromPublicMusic()
        is Screen.LoadFile.Audio.PublicStorage.CustomDirectory -> LoadMusicFromCustomPublic()
        is Screen.LoadFile.Audio.PublicStorage ->
          WhichAudioPublic(
            goToAudio = { navigate(Screen.LoadFile.Audio.PublicStorage.Music) },
            goToCustom = { navigate(Screen.LoadFile.Audio.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.LoadFile.Audio.PrivateStorage) },
            goToPublic = { navigate(Screen.LoadFile.Audio.PublicStorage()) }
          )
      }
    is Screen.LoadFile.Document ->
      when (destination) {
        is Screen.LoadFile.Document.PrivateStorage -> LoadDocumentPrivate()
        is Screen.LoadFile.Document.PublicStorage.Documents -> LoadFromPublicDocuments()
        is Screen.LoadFile.Document.PublicStorage.CustomDirectory -> LoadDocumentFromPublicCustom()
        is Screen.LoadFile.Document.PublicStorage ->
          WhichDocumentPublic(
            goToDocuments = { navigate(Screen.LoadFile.Document.PublicStorage.Documents) },
            goToDownloads = { /*navigate(Screen.LoadFile.Document.PublicStorage.Downloads) */},
            goToCustom = { navigate(Screen.LoadFile.Document.PublicStorage.CustomDirectory) },
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.LoadFile.Document.PrivateStorage) },
            goToPublic = { navigate(Screen.LoadFile.Document.PublicStorage()) }
          )
      }
    is Screen.LoadFile.Gallery ->
      when (destination) {
        is Screen.LoadFile.Gallery.PrivateStorage -> LoadGalleryPrivate()
        is Screen.LoadFile.Gallery.PublicStorage.Pictures -> LoadGalleryFromPublicPictures()
        is Screen.LoadFile.Gallery.PublicStorage.Multimedia -> LoadGalleryFromMultimediaPublic()
        is Screen.LoadFile.Gallery.PublicStorage.CustomDirectory -> LoadGalleryFromCustomPublic()
        is Screen.LoadFile.Gallery.PublicStorage ->
          WhichGalleryPublic(
            goToPictures = { navigate(Screen.LoadFile.Gallery.PublicStorage.Pictures) },
            goToMultimedia = { navigate(Screen.LoadFile.Gallery.PublicStorage.Multimedia) },
            goToCustom = { navigate(Screen.LoadFile.Gallery.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.LoadFile.Gallery.PrivateStorage) },
            goToPublic = { navigate(Screen.LoadFile.Gallery.PublicStorage()) }
          )
      }
    is Screen.LoadFile.File ->
      when (destination) {
        is Screen.LoadFile.File.PrivateStorage -> LoadFilePrivate()
        is Screen.LoadFile.File.PublicStorage -> LoadFilePublic()
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.LoadFile.File.PrivateStorage) },
            goToPublic = { navigate(Screen.LoadFile.File.PublicStorage) }
          )
      }
    is Screen.LoadFile.Folder ->
      when (destination) {
        is Screen.LoadFile.Folder.PrivateStorage -> LoadFolderPrivate()
        is Screen.LoadFile.Folder.PublicStorage -> LoadFolderPublic()
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.LoadFile.Folder.PrivateStorage) },
            goToPublic = { navigate(Screen.LoadFile.Folder.PublicStorage) }
          )
      }
    else -> LoadWhat(
      goToPicture = { navigate(Screen.LoadFile.Picture()) },
      goToVideo = { navigate(Screen.LoadFile.Video()) },
      goToAudio = { navigate(Screen.LoadFile.Audio()) },
      goToDocument = { navigate(Screen.LoadFile.Document()) },
      goToGallery = { navigate(Screen.LoadFile.Gallery()) },
      goToFile = { navigate(Screen.LoadFile.File()) },
      goToCustomDirectory = { navigate(Screen.LoadFile.Folder()) }
    )
  }

@Composable
private fun NavController.SaveFileRouter(destination: Screen.SaveFile) =
  when (destination) {
    is Screen.SaveFile.Picture ->
      when (destination) {
        is Screen.SaveFile.Picture.PrivateStorage -> SavePicturePrivate()
        is Screen.SaveFile.Picture.PublicStorage.Pictures -> SavePicturePublicPictures()
        is Screen.SaveFile.Picture.PublicStorage.Downloads -> SavePicturePublicDownloads()
        is Screen.SaveFile.Picture.PublicStorage.CustomDirectory -> SavePicturePublicCustom()
        is Screen.SaveFile.Picture.PublicStorage ->
          WhichPicturePublic(
            goToPictures = { navigate(Screen.SaveFile.Picture.PublicStorage.Pictures) },
            goToDownloads = { navigate(Screen.SaveFile.Picture.PublicStorage.Downloads) },
            goToCustom = { navigate(Screen.SaveFile.Picture.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.SaveFile.Picture.PrivateStorage) },
            goToPublic = { navigate(Screen.SaveFile.Picture.PublicStorage()) }
          )
      }
    is Screen.SaveFile.Video ->
      when (destination) {
        is Screen.SaveFile.Video.PrivateStorage -> SaveVideoPrivate()
        is Screen.SaveFile.Video.PublicStorage.Videos -> SaveVideoPublicVideos()
        is Screen.SaveFile.Video.PublicStorage.Downloads -> SaveVideoPublicDownloads()
        is Screen.SaveFile.Video.PublicStorage.CustomDirectory -> SaveVideoPublicCustom()
        is Screen.SaveFile.Video.PublicStorage ->
          WhichVideoPublic(
            goToVideos = { navigate(Screen.SaveFile.Video.PublicStorage.Videos) },
            goToDownloads = { navigate(Screen.SaveFile.Video.PublicStorage.Downloads) },
            goToCustom = { navigate(Screen.SaveFile.Video.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.SaveFile.Video.PrivateStorage) },
            goToPublic = { navigate(Screen.SaveFile.Video.PublicStorage()) }
          )
      }
    is Screen.SaveFile.Audio ->
      when (destination) {
        is Screen.SaveFile.Audio.PrivateStorage -> SaveAudioPrivate()
        is Screen.SaveFile.Audio.PublicStorage.Music -> SaveAudioPublicMusic()
        is Screen.SaveFile.Audio.PublicStorage.CustomDirectory -> SaveAudioPublicCustom()
        is Screen.SaveFile.Audio.PublicStorage ->
          WhichAudioPublic(
            goToAudio = { navigate(Screen.SaveFile.Audio.PublicStorage.Music) },
            goToCustom = { navigate(Screen.SaveFile.Audio.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.SaveFile.Audio.PrivateStorage) },
            goToPublic = { navigate(Screen.SaveFile.Audio.PublicStorage()) }
          )
      }
    is Screen.SaveFile.Document ->
      when (destination) {
        is Screen.SaveFile.Document.PrivateStorage -> SaveDocumentPrivate()
        is Screen.SaveFile.Document.PublicStorage.Documents -> SaveDocumentPublicDocuments()
        is Screen.SaveFile.Document.PublicStorage.Downloads -> SaveDocumentPublicDownloads()
        is Screen.SaveFile.Document.PublicStorage.CustomDirectory -> SaveDocumentPublicCustom()
        is Screen.SaveFile.Document.PublicStorage ->
          WhichDocumentPublic(
            goToDocuments = { navigate(Screen.SaveFile.Document.PublicStorage.Documents) },
            goToDownloads = { navigate(Screen.SaveFile.Document.PublicStorage.Downloads) },
            goToCustom = { navigate(Screen.SaveFile.Document.PublicStorage.CustomDirectory) }
          )
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.SaveFile.Document.PrivateStorage) },
            goToPublic = { navigate(Screen.SaveFile.Document.PublicStorage()) }
          )
      }
    is Screen.SaveFile.File ->
      when (destination) {
        is Screen.SaveFile.File.PrivateStorage -> SaveFilePrivate()
        is Screen.SaveFile.File.PublicStorage -> SaveFilePublic()
        else ->
          WhichType(
            what = destination,
            goToPrivate = { navigate(Screen.SaveFile.File.PrivateStorage) },
            goToPublic = { navigate(Screen.SaveFile.File.PublicStorage) }
          )
      }
    else -> SaveWhat(
      goToPicture = { navigate(Screen.SaveFile.Picture()) },
      goToVideo = { navigate(Screen.SaveFile.Video()) },
      goToAudio = { navigate(Screen.SaveFile.Audio()) },
      goToDocument = { navigate(Screen.SaveFile.Document()) },
      goToFile = { navigate(Screen.SaveFile.File()) },
    )
  }

@Composable
private fun NavController.DeleteFileRouter(destination: Screen.DeleteFile) =
  when (destination) {
    is Screen.DeleteFile.Picture -> {}
    is Screen.DeleteFile.Video -> {}
    is Screen.DeleteFile.Audio -> {}
    is Screen.DeleteFile.Document -> {}
    is Screen.DeleteFile.Multiselect -> {}
    is Screen.DeleteFile.File -> {}
    else -> {}
  }

@Composable
private fun NavController.MoveFileRouter(destination: Screen.MoveFile) =
  when (destination) {
    is Screen.MoveFile.PrivateStorage -> MoveFilePrivate()
    is Screen.MoveFile.PublicStorage -> MoveFilePublic()
    is Screen.MoveFile.FromPrivateToPublic -> MoveFilePrivatePublic()
    is Screen.MoveFile.FromPublicToPrivate -> MoveFilePublicPrivate()
    else -> MoveWhichType(
      what = destination,
      goToPrivate = { navigate(Screen.MoveFile.PrivateStorage) },
      goToPublic = { navigate(Screen.MoveFile.PublicStorage) },
      goToPrivateToPublic = { navigate(Screen.MoveFile.FromPrivateToPublic) },
      goToPublicToPrivate = { navigate(Screen.MoveFile.FromPublicToPrivate) }
    )
  }