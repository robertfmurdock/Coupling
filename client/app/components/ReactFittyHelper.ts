import * as ReactDOM from "react-dom";
import fitty from "fitty";

export function fitHeaderText(maxFontHeight, minFontHeight, component, className: any) {
    const node = ReactDOM.findDOMNode(component);
    let headerNode = node.getElementsByClassName(className)[0];

    headerNode.childNodes.forEach(node => {
        fitty(node, {
            maxSize: maxFontHeight,
            minSize: minFontHeight,
            multiLine: true
        })
    })
}