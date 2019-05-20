import * as React from 'react'
import * as styles from './styles.css'
import Player from "../../../../common/Player";
import * as md5 from 'blueimp-md5'
import * as ReactDOM from "react-dom";
import fitty from 'fitty'

interface Props {
    player: Player,
    tribeId: string,
    disabled: boolean,
    size: number
}

export default class ReactPlayerCard extends React.Component<Props> {

    render() {
        const player = this.props.player;
        const size = this.props.size || 100;

        const pixelWidth = size;
        const pixelHeight = (size * 1.4);
        const paddingAmount = (size * 0.06);

        const borderAmount = (size * 0.01);
        const cardStyle = {
            width: `${pixelWidth}px`,
            height: `${pixelHeight}px`,
            padding: `${paddingAmount}px`,
            'border-width': `${borderAmount}px`,
        };
        const headerMargin = (size * 0.02);
        const headerStyle = {margin: `${headerMargin}px 0 0 0`,};

        return <div className={`${styles.player}`} style={cardStyle}>
            <img
                className="player-icon"
                src={this.gravatarUrl(player, {size})}
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

    componentDidMount(): void {
        this.fitPlayerName();
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.fitPlayerName();
    }

    private fitPlayerName() {
        const size = this.props.size || 100;
        const maxFontHeight = (size * 0.31);
        const minFontHeight = (size * 0.16);
        this.fitHeaderText(maxFontHeight, minFontHeight);
    }

    private fitHeaderText(maxFontHeight, minFontHeight) {
        const node = ReactDOM.findDOMNode(this);
        let headerNode = node.getElementsByClassName(styles.header)[0];

        headerNode.childNodes.forEach(node => {
            fitty(node, {
                maxSize: maxFontHeight,
                minSize: minFontHeight,
                multiLine: true
            })
        })
    }

    private clickPlayerName(event) {
        if (this.props.disabled) {
            return;
        }

        if (event.stopPropagation) event.stopPropagation();

        window.location.pathname = `/${this.props.tribeId}/player/${this.props.player._id}`
    }

    private gravatarUrl(player: Player, options) {
        if (player && player.imageURL) {
            return player.imageURL;
        } else {
            options['default'] = "retro";
            let email = "";
            if (player) {
                email = player.email ? player.email : player.name || '';
            }

            const codedEmail = md5(email.toLowerCase().trim());

            return `https://www.gravatar.com/avatar/${codedEmail}?default=retro&s=${options.size}`;
        }
    }
}