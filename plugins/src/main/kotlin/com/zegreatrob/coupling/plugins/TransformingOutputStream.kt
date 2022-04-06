package com.zegreatrob.coupling.plugins

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

class TransformingOutputStream(val processLine: (String) -> Unit) : OutputStream() {
    private val messageLimitBytes = 1024 * 1024
    private var closed: Boolean = false
    private val buffer = ByteArrayOutputStream()
    private var overflowInsideMessage: Boolean = false

    override fun close() {
        closed = true
        flushLine()
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed) throw IOException("The stream has been closed.")
        var i = off
        var last = off

        fun bytesToAppend() =
            i - last

        val end = off + len

        fun append(len: Int = bytesToAppend()) {
            buffer.write(b, last, i - last)
            last += len
        }

        while (i < end) {
            val c = b[i++]
            if (c == '\n'.toByte()) {
                append()
                flushLine()
            } else if (buffer.size() + bytesToAppend() >= messageLimitBytes) {
                append(messageLimitBytes - buffer.size())
                overflow()
            }
        }

        append()
    }

    private fun overflow() {
        buffer.write(ByteArray(1) { '\n'.toByte() })
        flushLine()
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        write(byteArrayOf(b.toByte()), 0, 1)
    }

    private fun flushLine() {
        overflowInsideMessage = false
        if (buffer.size() > 0) {
            val text = buffer.toString("utf-8")
            processLine(text)
            buffer.reset()
        }
    }
}