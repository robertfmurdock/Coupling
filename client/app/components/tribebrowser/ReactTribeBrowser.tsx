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
                        <span className="icon-button-text">Statistics</span>
                    </a>
                    <a id="tribe-select-button" className="large gray button" href="/tribes/">
                        <i className="fa fa-arrow-circle-up"/>
                        <span className="icon-button-text">Tribe select</span>
                    </a>
                </span>
                <span>
                    <a id="logout-button" className="large red button" href="/logout">
                        <i className="fa fa-sign-out"/>
                        <span className="icon-button-text">Sign Out</span>
                    </a>
                </span>
            </span>
        </div>
    }
}