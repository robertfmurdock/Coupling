import * as React from 'react'
import * as styles from './styles.css'
import Player from "../../../../common/Player";
import {fitHeaderNode} from "../ReactFittyHelper";
import GravatarImage from "../gravatar/GravatarImage";
import {useRef, useLayoutEffect} from "react";

interface PlayerCardProps {
    player: Player,
    tribeId: string,
    disabled: boolean,
    size?: number,
    pathSetter: (string) => void
}

function PlayerGravatarImage({player, size}: { player: Player, size: number }) {
    if (player.imageURL) {
        return <img
            src={player.imageURL}
            className="player-icon"
            width={size}
            height={size}
            alt="icon"
        />
    } else {
        const email = player.email ? player.email : player.name || '';
        return <GravatarImage
            className="player-icon"
            email={email}
            alt="icon"
            options={{size, default: 'retro'}}
        />
    }
}

function PlayerCardHeader(props: PlayerCardProps) {
    const {player, size} = props;
    const cardHeaderRef = useRef(null);

    useLayoutEffect(() => fitPlayerName(size, cardHeaderRef.current));

    return <div className={`player-card-header ${styles.header}`}
                style={headerStyle(size)}
                onClick={(event) => clickPlayerName(event, props)}
                ref={cardHeaderRef}
    >
        <div>
            {player._id ? '' : 'NEW:'}
            {player.name || 'Unknown'}
        </div>
    </div>;
}

function fitPlayerName(size: number, node: any) {
    const maxFontHeight = (size * 0.31);
    const minFontHeight = (size * 0.16);
    fitHeaderNode(node, maxFontHeight, minFontHeight);
}

function headerStyle(size: any) {
    const headerMargin = (size * 0.02);
    return {margin: `${headerMargin}px 0 0 0`,};
}

function clickPlayerName(event, props: PlayerCardProps) {
    if (props.disabled) {
        return;
    }

    if (event.stopPropagation) event.stopPropagation();

    props.pathSetter(`/${props.tribeId}/player/${props.player._id}`)
}

export default function ReactPlayerCard(props: PlayerCardProps) {
    props.size = props.size || 100;
    const {player, size} = props;

    return <div className={`${styles.player} react-player-card`} style={playerCardStyle(size)}>
        <PlayerGravatarImage player={player} size={size}/>
        <PlayerCardHeader {...props} />
    </div>
}

function playerCardStyle(size: number) {
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

