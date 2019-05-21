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
        const player = this.props.player;
        const size = this.props.size;

        const cardStyle = this.calculateCardStyle(size);
        const headerStyle = this.calculateHeaderStyle(size);

        return <div className={`${styles.player}`} style={cardStyle}>
            <img
                className="player-icon"
                src={playerGravatarUrl(player, {size})}
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
        this.fitPlayerName(this, this.props.size, styles.header);
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.fitPlayerName(this, this.props.size, styles.header);
    }

    fitPlayerName(component: any, size: any, className: any) {
        const maxFontHeight = (size * 0.31);
        const minFontHeight = (size * 0.16);
        fitHeaderText(maxFontHeight, minFontHeight, component, className);
    }

    private clickPlayerName(event) {
        if (this.props.disabled) {
            return;
        }

        if (event.stopPropagation) event.stopPropagation();

        this.props.pathSetter(`/${this.props.tribeId}/player/${this.props.player._id}`)
    }

}