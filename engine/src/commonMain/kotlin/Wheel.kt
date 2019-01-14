import kotlin.js.JsName
import kotlin.random.Random

interface Wheel {

    @JsName("random")
    val random: Random get() = Random.Default

    @JsName("spin")
    fun Array<Player>.spin(): Player = random(random)

}
