// @ts-ignore
import * as client from 'client'
import * as Styles from './styles.css'
import * as React from "react";
import * as classNames from 'classnames'
import ReactTribeCard from "../tribe-card/ReactTribeCard";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import ReactHeatmap from "../heatmap/ReactHeatmap";
import {useState} from "react";
import StatisticComposer from "../../runners/StatisticComposer";
import Tribe from '../../../../common/Tribe';
import Player from '../../../../common/Player';
import PairAssignmentSet from '../../../../common/PairAssignmentSet';

const commandDispatcher = client.commandDispatcher();

function calculateStats(tribe, players, history) {
    const composer = new StatisticComposer();

    const statSet = composer.compose(tribe, players, history);

    const heatmapData = commandDispatcher.performCalculateHeatMapCommand(players, history, statSet.spinsUntilFullRotation);

    return {statSet, heatmapData};
}

interface Props {
    tribe: Tribe
    players: Player[]
    history: PairAssignmentSet[]
    pathSetter: (url: String) => void
}

export default function ReactTribeStatistics(props: Props) {
    const {tribe, players, history, pathSetter} = props;

    const [statistics] = useState(() => calculateStats(tribe, players, history));
    const {statSet: {spinsUntilFullRotation, medianSpinDuration, pairReports}, heatmapData} = statistics;

    const activePlayerCount = players.length;

    return <div className={Styles.statsPage}>

        <div>
            <ReactTribeCard tribe={tribe} pathSetter={pathSetter}/>
            <div className={Styles.teamStatistics}>
                <div className={Styles.statsHeader}>Team Stats</div>
                <div>
                    <span className={Styles.statLabel}>Spins Until Ful Rotation:</span>
                    <span className={"rotation-number"}>{spinsUntilFullRotation}</span>
                </div>
                <div>
                    <span className={Styles.statLabel}>Number of Active Players:</span>
                    <span className={Styles.activePlayerCount}>{activePlayerCount}</span>
                </div>
                <div>
                    <span className={Styles.statLabel}>Median Spin Duration:</span>
                    <span className={Styles.medianSpinDuration}>{medianSpinDuration}</span>
                </div>
            </div>
        </div>

        <div>
            <div className={Styles.leftSection}>
                <div className={Styles.pairReportTable}>
                    {
                        pairReports.map(report =>
                            <div className={classNames(Styles.pairReport, 'react-pair-report')}>
                                {
                                    report.pair.map(player =>
                                        <div className={Styles.playerCard}>
                                            <ReactPlayerCard player={player} tribeId={tribe.id} size={50}/>
                                        </div>)
                                }
                                <div className={Styles.pairStatistics}>
                                    <div className={Styles.statsHeader}>Stats</div>
                                    <span className={Styles.statLabel}>Spins since last paired:</span>
                                    <span className={"time-since-last-pairing"}>{report.timeSinceLastPaired}</span>
                                </div>
                            </div>)
                    }
                </div>
            </div>
            <div className={Styles.rightSection}>
                <div className={Styles.heatmapPlayersTopRow}>
                    <div className={Styles.spacer}/>
                    {
                        players.map(player =>
                            <div className={Styles.playerCard}>
                                <ReactPlayerCard player={player} tribeId={tribe.id} size={50}/>
                            </div>
                        )
                    }
                </div>
                <div className={Styles.heatmapPlayersSideRow}>
                    {
                        players.map(player =>
                            <div className={Styles.playerCard}>
                                <ReactPlayerCard player={player} tribeId={tribe.id} size={50}/>
                            </div>
                        )
                    }
                </div>
                <ReactHeatmap data={heatmapData} className={Styles.heatmap}/>
            </div>
        </div>
    </div>
}