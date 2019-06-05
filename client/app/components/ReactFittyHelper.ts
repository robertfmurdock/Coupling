import * as ReactDOM from "react-dom";
import fitty from "fitty";

export function fitHeaderNode(headerNode, maxFontHeight, minFontHeight) {
    headerNode.childNodes.forEach(node => {
        fitty(node, {
            maxSize: maxFontHeight,
            minSize: minFontHeight,
            multiLine: true
        })
    })
}

export function fitHeaderTextOnCardComponent(maxFontHeight, minFontHeight, component, className: any) {
    const node = ReactDOM.findDOMNode(component);
    let headerNode = node.getElementsByClassName(className)[0];

    fitHeaderNode(headerNode, maxFontHeight, minFontHeight);
}