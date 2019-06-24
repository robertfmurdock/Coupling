import * as React from "react";
import {useState} from "react";
import merge from 'ramda/es/merge'
import equals from 'ramda/es/equals'
import ReactTribeCard from "../tribe-card/ReactTribeCard";
import ReactPlayerCard from "../player-card/ReactPlayerCard";
import ReactPlayerRoster from "../player-roster/ReactPlayerRoster";
import Badge from "../../../../common/Badge";
import {Coupling} from "../../services";
import Tribe from "../../../../common/Tribe";
import Player from "../../../../common/Player";

const useForm = (callback, initialValues) => {
    const [values, setValues] = useState(initialValues);

    const handleSubmit = (event) => {
        if (event) event.preventDefault();
        callback();
    };

    const handleChange = (event) => {
        event.persist();
        setValues(values => ({...values, [event.target.name]: event.target.value}));
    };

    return [
        values,
        handleChange,
        handleSubmit,
    ]
};

interface Props {
    tribe: Tribe
    player: Player
    players: Player[]
    pathSetter: (url: string) => void
    coupling: Coupling
    locationChanger: (callback: () => void) => void
    reloader?: () => void
}

const defaultPlayerAttributes = {badge: Badge.Default};

function BadgeConfig(props: { tribe: Tribe, updatedPlayer: Player, onChange }) {
    const {tribe, updatedPlayer, onChange} = props;
    return <div className={"badge-config"}>
        <div>
            <label htmlFor="default-badge-radio">{tribe.defaultBadgeName}</label>
            <input
                name="badge"
                id={"default-badge-radio"}
                type={"radio"}
                value={Badge.Default}
                checked={updatedPlayer.badge == Badge.Default}
                onChange={onChange}
            />
        </div>
        <div>
            <label htmlFor="alt-badge-radio">{tribe.alternateBadgeName}</label>
            <input
                name="badge"
                id={"alt-badge-radio"}
                type={"radio"}
                value={Badge.Alternate}
                checked={updatedPlayer.badge == Badge.Alternate}
                onChange={onChange}
            />
        </div>
    </div>;
}

function CallSignConfig(props: { updatedPlayer: Player, onChange }) {
    const {updatedPlayer, onChange} = props;
    return <div>
        <div>
            <label htmlFor="adjective-input">Call-Sign Adjective</label>
            <input
                name="callSignAdjective"
                id={"adjective-input"}
                type={"text"}
                list="callSignAdjectiveOptions"
                value={updatedPlayer.callSignAdjective}
                onChange={onChange}
            />
            <datalist id="callSignAdjectiveOptions"/>
        </div>
        <div>
            <label htmlFor="noun-input">Call-Sign Noun</label>
            <input
                name="callSignNoun"
                id={"noun-input"}
                type={"text"}
                list="callSignNounOptions"
                value={updatedPlayer.callSignNoun}
                onChange={onChange}
            >
            </input>
            <datalist id="callSignNounOptions"/>
        </div>
    </div>;
}

function PlayerConfigForm(props: { onSubmit, player: Player, onChange, tribe: Tribe, removePlayer }) {
    const {onSubmit, player, onChange, tribe, removePlayer} = props;

    const [isSaving, setIsSaving] = useState(false);

    return <form name="playerForm" onSubmit={event => {
        setIsSaving(true);
        onSubmit(event);
    }}>
        <div>
            <label htmlFor="player-name">Name</label>
            <input name="name" id={"player-name"} type={"text"} value={player.name}
                   onChange={onChange}/>
        </div>
        <div>
            <label htmlFor="player-email">Email</label>
            <input name="email" id={"player-email"} type={"text"} value={player.email}
                   onChange={onChange}/>
        </div>
        {
            tribe.callSignsEnabled
                ? <CallSignConfig updatedPlayer={player} onChange={onChange}/>
                : []
        }
        {
            tribe.badgesEnabled
                ? <BadgeConfig tribe={tribe} updatedPlayer={player} onChange={onChange}/>
                : []
        }
        <button
            id={"save-player-button"}
            type={"submit"}
            className={"large blue button save-button"}
            tabIndex={0}
            value={"Save"}
            disabled={isSaving}>
            Save
        </button>
        {
            player._id
                ? <div className={"small red button delete-button"} onClick={removePlayer}>Retire</div>
                : []
        }
    </form>;
}

export default function ReactPlayerConfig(props: Props) {
    const {tribe, players, pathSetter, coupling, locationChanger, reloader = () => window.location.reload()} = props;
    const player = merge(defaultPlayerAttributes, props.player);

    const [values, handleChange, handleSubmit] = useForm(savePlayer, player);
    const updatedPlayer = merge(player, values);

    let promptIsUp = false;
    locationChanger(async () => {
        if (!equals(updatedPlayer, player) && !promptIsUp) {
            promptIsUp = true;
            const answer = confirm("You have unsaved data. Would you like to save before you leave?");
            if (answer) {
                await coupling.savePlayer(updatedPlayer, tribe.id);
            }
        }
    });

    async function savePlayer() {
        await coupling.savePlayer(updatedPlayer, tribe.id);
        reloader();
    }

    async function removePlayer() {
        if (confirm("Are you sure you want to delete this player?")) {
            await coupling.removePlayer(player, tribe.id);
            pathSetter(`/${tribe.id}/pairAssignments/current`);
        }
    }

    return <div className={"react-player-config"}>
        <div>
            <div id={"tribe-browser"}>
                <ReactTribeCard tribe={tribe} pathSetter={pathSetter}/>
            </div>
            <span id={"player-view"}>
            <span className={"player"}>
                <PlayerConfigForm
                    player={updatedPlayer}
                    tribe={tribe}
                    onChange={handleChange}
                    onSubmit={handleSubmit}
                    removePlayer={removePlayer}
                />
            </span>
            <ReactPlayerCard player={updatedPlayer} tribeId={tribe.id} size={250}/>
        </span>
        </div>
        <ReactPlayerRoster players={players} tribeId={tribe.id} pathSetter={pathSetter} className={"player-roster"}/>
    </div>
}