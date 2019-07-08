import * as React from "react";
import Player from "../../../../common/Player";
import Tribe from "../../../../common/Tribe";
import * as Styles from "./styles.css";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import ReactHeatmap from "../heatmap/ReactHeatmap";

export default function PlayerHeatmap(props: { players: Player[], tribe: Tribe, heatmapData }) {
    const {players, tribe, heatmapData} = props;
    return <div className={Styles.rightSection}>
        <div className={Styles.heatmapPlayersTopRow}>
            <div className={Styles.spacer}/>
            {
                players.map(player =>
                    <div key={player._id} className={Styles.playerCard}>
                        <ReactPlayerCard player={player} tribeId={tribe.id} size={50}/>
                    </div>
                )
            }
        </div>
        <div className={Styles.heatmapPlayersSideRow}>
            {
                players.map(player =>
                    <div key={player._id} className={Styles.playerCard}>
                        <ReactPlayerCard player={player} tribeId={tribe.id} size={50}/>
                    </div>
                )
            }
        </div>
        <ReactHeatmap data={heatmapData} className={Styles.heatmap}/>
    </div>;
}