package me.jiahuan.alfred.kmc

import sun.awt.image.MultiResolutionCachedImage
import java.awt.Color
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.locks.ReentrantLock
import javax.imageio.ImageIO

System.setProperty("java.awt.headless", "false")

val clipImage: BufferedImage? = getClipboardImage()
if (clipImage != null) {
    val byteArrayOutputStream = ByteArrayOutputStream()
    ImageIO.write(clipImage, "png", byteArrayOutputStream)
    byteArrayOutputStream.close()
    if (uploadImage(byteArrayOutputStream.toByteArray())) {
        Runtime.getRuntime().exec("./show.sh success")
    } else {
        Runtime.getRuntime().exec("./show.sh error")
    }
} else {
    Runtime.getRuntime().exec("./show.sh error")
}


fun getClipboardImage(): BufferedImage? {
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
        println("剪切板中是文件类型")
        val fileList: List<File>? = clipboard.getData(DataFlavor.javaFileListFlavor) as List<File>?
        if (fileList != null && fileList.isNotEmpty()) {
            return ImageIO.read(fileList[0])
        }
    } else if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
        println("剪切板中是图片类型")
        val image = clipboard.getData(DataFlavor.imageFlavor)
        if (image is BufferedImage) {
            return image
        }
    }
    return null
}

fun setTextContentToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(text), null)
}

fun uploadImage(byteArray: ByteArray): Boolean {
    var retValue = true
    val endString = "\r\n"
    val twoHyphen = "--"
    val boundary = "----WebKitFormBoundary123456789"
    val url = URL("https://sm.ms/api/upload")
    val httpConnection = url.openConnection() as HttpURLConnection

    httpConnection.setDoInput(true);
    httpConnection.setDoOutput(true);
    httpConnection.setUseCaches(false);

    httpConnection.requestMethod = "POST"
    httpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

    var dataOutputStream: DataOutputStream? = null
    var inputStream: InputStream? = null
    try {

        dataOutputStream = DataOutputStream(httpConnection.outputStream)

        dataOutputStream.writeBytes(twoHyphen + boundary + endString)
        dataOutputStream.writeBytes("Content-Disposition:form-data; name=\"smfile\"; filename=\"upload.png\"${endString}")
        dataOutputStream.writeBytes(endString)

        dataOutputStream.write(byteArray, 0, byteArray.size);

        dataOutputStream.writeBytes(endString);
        dataOutputStream.writeBytes(twoHyphen + boundary + twoHyphen + endString);



        if (httpConnection.responseCode == 200) {
            inputStream = httpConnection.inputStream
            var length = inputStream.read()
            val sb = StringBuilder()
            while (length != -1) {
                sb.append(length.toChar())
                length = inputStream.read()
            }

            val startIndex = sb.indexOf("\"url\"") + 7
            // 设置返回内容到粘贴板
            setTextContentToClipboard("![](${sb.substring(startIndex, startIndex + 53).replace("\\", "")})")
        }

    } catch (e: Exception) {
        retValue = false
    } finally {
        if (dataOutputStream != null) {
            dataOutputStream.close()
        }
        if (inputStream != null) {
            inputStream.close()
        }
    }

    return retValue
}

