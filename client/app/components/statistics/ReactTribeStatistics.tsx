// @ts-ignore
import * as client from 'client'
import * as Styles from './styles.css'
import * as React from "react";
import {useState} from "react";
import * as classNames from 'classnames'
import ReactTribeCard from "../tribe-card/ReactTribeCard";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import StatisticComposer from "../../runners/StatisticComposer";
import Tribe from '../../../../common/Tribe';
import Player from '../../../../common/Player';
import PairAssignmentSet from '../../../../common/PairAssignmentSet';
import PlayerHeatmap from "./PlayersHeatmap";

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

export function TeamStatistics(props: { spinsUntilFullRotation, activePlayerCount, medianSpinDuration }) {
    const {spinsUntilFullRotation, activePlayerCount, medianSpinDuration} = props;
    return <div className={Styles.teamStatistics}>
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
    </div>;
}

export function PairReportTable(props: { pairReports, tribe }) {
    const {pairReports, tribe} = props;
    return <div className={Styles.pairReportTable}>
        {
            pairReports.map(report =>
                <div key={report._id} className={classNames(Styles.pairReport, 'react-pair-report')}>
                    {
                        report.pair.map(player =>
                            <div key={player._id} className={Styles.playerCard}>
                                <ReactPlayerCard player={player} tribeId={tribe.id} size={50} />
                            </div>)
                    }
                    <div className={Styles.pairStatistics}>
                        <div className={Styles.statsHeader}>Stats</div>
                        <span className={Styles.statLabel}>Spins since last paired:</span>
                        <span className={"time-since-last-pairing"}>{report.timeSinceLastPaired}</span>
                    </div>
                </div>)
        }
    </div>;
}

export default function ReactTribeStatistics(props: Props) {
    const {tribe, players, history, pathSetter} = props;

    const [statistics] = useState(() => calculateStats(tribe, players, history));
    const {statSet: {spinsUntilFullRotation, medianSpinDuration, pairReports}, heatmapData} = statistics;

    const activePlayerCount = players.length;

    return <div className={Styles.statsPage}>
        <div>
            <ReactTribeCard tribe={tribe} pathSetter={pathSetter}/>
            <TeamStatistics
                spinsUntilFullRotation={spinsUntilFullRotation}
                activePlayerCount={activePlayerCount}
                medianSpinDuration={medianSpinDuration}
            />
        </div>
        <div>
            <div className={Styles.leftSection}>
                <PairReportTable tribe={tribe} pairReports={pairReports}/>
            </div>
            <PlayerHeatmap players={players} tribe={tribe} heatmapData={heatmapData}/>
        </div>
    </div>
}