interface PinAssignmentSyntax {
    fun List<Player>.assign(pins: List<Pin>) = map { it.copy(pins = pins) }

}