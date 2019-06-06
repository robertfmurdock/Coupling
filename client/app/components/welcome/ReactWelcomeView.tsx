import * as React from 'react'
import {useRef, useState} from 'react'
import * as Styles from "./styles.css";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import ReactLoginChooser from "../login-chooser/ReactLoginChooser";
import * as services from "../../services";
import fitty from "fitty";
import {useLayoutEffect} from "react";

interface Card {
    name: string
    imagePath: string
}

interface WelcomeCardSet {
    leftCard: Card
    rightCard: Card
    proverb: string
}

const candidates: WelcomeCardSet[] = [{
    leftCard: {
        name: 'Frodo',
        imagePath: 'frodo-icon.png'
    },
    rightCard: {
        name: 'Sam',
        imagePath: 'samwise-icon.png'
    },
    proverb: 'Together, climb mountains.'
}, {
    leftCard: {
        name: 'Batman',
        imagePath: 'grayson-icon.png'
    },
    rightCard: {
        name: 'Robin',
        imagePath: 'wayne-icon.png'
    },
    proverb: 'Clean up the city, together.'
}, {
    leftCard: {
        name: 'Rosie',
        imagePath: 'rosie-icon.png'
    },
    rightCard: {
        name: 'Wendy',
        imagePath: 'wendy-icon.png'
    },
    proverb: 'Team up. Get things done.'
}];

function chooseWelcomeCards(randomizer): WelcomeCardSet {
    const indexToUse = randomizer.next(candidates.length - 1);
    return candidates[indexToUse];
}

let makePlayerForCard = function (card: Card) {
    return {
        _id: card.name,
        name: card.name,
        imageURL: `/images/icons/players/${card.imagePath}`
    };
};

function WelcomeTitle() {
    const welcomeTitleRef = useRef(null);
    useLayoutEffect(() => {
        fitty(welcomeTitleRef.current, {
            maxSize: 75,
            minSize: 5,
            multiLine: false
        });
    });

    return <div className={Styles.welcomeTitle} ref={welcomeTitleRef}>
        Coupling!
    </div>
}


export default function (props: { randomizer: services.Randomizer }) {
    let tribeId = "welcome";
    const {randomizer} = props;

    const [show, setShow] = useState(false);
    const [showLoginChooser, setShowLoginChooser] = useState(false);
    const pairRef = useRef(null);

    if (!pairRef.current) {
        const choice = chooseWelcomeCards(randomizer);
        const leftPlayer = makePlayerForCard(choice.leftCard);
        const rightPlayer = makePlayerForCard(choice.rightCard);
        const proverb = choice.proverb;
        pairRef.current = {leftPlayer, rightPlayer, proverb}
    }

    setTimeout(() => setShow(true), 0);

    const {leftPlayer, rightPlayer, proverb} = pairRef.current;

    let hiddenTag = show ? "" : `${Styles.hidden}`;

    return <div className={`${Styles.className} ${hiddenTag}`}>
        <div>
            <span className={Styles.welcome}>
<WelcomeTitle/>
                <div>
                    <div className={Styles.welcomePair}>
                        <ReactPlayerCard className={`left ${Styles.playerCard}`} player={leftPlayer} size={100}
                                         tribeId={tribeId}
                                         disabled={true}/>
                        <ReactPlayerCard className={`right  ${Styles.playerCard}`} player={rightPlayer} size={100}
                                         tribeId={tribeId}
                                         disabled={true}/>
                    </div>
                </div>
                <div className={`${Styles.welcomeProverb} ${hiddenTag}`}>
                    {proverb}
                </div>
                </span>
        </div>
        <div>
            <div className={Styles.enterButtonContainer}>
                {
                    showLoginChooser
                        ? <ReactLoginChooser/>
                        : <a className={`${Styles.enterButton} ${hiddenTag} enter-button super pink button`}
                             onClick={() => setShowLoginChooser(true)}
                             target={'_self'}> Come on in! </a>
                }
            </div>
        </div>
    </div>
}