interface Spy<I, O> {
    val spyReceivedValues: MutableList<I>
    val spyReturnValues: MutableList<O>
    fun spyFunction(input: I) = spyReturnValues.popValue()!!.also { spyReceivedValues.add(input) }

    infix fun spyWillReturn(values: Collection<O>) {
        spyReturnValues += values
    }

    infix fun spyWillReturn(value: O) {
        spyReturnValues += value
    }
}

class SpyData<I, O> : Spy<I, O> {
    override val spyReceivedValues = mutableListOf<I>()
    override val spyReturnValues = mutableListOf<O>()
}