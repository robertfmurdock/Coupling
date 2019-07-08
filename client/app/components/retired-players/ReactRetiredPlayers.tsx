import ReactTribeBrowser from "../tribebrowser/ReactTribeBrowser";
import * as React from "react";
import Tribe from "../../../../common/Tribe";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import Player from "../../../../common/Player";
import * as Styles from './styles.css'

export default function ReactRetiredPlayers(props: { tribe: Tribe, retiredPlayers: Player[], pathSetter: (string) => void }) {

    const {tribe, retiredPlayers, pathSetter} = props;

    return <div className={"react-retired-players"}>
        <ReactTribeBrowser tribe={tribe} pathSetter={pathSetter}/>
        <div className={Styles.retiredPlayersHeader}>Retired Players</div>
        <div>
            {
                retiredPlayers.map(player =>
                    <ReactPlayerCard
                        key={player._id}
                        player={player}
                        tribeId={tribe.id}
                        disabled={true}
                        className={"disabled"}
                    />)
            }
        </div>
    </div>
}