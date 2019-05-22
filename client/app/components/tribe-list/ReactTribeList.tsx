import * as React from "react";
import * as Styles from "./styles.css"
import {Tribe} from "../../../../common";
import ReactTribeCard from "../tribe-card/ReactTribeCard";

interface Props {
    tribes: Tribe[]
    pathSetter: (string) => void
}

export default class ReactTribeList extends React.Component<Props> {

    render() {
        const {tribes, pathSetter} = this.props;

        return <div className={Styles.className}>
            <div>
                {tribes.map(tribe => <ReactTribeCard tribe={tribe} pathSetter={pathSetter}/>)}
            </div>
            <div>
                <a type="button" id="new-tribe-button" className="super green button" href="/new-tribe">
                    Add a new tribe!
                </a>
            </div>
        </div>
    }

}