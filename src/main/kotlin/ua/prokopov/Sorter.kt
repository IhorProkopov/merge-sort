package ua.prokopov

import java.io.*
import java.util.*

/**
 * Implementation of Merge sort algorithm [https://en.wikipedia.org/wiki/Merge_sort]
 */
class Sorter {

    /**
     * This method will sort file's content and save it to new file
     *
     * @param      fileName   file to sort
     * @param      newFileName   file to save result
     * @param      buffer   buffer size
     */
    fun sort(fileName: String, newFileName: String, buffer: Long) {
        totalReduce(map(fileName, buffer), newFileName, buffer)
    }

    private tailrec fun totalReduce(files: MutableList<String>, newFileName: String, bufferSize: Long) {
        val newList = mutableListOf<String>()
        for (x in 1 until files.size step 2) {
            newList.add(reduce(File(files[x - 1]), File(files[x]), bufferSize))
        }
        if (files.size > 1 && files.size % 2 != 0) {
            newList.add(files.last())
            files.remove(files.last())
        }
        files.map { File(it) }.forEach { it.delete() }
        if (newList.size > 1) {
            totalReduce(newList, newFileName, bufferSize)
        } else {
            File(newList.first()).renameTo(File(newFileName))
        }
    }

    private fun map(fileName: String, buffer: Long): MutableList<String> {
        val files = mutableListOf<String>()
        val raf = RandomAccessFile(fileName, "r")
        val numSplits = if ((File(fileName).length() / buffer) % 2 == 0L) File(fileName).length() / buffer + 2
        else File(fileName).length() / buffer + 1
        val sourceSize = raf.length()
        val bytesPerSplit = (sourceSize / numSplits) + 1
        val remainingBytes = sourceSize % numSplits
        for (destIx in 1..numSplits) {
            val name = "split-$destIx.txt"
            files.add(name)
            val bw = BufferedOutputStream(FileOutputStream(name))
            if (bytesPerSplit > buffer) {
                val numReads = bytesPerSplit / buffer
                val numRemainingRead = bytesPerSplit % buffer
                for (i in 0 until numReads) {
                    readWrite(raf, bw, buffer)
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead)
                }
            } else {
                readWrite(raf, bw, bytesPerSplit)
            }
            bw.close()
        }
        if (remainingBytes > 0) {
            val name = "split-${numSplits + 1}.txt"
            files.add(name)
            val bw = BufferedOutputStream(FileOutputStream(name))
            readWrite(raf, bw, remainingBytes)
            bw.close()
        }
        raf.close()
        return files
    }

    private fun readWrite(raf: RandomAccessFile, bw: BufferedOutputStream, numBytes: Long) {
        val buf = ByteArray(numBytes.toInt())
        val `val` = raf.read(buf)
        if (`val` != -1) {
            bw.write(sort(buf))
        }
    }

    private fun sort(buf: ByteArray): ByteArray {
        val chars = String(buf).toCharArray()
        chars.sort()
        return String(chars).toByteArray()
    }


    private fun reduce(file1: File, file2: File, bufferSize: Long): String {
        val newFile = File("temp_${Random().nextLong()}.txt")
        FileInputStream(file1).use { `in` ->
            InputStreamReader(`in`).use { reader ->
                BufferedReader(reader).use { buffer1 ->
                    FileInputStream(file2).use { `in` ->
                        InputStreamReader(`in`).use { reader ->
                            BufferedReader(reader).use { buffer2 ->
                                handleCharacters(buffer1, buffer2, buffer1.read(), buffer2.read(), newFile,
                                        StringBuffer(bufferSize.toInt()), bufferSize)
                            }
                        }
                    }
                }
            }
        }
        return newFile.name
    }

    private tailrec fun handleCharacters(reader1: Reader, reader2: Reader,
                                         v1: Int, v2: Int, file: File, buffer: StringBuffer,
                                         bufferSize: Long) {
        if (buffer.length > bufferSize) {
            file.appendText(buffer.toString())
            buffer.delete(0, buffer.length)
        }
        when {
            v1 <= v2 && v1 != -1 -> {
                buffer.append(v1.toChar())
                handleCharacters(reader1, reader2, reader1.read(), v2, file, buffer, bufferSize)
            }
            v2 <= v1 && v2 != -1 -> {
                buffer.append(v2.toChar())
                handleCharacters(reader1, reader2, v1, reader2.read(), file, buffer, bufferSize)
            }
            v1 == -1 && v2 != -1 -> {
                buffer.append(v2.toChar())
                handleCharacters(reader1, reader2, v1, reader2.read(), file, buffer, bufferSize)
            }
            v2 == -1 && v1 != -1 -> {
                buffer.append(v1.toChar())
                handleCharacters(reader1, reader2, reader1.read(), v2, file, buffer, bufferSize)
            }
            else -> file.appendText(buffer.toString())
        }
    }

}