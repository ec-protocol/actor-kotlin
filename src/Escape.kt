import ec.actor.ControlByte

private const val ESCAPE_BYTE: Byte = 7
private const val PACKAGE_START_ESCAPE_BYTE: Byte = 8
private const val PACKAGE_END_ESCAPE_BYTE: Byte = 9
private const val CONTROL_PACKAGE_START_ESCAPE_BYTE: Byte = 10
private const val CONTROL_PACKAGE_END_ESCAPE_BYTE: Byte = 11
private const val IGNORE_ESCAPE_BYTE: Byte = 12
private val packageStartValue = ControlByte.PACKAGE_START.value
private val packageEndValue = ControlByte.PACKAGE_END.value
private val controlPackageStartValue = ControlByte.CONTROL_PACKAGE_START.value
private val controlPackageEndValue = ControlByte.CONTROL_PACKAGE_END.value
private val ignoreValue = ControlByte.IGNORE.value

fun escape(pkg: ByteArray): ByteArray {
    val ret = arrayListOf<Byte>()
    pkg.forEach {
        when (it) {
            packageStartValue -> {
                ret.add(ESCAPE_BYTE)
                ret.add(PACKAGE_START_ESCAPE_BYTE)
            }
            packageEndValue -> {
                ret.add(ESCAPE_BYTE)
                ret.add(PACKAGE_END_ESCAPE_BYTE)
            }
            controlPackageStartValue -> {
                ret.add(ESCAPE_BYTE)
                ret.add(CONTROL_PACKAGE_START_ESCAPE_BYTE)
            }
            controlPackageEndValue -> {
                ret.add(ESCAPE_BYTE)
                ret.add(CONTROL_PACKAGE_END_ESCAPE_BYTE)
            }
            ignoreValue -> {
                ret.add(ESCAPE_BYTE)
                ret.add(IGNORE_ESCAPE_BYTE)
            }
            ESCAPE_BYTE -> {
                ret.add(ESCAPE_BYTE)
                ret.add(ESCAPE_BYTE)
            }
            else                     -> {
                ret.add(it)
            }
        }
    }
    return ret.toByteArray()
}

fun unescape(pkg: ByteArray) = contextAwareUnescape(pkg).first

fun contextAwareUnescape(pkg: ByteArray, startInEscape: Boolean = false): Pair<ByteArray, Boolean> {
    val ret = arrayListOf<Byte>()
    var escape = false
    pkg.forEach {
        when {
            escape            -> {
                when (it) {
                    PACKAGE_START_ESCAPE_BYTE         -> {
                        ret.add(packageStartValue)
                    }
                    PACKAGE_END_ESCAPE_BYTE           -> {
                        ret.add(packageEndValue)
                    }
                    CONTROL_PACKAGE_START_ESCAPE_BYTE -> {
                        ret.add(controlPackageStartValue)
                    }
                    CONTROL_PACKAGE_END_ESCAPE_BYTE   -> {
                        ret.add(controlPackageEndValue)
                    }
                    IGNORE_ESCAPE_BYTE                -> {
                        ret.add(ignoreValue)
                    }
                    ESCAPE_BYTE                       -> {
                        ret.add(ESCAPE_BYTE)
                    }
                }
                escape = false
            }
            it == ESCAPE_BYTE -> {
                escape = true
            }
            else              -> {
                ret.add(it)
            }
        }
    }
    return Pair(ret.toByteArray(), escape)
}
