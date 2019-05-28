import * as React from "react";
import ReactTribeCard from "../tribe-card/ReactTribeCard";
import PairAssignmentSet from "../../../../common/PairAssignmentSet";
import {Tribe} from "../../../../common";
import PathSetter from "../PathSetter";
import * as dateFns from 'date-fns'

const dateFormat = 'MM/DD/YYYY';
const timeFormat = 'HH:mm:ss';

interface Props {
    tribe: Tribe
    history: PairAssignmentSet[]
    pathSetter: PathSetter,
    coupling: { removeAssignments(pairAssignments: PairAssignmentSet, tribeId: String) },
    reload: () => void
}

export default class ReactHistory extends React.Component<Props> {

    render() {
        const {tribe, pathSetter} = this.props;
        return <div>
            <div id="tribe-browser">
                <ReactTribeCard tribe={tribe} pathSetter={pathSetter}/>
            </div>
            <span id="history-view">
                <div className="header"> History! </div>
                {this.pairAssignmentList()}
            </span>
        </div>;
    }

    private pairAssignmentList() {
        const {history} = this.props;

        return history.map(pairAssignments => {
            return <div className="pair-assignments">
                    <span className="pair-assignments-header">
                        {this.dateText(pairAssignments)}
                    </span>
                <span className="small red button delete-button"
                      onClick={() => this.removeButtonOnClick(pairAssignments)}>
                    DELETE
                </span>
                <div> {this.showPairs(pairAssignments)} </div>
            </div>;
        })
    }

    private showPairs(pairAssignments: PairAssignmentSet) {
        return pairAssignments.pairs.map(pair =>
            <span className="pair">
                {pair.map(player =>
                    <span className="player">
                        <div className="player-header">{player.name}</div>
                    </span>)}
            </span>);
    }

    private dateText(pairAssignments) {
        return `${this.formatDate(pairAssignments)} - ${this.formatTime(pairAssignments)}`;
    }

    private formatTime(pairAssignments) {
        return dateFns.format(pairAssignments.date, timeFormat);
    }

    private formatDate(pairAssignments) {
        return dateFns.format(pairAssignments.date, dateFormat);
    }

    private async removeButtonOnClick(pairAssignments: PairAssignmentSet) {
        const {coupling, tribe, reload} = this.props;
        if (confirm("Are you sure you want to delete these pair assignments?")) {
            await coupling.removeAssignments(pairAssignments, tribe.id);
            reload()
        } else {
            console.log('hohohohohohool')
        }
    }
}