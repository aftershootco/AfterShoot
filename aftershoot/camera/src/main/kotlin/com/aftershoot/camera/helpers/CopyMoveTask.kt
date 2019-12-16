package com.aftershoot.camera.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.util.Pair
import androidx.documentfile.provider.DocumentFile
import com.aftershoot.camera.R
import com.aftershoot.camera.activities.BaseSimpleActivity
import com.aftershoot.camera.extensions.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.CONFLICT_KEEP_BOTH
import com.simplemobiletools.commons.helpers.CONFLICT_OVERWRITE
import com.simplemobiletools.commons.helpers.CONFLICT_SKIP
import com.simplemobiletools.commons.helpers.getConflictResolution
import com.simplemobiletools.commons.interfaces.CopyMoveListener
import com.simplemobiletools.commons.models.FileDirItem
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.*

class CopyMoveTask(val activity: BaseSimpleActivity, val copyOnly: Boolean = false, val copyMediaOnly: Boolean, val conflictResolutions: LinkedHashMap<String, Int>,
                   listener: CopyMoveListener, val copyHidden: Boolean) : AsyncTask<Pair<ArrayList<FileDirItem>, String>, Void, Boolean>() {
    private val INITIAL_PROGRESS_DELAY = 3000L
    private val PROGRESS_RECHECK_INTERVAL = 500L

    private var mListener: WeakReference<CopyMoveListener>? = null
    private var mTransferredFiles = ArrayList<FileDirItem>()
    private var mDocuments = LinkedHashMap<String, DocumentFile?>()
    private var mFiles = ArrayList<FileDirItem>()
    private var mFileCountToCopy = 0
    private var mDestinationPath = ""

    // progress indication
    private var mNotificationManager: NotificationManager
    private var mNotificationBuilder: NotificationCompat.Builder
    private var mCurrFilename = ""
    private var mCurrentProgress = 0L
    private var mMaxSize = 0
    private var mNotifId = 0
    private var mIsTaskOver = false
    private var mProgressHandler = Handler()

    init {
        mListener = WeakReference(listener)
        mNotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationBuilder = NotificationCompat.Builder(activity)
    }

    override fun doInBackground(vararg params: Pair<ArrayList<FileDirItem>, String>): Boolean? {
        if (params.isEmpty()) {
            return false
        }

        val pair = params[0]
        mFiles = pair.first!!
        mDestinationPath = pair.second!!
        mFileCountToCopy = mFiles.size
        mNotifId = (System.currentTimeMillis() / 1000).toInt()
        mMaxSize = 0
        for (file in mFiles) {
            if (file.size == 0L) {
                file.size = file.getProperSize(copyHidden)
            }
            val newPath = "$mDestinationPath/${file.name}"
            val fileExists = File(newPath).exists()
            if (getConflictResolution(conflictResolutions, newPath) != CONFLICT_SKIP || !fileExists) {
                mMaxSize += (file.size / 1000).toInt()
            }
        }

        mProgressHandler.postDelayed({
            initProgressNotification()
            updateProgress()
        }, INITIAL_PROGRESS_DELAY)

        for (file in mFiles) {
            try {
                val newPath = "$mDestinationPath/${file.name}"
                var newFileDirItem = FileDirItem(newPath, newPath.getFilenameFromPath(), file.isDirectory)
                if (File(newPath).exists()) {
                    val resolution = getConflictResolution(conflictResolutions, newPath)
                    if (resolution == CONFLICT_SKIP) {
                        mFileCountToCopy--
                        continue
                    } else if (resolution == CONFLICT_OVERWRITE) {
                        newFileDirItem.isDirectory = if (File(newPath).exists()) File(newPath).isDirectory else activity.getSomeDocumentFile(newPath)!!.isDirectory
                        activity.deleteFileBg(newFileDirItem, true)
                    } else if (resolution == CONFLICT_KEEP_BOTH) {
                        val newFile = activity.getAlternativeFile(File(newFileDirItem.path))
                        newFileDirItem = FileDirItem(newFile.path, newFile.name, newFile.isDirectory)
                    }
                }

                copy(file, newFileDirItem)
            } catch (e: Exception) {
                activity.toast(e.toString())
                return false
            }
        }

        if (!copyOnly) {
            activity.deleteFilesBg(mTransferredFiles) {}
        }

        return true
    }

    override fun onPostExecute(success: Boolean) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        mProgressHandler.removeCallbacksAndMessages(null)
        mNotificationManager.cancel(mNotifId)
        val listener = mListener?.get() ?: return

        if (success) {
            listener.copySucceeded(copyOnly, mTransferredFiles.size >= mFileCountToCopy, mDestinationPath)
        } else {
            listener.copyFailed()
        }
    }

    private fun initProgressNotification() {
        val channelId = "Copy/Move"
        val title = activity.getString(if (copyOnly) R.string.copying else R.string.moving)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(channelId, title, importance).apply {
                enableLights(false)
                enableVibration(false)
                mNotificationManager.createNotificationChannel(this)
            }
        }

        mNotificationBuilder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_copy)
                .setChannelId(channelId)
    }

    private fun updateProgress() {
        if (mIsTaskOver) {
            mNotificationManager.cancel(mNotifId)
            cancel(true)
            return
        }

        mNotificationBuilder.apply {
            setContentText(mCurrFilename)
            setProgress(mMaxSize, (mCurrentProgress / 1000).toInt(), false)
            mNotificationManager.notify(mNotifId, build())
        }

        mProgressHandler.removeCallbacksAndMessages(null)
        mProgressHandler.postDelayed({
            updateProgress()

            if (mCurrentProgress / 1000 > mMaxSize) {
                mIsTaskOver = true
            }
        }, PROGRESS_RECHECK_INTERVAL)
    }

    private fun copy(source: FileDirItem, destination: FileDirItem) {
        if (source.isDirectory) {
            copyDirectory(source, destination.path)
        } else {
            copyFile(source, destination)
        }
    }

    private fun copyDirectory(source: FileDirItem, destinationPath: String) {
        if (!activity.createDirectorySync(destinationPath)) {
            val error = String.format(activity.getString(R.string.could_not_create_folder), destinationPath)
            activity.showErrorToast(error)
            return
        }

        val children = File(source.path).list()
        for (child in children) {
            val newPath = "$destinationPath/$child"
            if (File(newPath).exists()) {
                continue
            }

            val oldFile = File(source.path, child)
            val oldFileDirItem = oldFile.toFileDirItem()
            val newFileDirItem = FileDirItem(newPath, newPath.getFilenameFromPath(), oldFile.isDirectory)
            copy(oldFileDirItem, newFileDirItem)
        }
        mTransferredFiles.add(source)
    }

    private fun copyFile(source: FileDirItem, destination: FileDirItem) {
        if (copyMediaOnly && !source.path.isMediaFile()) {
            mCurrentProgress += source.size
            return
        }

        val directory = destination.getParentPath()
        if (!activity.createDirectorySync(directory)) {
            val error = String.format(activity.getString(R.string.could_not_create_folder), directory)
            activity.showErrorToast(error)
            mCurrentProgress += source.size
            return
        }

        mCurrFilename = source.name
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            if (!mDocuments.containsKey(directory) && activity.needsStupidWritePermissions(destination.path)) {
                mDocuments[directory] = activity.getDocumentFile(directory)
            }
            out = activity.getFileOutputStreamSync(destination.path, source.path.getMimeType(), mDocuments[directory])
            inputStream = activity.getFileInputStreamSync(source.path)

            var copiedSize = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = inputStream.read(buffer)
            while (bytes >= 0) {
                out!!.write(buffer, 0, bytes)
                copiedSize += bytes
                mCurrentProgress += bytes
                bytes = inputStream.read(buffer)
            }

            out?.flush()

            if (source.size == copiedSize && File(destination.path).exists()) {
                mTransferredFiles.add(source)
                if (activity.baseConfig.keepLastModified) {
                    copyOldLastModified(source.path, destination.path)
                    File(destination.path).setLastModified(File(source.path).lastModified())
                }

                if (!copyOnly) {
                    activity.deleteFromMediaStore(source.path)
                }
            }
        } catch (e: Exception) {
            activity.showErrorToast(e)
        } finally {
            inputStream?.close()
            out?.close()
        }
    }

    private fun copyOldLastModified(sourcePath: String, destinationPath: String) {
        val projection = arrayOf(
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_MODIFIED)
        val uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        var selectionArgs = arrayOf(sourcePath)
        val cursor = activity.applicationContext.contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            if (cursor.moveToFirst()) {
                val dateTaken = cursor.getLongValue(MediaStore.Images.Media.DATE_TAKEN)
                val dateModified = cursor.getIntValue(MediaStore.Images.Media.DATE_MODIFIED)

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATE_TAKEN, dateTaken)
                    put(MediaStore.Images.Media.DATE_MODIFIED, dateModified)
                }

                selectionArgs = arrayOf(destinationPath)
                activity.applicationContext.contentResolver.update(uri, values, selection, selectionArgs)
            }
        }
    }
}