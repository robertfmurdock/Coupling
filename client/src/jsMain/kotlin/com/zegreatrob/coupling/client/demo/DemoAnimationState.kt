package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.Frame
import com.zegreatrob.coupling.client.components.pairassignments.pairAssignmentsClassName
import com.zegreatrob.coupling.client.components.pairassignments.prepareToSpinButtonClassName
import com.zegreatrob.coupling.client.components.party.partyConfigContentClassName
import com.zegreatrob.coupling.client.components.pin.pinConfigContentClassName
import com.zegreatrob.coupling.client.components.player.playerConfigContentClassName
import com.zegreatrob.coupling.client.components.spin.playerSelectorClass
import com.zegreatrob.coupling.client.components.svgPath
import com.zegreatrob.coupling.client.components.welcome.playerImage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotools.types.collection.notEmptyListOf
import popper.core.Placement
import web.cssom.ClassName
import kotlin.time.Clock
import kotlin.uuid.Uuid

private val demoParty = PartyDetails(
    id = PartyId("${Uuid.random()}"),
    name = "The Simpsons",
    imageURL = svgPath("parties/simpsons"),
)

private val homer by playerImage()
private val marge by playerImage()
private val bart by playerImage()
private val lisa by playerImage()
private val maggie by playerImage()
private val slh by playerImage()

private val player1 = defaultPlayer.copy(id = PlayerId.new(), name = "Homer", imageURL = homer)
private val player2 = defaultPlayer.copy(id = PlayerId.new(), name = "Marge", imageURL = marge)
private val player3 = defaultPlayer.copy(id = PlayerId.new(), name = "Bart", imageURL = bart)
private val player4 = defaultPlayer.copy(id = PlayerId.new(), name = "Lisa", imageURL = lisa)
private val player5 = defaultPlayer.copy(id = PlayerId.new(), name = "Maggie", imageURL = maggie)
private val player6 = defaultPlayer.copy(id = PlayerId.new(), name = "Santa's Lil Helper", imageURL = slh)

private val players = listOf(
    player1,
    player2,
    player3,
    player4,
    player5,
    player6,
)

private val pins = listOf(
    Pin(id = PinId.new(), name = "watchman", icon = "eye"),
)

private val pairAssignments = PairAssignmentDocument(
    id = PairAssignmentDocumentId.new(),
    date = Clock.System.now(),
    pairs = notEmptyListOf(
        pairOf(player1, player4).withPins(emptySet()),
        pairOf(player2, player5).withPins(pins.toSet()),
        pairOf(player6).withPins(emptySet()),
    ),
    discordMessageId = null,
)

sealed class DemoAnimationState {

    open val descriptionSelector: String get() = ""
    open val description: String get() = ""
    open val placement: Placement get() = Placement.right
    open val showReturnButton: Boolean = false

    companion object {
        fun generateSequence(): Sequence<Frame<DemoAnimationState>> = listOfPairs()
            .runningFold(Frame<DemoAnimationState>(Start, 0)) { frame, (state, time) ->
                Frame(state, frame.delay + time)
            }.asSequence()

        private fun listOfPairs(): List<Pair<DemoAnimationState, Int>> = listOf(Pair(ShowIntro, 3000)) +
            makePartySequence().pairWithDurations(800, 100) +
            makePlayerSequence() +
            makePinSequence().pairWithDurations(800, 100) +
            (CurrentPairs(demoParty, players, pins, null, false) to 3000) +
            (PrepareToSpin(demoParty, players.map { it to false }, pins) to 4000) +
            (PrepareToSpin(demoParty, players.map { it to true }, pins) to 1500) +
            (PrepareToSpin(demoParty, players.map { it to (it != player3) }, pins) to 1500) +
            (CurrentPairs(demoParty, players, pins, pairAssignments, true) to 1500) +
            (CurrentPairs(demoParty, players, pins, pairAssignments, false) to 12000)
    }
}

private fun <T> List<T>.pairWithDurations(firstDuration: Int, duration: Int) = mapIndexed { index, it ->
    it to if (index == 0) firstDuration else duration
}

data object Start : DemoAnimationState() {
    val text = """
        # A Demo of Coupling
        
        How's this thing work? Well here's some typical usage.
        
        ---
        
    """.trimIndent()
}

data object ShowIntro : DemoAnimationState() {
    val text = """
        # A Demo of Coupling
        
        Alright! Here we go!
        
        ---
        
        #### See you on the other side!
    """.trimIndent()
}

fun makePartySequence() = demoParty.name.rangeOfStringLength().map { index ->
    MakeParty(demoParty.copy(name = demoParty.name?.substring(0, index)))
}

fun makePlayerSequence() = players.flatMapIndexed { playerIndex, player ->
    playerNameSequence(player, players.subList(0, playerIndex))
        .pairWithDurations(750, 100)
} + (AddPlayer(demoParty, players.last(), players) to 100)

private fun playerNameSequence(
    player: Player,
    playersSoFar: List<Player>,
) = player.name.rangeOfStringLength().map { index ->
    AddPlayer(demoParty, player.copy(name = player.name.substring(0, index)), playersSoFar)
}

fun makePinSequence() = pins.flatMapIndexed { pinIndex, pin ->
    pin.name.rangeOfStringLength().map { index ->
        AddPin(demoParty, pin.copy(name = pin.name.substring(0, index)), pins.subList(0, pinIndex))
    }
} + AddPin(demoParty, pins.last(), pins)

private fun String?.rangeOfStringLength() = (0..(this ?: "").length)

data class MakeParty(val party: PartyDetails) : DemoAnimationState() {
    override val descriptionSelector = ".$partyConfigContentClassName input[name=name]"
    override val placement = Placement.bottomStart
    override val description = """

## First, we configure a new party.

We'll enter the name and then save.

"""
}

data class AddPlayer(val party: PartyDetails, val newPlayer: Player, val players: List<Player>) : DemoAnimationState() {
    override val descriptionSelector = ".$playerConfigContentClassName li:first-of-type"
    override val description = """
## Now we'll add a few players. 

Just enough players to make it interesting.

Pro tip: if you enter a player's email, they can log in using that email and see the party! 

In this way, your entire team can operate Coupling.

"""
}

data class AddPin(val party: PartyDetails, val newPin: Pin, val pins: List<Pin>) : DemoAnimationState() {
    override val descriptionSelector = ".$pinConfigContentClassName li:first-of-type"
    override val description = """
## And now... a pin! 

A pin is a way to highlight a special job, role, or hat that a pair can wear.

It'll prefer to be with pairs that haven't done it recently, but it's not too particular about these things.

"""
}

data class CurrentPairs(
    val party: PartyDetails,
    val players: List<Player>,
    val pins: List<Pin>,
    val pairAssignments: PairAssignmentDocument?,
    val allowSave: Boolean,
) : DemoAnimationState() {

    override val descriptionSelector = if (pairAssignments == null) {
        classSelector(prepareToSpinButtonClassName)
    } else {
        ".$pairAssignmentsClassName div"
    }
    override val placement: Placement = if (pairAssignments == null) {
        super.placement
    } else {
        Placement.bottom
    }

    override val showReturnButton: Boolean = pairAssignments != null && !allowSave

    override val description = if (pairAssignments == null) {
        """
## Alright. Now we're prepared. 

Its time to spin!

We'll hit the spin button.
"""
    } else if (allowSave) {
        """
            ## Here they are!
        
            If we want to swap people, we can drag one to another.
            
            Once we like it, we lock it in by hitting "save".
        """.trimIndent()
    } else {
        """
            ## All done and ready to go!
        
            That's basic usage! You can probably figure it out from here.
            
            Thanks for watching, and...
            
            Happy Coupling!
            
        """.trimIndent()
    }
}

private fun classSelector(className: ClassName) = ".$className"

data class PrepareToSpin(val party: PartyDetails, val players: List<Pair<Player, Boolean>>, val pins: List<Pin>) : DemoAnimationState() {
    override val descriptionSelector = ".$playerSelectorClass"
    override val placement: Placement = Placement.right
    override val description: String = """
        ## Time to choose today's players!
        
        We'll tap on the people we want to include, or again to exclude.
        
        Once we're ready, we'll hit spin again. 
        
    """.trimIndent()
}
