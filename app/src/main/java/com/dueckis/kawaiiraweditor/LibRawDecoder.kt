package com.dueckis.kawaiiraweditor

object LibRawDecoder {
    init {
        System.loadLibrary("kawaiiraweditor")
    }

    external fun decode(rawData: ByteArray, adjustmentsJson: String): ByteArray?
    external fun lowlowdecode(rawData: ByteArray, adjustmentsJson: String): ByteArray?
    external fun lowdecode(rawData: ByteArray, adjustmentsJson: String): ByteArray?
    external fun decodeFullRes(rawData: ByteArray, adjustmentsJson: String): ByteArray?
}
