import * as React from "react";
import * as ReactDOM from "react-dom";

export function connectReactToNg(options) {
    const {component, props, domNode, $scope, watchExpression} = options;

    const renderReactElement = () => {
        ReactDOM.render(
            React.createElement(component, props()),
            domNode
        );
    };
    $scope.$watch(watchExpression, renderReactElement);
    $scope.$on("$destroy", unmountReactElement);

    function unmountReactElement() {
        ReactDOM.unmountComponentAtNode(domNode);
    }
}