import * as React from "react";
import {useRef, useState} from "react";
import * as Styles from './styles.css'
import ReactTribeBrowser from "../tribebrowser/ReactTribeBrowser";
import * as dateFns from "date-fns";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import * as classNames from 'classnames'
import {DndProvider, DragObjectWithType, useDrag, useDrop} from 'react-dnd'
import Tribe from "../../../../common/Tribe";
import {PairAssignmentSet, Player} from "../../../../common";
import HTML5Backend from "react-dnd-html5-backend";
import ReactPlayerRoster from "../player-roster/ReactPlayerRoster";
import flatten from "ramda/es/flatten";
import differenceWith from "ramda/es/differenceWith";
import eqBy from "ramda/es/eqBy";
import prop from "ramda/es/prop";
import find from "ramda/es/find";
import clone from "ramda/es/clone";
import propEq from "ramda/es/propEq";
import ReactServerMessage from "../server-message/ReactServerMessage";
import Pair from "../../../../common/Pair";
import {Coupling} from "../../services";

const dateFormat = 'MM/DD/YYYY';
const timeFormat = 'HH:mm:ss';

const differenceOfPlayers = differenceWith(eqBy(prop('_id')));

function formatDate(pairAssignments) {
    return dateFns.format(pairAssignments.date, dateFormat);
}

function formatTime(pairAssignments) {
    return dateFns.format(pairAssignments.date, timeFormat);
}

function findCallSign(pair) {
    const adjectivePlayer = pair.length > 1 ? pair[1] : pair[0];
    let adjective = adjectivePlayer.callSignAdjective;
    let noun = pair[0].callSignNoun;
    if (adjective && noun) {
        return `${adjective} ${noun}`
    } else {
        return null
    }
}

let dragItemType = 'PLAYER';


interface PlayerDragObject extends DragObjectWithType {
    id: string
}

function DraggablePlayer(props: {
    player: Player,
    tribe: Tribe,
    pairAssignments: PairAssignmentSet,
    swapCallback: (droppedPlayerId: string) => void
}) {
    const {player, tribe, pairAssignments, swapCallback} = props;
    const ref = useRef(null);

    const [{isDragging}, drag] = useDrag({
        item: {type: dragItemType, id: player._id},
        collect: monitor => ({
            isDragging: !!monitor.isDragging(),
        }),
    });

    const [{isOver}, drop] = useDrop<PlayerDragObject, void, { isOver: boolean }>({
        accept: dragItemType,
        drop: function (item) {
            return swapCallback(item.id);
        },
        collect: monitor => ({
            isOver: !!monitor.isOver(),
            droppedPlayer: monitor.getItem()
        }),
    });

    drag(drop(ref));

    return <div ref={ref} style={{display: 'inline-block'}}>
        <ReactPlayerCard
            player={player}
            tribeId={tribe.id}
            disabled={false}
            className={classNames({
                hoverzoom: !pairAssignments._id,
                'on-drag-hover': isOver
            })}
        />
    </div>;
}

function findPairContainingPlayer(playerId, pairs: Player[][]) {
    return find(find(propEq('_id', playerId)), pairs);
}

function findUnpairedPlayers(players: Player[], pairAssignmentDocument: PairAssignmentSet): Player[] {
    if (!pairAssignmentDocument) {
        return players;
    }
    const currentlyPairedPlayers = flatten(pairAssignmentDocument.pairs);

    return differenceOfPlayers(players, currentlyPairedPlayers);
}

function AssignedPair(props: { pair, tribe: Tribe, pairAssignments, swapCallback: (droppedPlayerId: string, player: Player, pair: Player[]) => void, isNew: boolean, pathSetter }) {
    const {pair, tribe, pairAssignments, swapCallback, isNew, pathSetter} = props;
    let callSign = findCallSign(pair);
    return <span className={"pair"}>
                                <div>
                                    {
                                        tribe.callSignsEnabled && callSign
                                            ? <span className={"call-sign"}>{callSign}</span>
                                            : <span/>
                                    }
                                </div>
        {
            pair.map(player =>
                isNew
                    ? <DraggablePlayer
                        key={player._id}
                        player={player}
                        tribe={tribe}
                        pairAssignments={pairAssignments}
                        swapCallback={(droppedPlayerId) => swapCallback(droppedPlayerId, player, pair)}
                    />
                    : <ReactPlayerCard
                        player={player}
                        tribeId={tribe.id}
                        disabled={false}
                        pathSetter={pathSetter}
                    />
            )
        }
                            </span>
}

interface Props {
    tribe: Tribe
    pathSetter: (string) => void,
    pairAssignments: PairAssignmentSet,
    isNew: boolean,
    players: Player[],
    coupling: Coupling
}

export default function (props: Props) {
    return <DndProvider backend={HTML5Backend}>
        <ReactPairAssignments {...props}/>
    </DndProvider>
}

function swapPlayers(pairAssignments, droppedPlayerId, targetPlayer, targetPair) {
    const sourcePair = findPairContainingPlayer(droppedPlayerId, pairAssignments.pairs);
    const droppedPlayer = sourcePair.find(player => player._id === droppedPlayerId);

    if (sourcePair === targetPair) {
        return pairAssignments;
    }

    const newPairs = pairAssignments.pairs.map(pair => {
        if (pair === targetPair) {
            return pair.map(pairPlayer => {
                if (pairPlayer === targetPlayer) {
                    return droppedPlayer;
                } else {
                    return pairPlayer
                }
            }) as Pair
        } else if (pair === sourcePair) {
            return pair.map(pairPlayer => {
                if (pairPlayer === droppedPlayer) {
                    return targetPlayer;
                } else {
                    return pairPlayer
                }
            }) as Pair
        } else {
            return pair;
        }
    });
    const updatePairAssignments = clone(pairAssignments);
    updatePairAssignments.pairs = newPairs;
    return updatePairAssignments;
}

function SaveButton(props: { coupling, pairAssignments, tribe, pathSetter }) {
    const {coupling, pairAssignments, tribe, pathSetter} = props;

    return <a
        id={"save-button"}
        className={"super green button"}
        onClick={() =>
            coupling.saveCurrentPairAssignments(pairAssignments, tribe.id)
                .then(() => pathSetter(`/${tribe.id}/pairAssignments/current`))
        }>Save!</a>;
}

export function ReactPairAssignments(props: Props) {
    const {tribe, pathSetter, isNew, players} = props;

    const [pairAssignments, setPairAssignments] = useState(props.pairAssignments);

    return <div className={Styles.className}>
        <div>
            <ReactTribeBrowser tribe={tribe} pathSetter={pathSetter}/>
            <div className={"current pair-assignments"}>
                {
                    pairAssignments
                        ? <div>
                            <div>
                                <div className={"pair-assignments-header"}>
                                    Couples for {formatDate(pairAssignments)} - {formatTime(pairAssignments)}
                                </div>
                            </div>
                        </div>
                        : <div className={"no-pairs-notice"}>
                            No pair assignments yet!
                        </div>
                }
                <div id={'pair-assignments-content'}>
                    {pairAssignments
                        ?
                        pairAssignments.pairs.map((pair, index) =>
                            <AssignedPair
                                key={index}
                                pair={pair}
                                tribe={tribe}
                                isNew={isNew}
                                pathSetter={pathSetter}
                                pairAssignments={pairAssignments}
                                swapCallback={(droppedPlayerId, targetPlayer, targetPair) =>
                                    setPairAssignments(swapPlayers(
                                        pairAssignments,
                                        droppedPlayerId,
                                        targetPlayer,
                                        targetPair
                                    ))
                                }
                            />)
                        : []
                    }
                </div>
                <div> {isNew ? <SaveButton {...props} pairAssignments={pairAssignments}/> : <span/>} </div>
                <a id={"new-pairs-button"} className={"large pink button"} href={`/${tribe.id}/prepare/`}>
                    Prepare to spin!
                </a>
                <a id={"view-history-button"} className={"large blue button"} href={`/${tribe.id}/history/`}>
                    View history!
                </a>
                <a id={"retired-players-button"} className={"large yellow button"}
                   href={`/${tribe.id}/players/retired`}>
                    View retirees!
                </a>
            </div>
        </div>
        <ReactPlayerRoster
            label={"Unpaired players"}
            players={findUnpairedPlayers(players, pairAssignments)}
            tribeId={tribe.id}
            pathSetter={pathSetter}
        />
        <ReactServerMessage tribeId={tribe.id} useSsl={'https:' === window.location.protocol}/>
    </div>
}