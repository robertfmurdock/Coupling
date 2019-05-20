import * as styles from "./player-card/styles.css";
import * as ReactDOM from "react-dom";
import fitty from "fitty";

export function fitPlayerName(component: any, size: any) {
    const maxFontHeight = (size * 0.31);
    const minFontHeight = (size * 0.16);
    fitHeaderText(maxFontHeight, minFontHeight, component);
}

function fitHeaderText(maxFontHeight, minFontHeight, component) {
    const node = ReactDOM.findDOMNode(component);
    let headerNode = node.getElementsByClassName(styles.header)[0];

    headerNode.childNodes.forEach(node => {
        fitty(node, {
            maxSize: maxFontHeight,
            minSize: minFontHeight,
            multiLine: true
        })
    })
}