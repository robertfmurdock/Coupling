import kotlin.js.Date
import kotlin.js.Json

interface DbRecordInfoSyntax : UserContextSyntax {
    fun Json.addRecordInfo() = also {
        this["timestamp"] = Date()
        this["modifiedByUsername"] = username()
    }
}