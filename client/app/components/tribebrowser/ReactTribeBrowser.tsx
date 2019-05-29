import * as React from "react";
import * as Styles from './styles.css'
import * as classNames from 'classnames'
import ReactTribeCard from "../tribe-card/ReactTribeCard";
import {Tribe} from "../../../../common";

interface Props {
    tribe: Tribe,
    pathSetter: (string) => void
}

export default class ReactTribeBrowser extends React.Component<Props> {

    render() {
        const {pathSetter, tribe} = this.props;

        return <div className={Styles.className}>
            <span>
                <ReactTribeCard pathSetter={pathSetter} tribe={tribe} size={50}/>
                <h1>{tribe.name}</h1>
            </span>
            <span>
                <span>
                    <a className={classNames(Styles.statisticsButton, 'statistics-button', 'large gray button')}
                       href={`/${tribe.id}/statistics`}>
                        <text className="icon-button-text">Statistics</text>
                    </a>
                    <a id="tribe-select-button" className="large gray button" href="/tribes">
                        <i className="fa fa-arrow-circle-up"/>
                        <text className="icon-button-text">Tribe select</text>
                    </a>
                </span>
                <span>
                    <a id="logout-button" className="large red button" href="/logout">
                        <i className="fa fa-sign-out"/>
                        <text className="icon-button-text">Sign Out</text>
                    </a>
                </span>
            </span>
        </div>
    }
}