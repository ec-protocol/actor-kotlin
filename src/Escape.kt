import ec.actor.ControlByte

private const val ESCAPE_BYTE: Byte = 7
private const val PACKAGE_START_ESCAPE_BYTE: Byte = 8
private const val PACKAGE_END_ESCAPE_BYTE: Byte = 9
private const val CONTROL_PACKAGE_START_ESCAPE_BYTE: Byte = 10
private const val CONTROL_PACKAGE_END_ESCAPE_BYTE: Byte = 11
private const val IGNORE_ESCAPE_BYTE: Byte = 12

fun escape(pkg: ByteArray): ByteArray {
    val ret = arrayListOf<Byte>()
    pkg.forEach {
        when (it) {
            ControlByte.PACKAGE_START.value -> {
                ret.add(ESCAPE_BYTE)
                ret.add(PACKAGE_START_ESCAPE_BYTE)
            }
            ControlByte.PACKAGE_END.value -> {
                ret.add(ESCAPE_BYTE)
                ret.add(PACKAGE_END_ESCAPE_BYTE)
            }
            ControlByte.CONTROL_PACKAGE_START.value -> {
                ret.add(ESCAPE_BYTE)
                ret.add(CONTROL_PACKAGE_START_ESCAPE_BYTE)
            }
            ControlByte.CONTROL_PACKAGE_END.value -> {
                ret.add(ESCAPE_BYTE)
                ret.add(CONTROL_PACKAGE_END_ESCAPE_BYTE)
            }
            ControlByte.IGNORE.value -> {
                ret.add(ESCAPE_BYTE)
                ret.add(IGNORE_ESCAPE_BYTE)
            }
            ESCAPE_BYTE -> {
                ret.add(ESCAPE_BYTE)
                ret.add(ESCAPE_BYTE)
            }
            else -> {
                ret.add(it)
            }
        }
    }
    return ret.toByteArray()
}

fun unescape(pkg: ByteArray): ByteArray {
    val ret = arrayListOf<Byte>()
    var i = 0
    while (i < pkg.size) {
        val byte = pkg[i]
        if (byte == ESCAPE_BYTE) {
            i++
            when(pkg[i]) {
                PACKAGE_START_ESCAPE_BYTE -> {
                    ret.add(ControlByte.PACKAGE_START.value)
                }
                PACKAGE_END_ESCAPE_BYTE -> {
                    ret.add(ControlByte.PACKAGE_END.value)
                }
                CONTROL_PACKAGE_START_ESCAPE_BYTE -> {
                    ret.add(ControlByte.CONTROL_PACKAGE_START.value)
                }
                CONTROL_PACKAGE_END_ESCAPE_BYTE -> {
                    ret.add(ControlByte.CONTROL_PACKAGE_END.value)
                }
                IGNORE_ESCAPE_BYTE -> {
                    ret.add(ControlByte.IGNORE.value)
                }
                ESCAPE_BYTE -> {
                    ret.add(ESCAPE_BYTE)
                }
            }
        } else {
            ret.add(byte)
        }
        i++
    }
    return ret.toByteArray()
}
