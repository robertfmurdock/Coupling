interface MonkToolkit {

    fun id(): String {
        @Suppress("UNUSED_VARIABLE")
        val monk = js("require(\"monk\")")
        return js("monk.id()").toString()
    }

    fun jsRepository(mongoUrl: String): dynamic {
        @Suppress("UNUSED_VARIABLE")
        val clazz = js("require('../../../../lib/CouplingDataService').default")
        return js("new clazz(mongoUrl)")
    }

    fun getCollection(collectionName: String, mongoUrl: String): dynamic {
        @Suppress("UNUSED_VARIABLE")
        val monk = js("require(\"monk\")")
        val db = js("monk.default(mongoUrl)")
        return db.get(collectionName)
    }
}
