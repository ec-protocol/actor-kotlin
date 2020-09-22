package ec.actor

enum class ControlByte(val value: Byte) {
    PACKAGE_START(1),
    PACKAGE_END(2),
    CONTROL_PACKAGE_START(3),
    CONTROL_PACKAGE_END(4),
    IGNORE(5)
}