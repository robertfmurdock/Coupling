import * as React from "react";
import * as classNames from 'classnames'
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import Player from "../../../../common/Player";

interface Props {
    label?: string,
    players: Player[],
    tribeId: string,
    pathSetter: (string) => void,
    className?: string
}

export default class ReactPlayerRoster extends React.Component<Props> {

    render() {
        const {label, tribeId, className} = this.props;

        return <div className={classNames("react-player-roster", className)}>
            <div>
                <div className="roster-header">
                    {label || 'Players'}
                </div>
                {this.renderPlayers()}
            </div>
            <a id="add-player-button" className="large orange button" href={`/${tribeId}/player/new/`}>
                Add a new player!
            </a>
        </div>;
    }

    private renderPlayers() {
        const {players, tribeId, pathSetter} = this.props;

        if (players) {
            return players.map(player => {
                return <ReactPlayerCard
                    player={player}
                    tribeId={tribeId}
                    pathSetter={pathSetter}
                    disabled={false}
                    key={player._id}
                />;
            })
        } else {
            return undefined;
        }
    }
}