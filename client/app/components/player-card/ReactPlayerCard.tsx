import * as React from 'react'
import * as styles from './styles.css'
import Player from "../../../../common/Player";
import {gravatarUrl} from "./GravatarHelper";
import {fitPlayerName} from "../ReactFittyHelper";

interface Props {
    player: Player,
    tribeId: string,
    disabled: boolean,
    size: number
}

export default class ReactPlayerCard extends React.Component<Props> {

    static defaultProps = {
        size: 100
    };

    render() {
        const player = this.props.player;
        const size = this.props.size;

        const cardStyle = this.calculateCardStyle(size);
        const headerStyle = this.calculateHeaderStyle(size);

        return <div className={`${styles.player}`} style={cardStyle}>
            <img
                className="player-icon"
                src={gravatarUrl(player, {size})}
                alt="icon"
                width={size}
                height={size}
            />
            <div className={`player-card-header ${styles.header}`}
                 style={headerStyle}
                 onClick={(event) => this.clickPlayerName(event)}
            >
                <div>
                    {player._id ? '' : 'NEW:'}
                    {player.name || 'Unknown'}
                </div>
            </div>
        </div>
    }

    private calculateHeaderStyle(size) {
        const headerMargin = (size * 0.02);
        return {margin: `${headerMargin}px 0 0 0`,};
    }

    private calculateCardStyle(size) {
        const pixelWidth = size;
        const pixelHeight = (size * 1.4);
        const paddingAmount = (size * 0.06);

        const borderAmount = (size * 0.01);
        return {
            width: `${pixelWidth}px`,
            height: `${pixelHeight}px`,
            padding: `${paddingAmount}px`,
            'border-width': `${borderAmount}px`,
        };
    }

    componentDidMount(): void {
        fitPlayerName(this, this.props.size);
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        fitPlayerName(this, this.props.size);
    }

    private clickPlayerName(event) {
        if (this.props.disabled) {
            return;
        }

        if (event.stopPropagation) event.stopPropagation();

        window.location.pathname = `/${this.props.tribeId}/player/${this.props.player._id}`
    }

}