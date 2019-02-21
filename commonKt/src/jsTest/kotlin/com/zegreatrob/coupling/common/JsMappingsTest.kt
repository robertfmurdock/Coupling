
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

@Suppress("unused")
class JsMappingsTest {

    class PlayerMappings {
        @Test
        fun typicalPlayerRoundTrip() = setup(object {
            val original = json(
                    "_id" to "599ae087ce362e001a8c1620",
                    "badge" to 1,
                    "name" to "Neal",
                    "email" to "nfebbrar@gmail.com"
            )
        }) exercise {
            original.toPlayer().toJson()
        } verify { result -> result.assertIsEquivalentTo(original) }

        @Test
        fun secretPropertiesRoundTrip() = setup(object {
            val original = json(
                    "_id" to "TheGuy",
                    "badge" to 2,
                    "name" to "Teal",
                    "email" to "tealguy@gmail.com",
                    "callSignAdjective" to "Furious",
                    "callSignNoun" to "Squirrel",
                    "imageURL" to "batman.png"
            )
        }) exercise {
            original.toPlayer().toJson()
        } verify { result -> result.assertIsEquivalentTo(original) }
    }

    companion object {
        private fun Json.assertIsEquivalentTo(expected: Json) = toComparableMap()
                .assertIsEqualTo(expected.toComparableMap())


        private fun Json.toComparableMap() = (js("Object").keys(this) as Array<String>)
                .map { key ->
                    key to this[key].let {
                        if (it is Array<*>) {
                            it.asList()
                        } else it
                    }
                }.filterNot { (_, value) -> value == null }
                .toMap()
    }
}