import * as React from 'react'
import * as styles from './styles.css'
import Player from "../../../../common/Player";
import {playerGravatarUrl} from "./GravatarHelper";
import {fitHeaderText} from "../ReactFittyHelper";

interface Props {
    player: Player,
    tribeId: string,
    disabled: boolean,
    size: number,
    pathSetter: (string) => void
}

export default class ReactPlayerCard extends React.Component<Props> {

    static defaultProps = {
        size: 100
    };

    render() {
        return <div className={`${styles.player}`} style={this.cardStyle()}>
            {this.gravatarImage()}
            {this.cardHeader()}
        </div>
    }

    private cardStyle() {
        const size = this.props.size;
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

    private gravatarImage() {
        const player = this.props.player;
        const size = this.props.size;
        return <img
            className="player-icon"
            src={playerGravatarUrl(player, {size})}
            alt="icon"
            width={size}
            height={size}
        />;
    }

    private cardHeader() {
        const player = this.props.player;
        return <div className={`player-card-header ${styles.header}`}
                    style={this.headerStyle()}
                    onClick={(event) => this.clickPlayerName(event)}
        >
            <div>
                {player._id ? '' : 'NEW:'}
                {player.name || 'Unknown'}
            </div>
        </div>;
    }

    private headerStyle() {
        const size = this.props.size;
        const headerMargin = (size * 0.02);
        return {margin: `${headerMargin}px 0 0 0`,};
    }

    private clickPlayerName(event) {
        if (this.props.disabled) {
            return;
        }

        if (event.stopPropagation) event.stopPropagation();

        this.props.pathSetter(`/${this.props.tribeId}/player/${this.props.player._id}`)
    }

    componentDidMount(): void {
        this.fitPlayerName();
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.fitPlayerName();
    }

    private fitPlayerName() {
        const size = this.props.size;
        const maxFontHeight = (size * 0.31);
        const minFontHeight = (size * 0.16);
        fitHeaderText(maxFontHeight, minFontHeight, this, styles.header);
    }

}