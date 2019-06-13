import * as React from "react";
import ReactTribeBrowser from "../tribebrowser/ReactTribeBrowser";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import Player from "../../../../common/Player";
import {Tribe, PairAssignmentSet} from "../../../../common";
import * as Styles from './styles.css'
import classNames from 'classnames'
import {useState} from "react";
import flatten from "ramda/es/flatten";
import find from "ramda/es/find";
import propEq from "ramda/es/propEq";

function flipSelectionForPlayer(player, isSelected, playerSelections) {
    return playerSelections.map((pair) => {
        if (pair.player === player) {
            return {player, isSelected: !isSelected}
        } else {
            return pair;
        }
    });
}

function isInLastSetOfPairs(player, history) {
    if (history.length === 0) {
        return true;
    }

    const flattenResult = flatten(history[0].pairs);
    const result = find(propEq('_id', player._id), flattenResult);
    return !!result;
}


interface Props {
    players: Player[];
    tribe: Tribe;
    pathSetter: (string) => void;
    history: PairAssignmentSet[];
}

export default function ReactPrepareSpin(props: Props) {

    const {players, tribe, pathSetter, history} = props;

    const [playerSelections, setPlayerSelections] = useState(players.map(player => {
        return {player: player, isSelected: isInLastSetOfPairs(player, history)}
    }));

    return <div className={Styles.className}>
        <div>
            <ReactTribeBrowser {...props} />
        </div>
        <div>
            <div>
                <a
                    className={classNames(Styles.spinButton, 'spin-button', 'super', 'pink', 'button')}
                    onClick={() => {
                        let url = `${tribe.id}/pairAssignments/new?`
                            + playerSelections.filter(element => element.isSelected)
                                .map(element => "player=" + encodeURIComponent(element.player._id))
                                .join('&');
                        pathSetter(url);
                    }}
                >
                    Spin!
                </a>
            </div>
            {
                playerSelections.map(({player, isSelected}) =>
                    <ReactPlayerCard
                        player={player}
                        tribeId={tribe.id}
                        disabled={true}
                        className={classNames(Styles.playerCard, {'disabled': !isSelected})}
                        onClick={() => {
                            setPlayerSelections(
                                flipSelectionForPlayer(player, isSelected, playerSelections)
                            )
                        }
                        }/>)
            }
        </div>
    </div>
}