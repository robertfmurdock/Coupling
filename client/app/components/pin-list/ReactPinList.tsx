import * as React from "react";
import * as classNames from 'classnames'
import Tribe from "../../../../common/Tribe";

function pinThing(pin) {
    return <span className={"pin"}>
                <i className={classNames(["pin-icon",
                    "fa",
                    "fa-fw",
                    "fa-d2",
                    "fa-2x",
                    pin.icon
                ])}/>
                <input type={"text"} value={pin.name}/>
                <input type={"text"} value={pin.icon}/>
            </span>;
}

export default function (props: {
    tribe: Tribe,
    pins: { name: string, icon: string }[]
}) {
    const {tribe, pins} = props;
    return <div className={"pin-list-frame"}>
        <div id={"pin-listing"}>
            {pins.map(pin => pinThing(pin))}
        </div>
        <a className={"large orange button"} href={`/${tribe.id}/pin/new`}>
            Add a new pin.
        </a>
    </div>
}