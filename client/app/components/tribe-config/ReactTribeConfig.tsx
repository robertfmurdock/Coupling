import * as Styles from './styles.css'
import * as React from "react";
import * as classNames from 'classnames'
import ReactTribeCard from "../tribe-card/ReactTribeCard";
import {Tribe} from '../../../../common';
import PairingRule from '../../../../common/PairingRule';
import flip from "ramda/es/flip";
import merge from "ramda/es/merge";
import {Coupling} from "../../services";
import {useState} from "react";

const defaults = flip(merge);

interface Props {
    tribe: Tribe
    pathSetter: (string) => void
    isNew: boolean
    coupling: Coupling
}

const pairingRules = [
    {id: PairingRule.LongestTime, description: "Prefer Longest Time"},
    {id: PairingRule.PreferDifferentBadge, description: "Prefer Different Badges (Beta)"},
];

export function TribeForm(props: { tribe: Tribe, isNew: boolean, handleChange, pathSetter }) {
    const {tribe, isNew, handleChange, pathSetter} = props;
    return <div>
            <span>
                <ul className={Styles.editor}>
                    <li>
                        <label htmlFor={"tribe-nome"}>Name</label>
                        <input
                            id={"tribe-name"}
                            type={"text"}
                            name="name"
                            value={tribe.name}
                            placeholder={"Enter the tribe name here"}
                            onChange={handleChange}
                        />
                        <span>The full tribe name!</span>
                    </li>
                    <li>
                        <label htmlFor={"tribe-email"}>Email</label>
                        <input
                            id={"tribe-email"}
                            type={"text"}
                            name="email"
                            value={tribe.email}
                            placeholder={"Enter the tribe email here"}
                            onChange={handleChange}
                        />
                        <span>The tribe email address - Attach a Gravatar to this to cheese your tribe icon.</span>
                    </li>
                    {
                        isNew
                            ? <li>
                                <label htmlFor={"tribe-id"}>Unique Id</label>
                                <input
                                    id={"tribe-id"}
                                    type={"text"}
                                    name="id"
                                    value={tribe.id}
                                    onChange={handleChange}
                                />
                            </li>
                            : []
                    }
                    <li>
                        <label htmlFor={"call-sign-checkbox"}>Enable Call Signs</label>
                        <input
                            id={"call-sign-checkbox"}
                            type={"checkbox"}
                            name="callSignsEnabled"
                            checked={tribe.callSignsEnabled}
                            onChange={handleChange}
                        />
                        <span>Every Couple needs a Call Sign. Makes things more fun!</span>
                    </li>
                    <li>
                        <label htmlFor={"badge-checkbox"}>Enable Badges</label>
                        <input
                            id={"badge-checkbox"}
                            type={"checkbox"}
                            name="badgesEnabled"
                            checked={tribe.badgesEnabled}
                            onChange={handleChange}
                        />
                        <span>Advanced users only: this lets you divide your tribe into two groups.</span>
                    </li>
                    <li>
                        <label htmlFor={"default-badge-name"}>Default Badge Name</label>
                        <input
                            id={"default-badge-name"}
                            type={"text"}
                            name="defaultBadgeName"
                            value={tribe.defaultBadgeName}
                            onChange={handleChange}
                        />
                        <span>The first badge a player can be given. When badges are enabled, existing players default to having this badge.</span>
                    </li>
                    <li>
                        <label htmlFor={"alt-badge-name"}>Alt Badge Name</label>
                        <input
                            id={"alt-badge-name"}
                            type={"text"}
                            name="alternateBadgeName"
                            value={tribe.alternateBadgeName}
                            onChange={handleChange}
                        />
                        <span>The other badge a player can be given. A player can only have one badge at a time.</span>
                    </li>
                    <li>
                        <label htmlFor={"pairing-rule"}>Pairing Rule</label>
                        <select
                            id={"pairing-rule"}
                            value={tribe.pairingRule}
                            name="pairingRule"
                            onChange={handleChange}
                        >{pairingRules.map(rule =>
                            <option value={rule.id} label={rule.description}/>)
                        }
                        </select>
                        <span>Advanced users only: This rule affects how players are assigned.</span>
                    </li>
                </ul>
            </span>
        <ReactTribeCard tribe={tribe} pathSetter={pathSetter}/>
    </div>;
}

export default function ReactTribeConfig(props: Props) {
    const {pathSetter, isNew, coupling} = props;
    const tribe = defaults(props.tribe, {
        pairingRule: PairingRule.LongestTime,
        defaultBadgeName: 'Default',
        alternateBadgeName: 'Alternate',
    });
    const [values, setValues] = useState(tribe);
    const updatedTribe = defaults(values, tribe);

    const handleChange = (event) => {
        event.persist();
        if (event.target.name === 'pairingRule') {
            setValues(values => ({...values, pairingRule: Number(event.target.value)}));
        } else {
            setValues(values => ({...values, [event.target.name]: event.target.value}));
        }
    };

    async function clickSaveButton() {
        await coupling.saveTribe(updatedTribe);
        pathSetter("/tribes");
    }

    async function onDeleteClick() {
        await coupling.deleteTribe(tribe.id);
        pathSetter("/tribes");
    }

    return <div className={classNames("tribe-config", Styles.className)}>
        <div>
            <h1>Tribe Configuration</h1>
        </div>
        <TribeForm
            tribe={updatedTribe}
            isNew={isNew}
            handleChange={handleChange}
            pathSetter={pathSetter}
        />
        <div>
            <input
                id={"save-tribe-button"}
                type={"button"}
                className={"super blue button save-button"}
                onClick={clickSaveButton}
                tabIndex={0}
                value={"Save"}
            />
            {
                !isNew
                    ? <div
                        className={"small red button delete-tribe-button"}
                        onClick={onDeleteClick}>
                        Retire
                    </div>
                    : []
            }
        </div>
    </div>
}