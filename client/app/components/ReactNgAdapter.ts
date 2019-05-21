import * as React from "react";
import * as ReactDOM from "react-dom";

export function connectReactToNg(options) {
    const {component, props, domNode, $scope, watchExpression, $location} = options;

    const renderReactElement = () => {
        let theProps = props();
        theProps.pathSetter = url => $scope.$apply(() => $location.path(url));

        ReactDOM.render(
            React.createElement(component, theProps),
            domNode
        );
    };
    $scope.$watch(watchExpression, renderReactElement);
    $scope.$on("$destroy", unmountReactElement);

    function unmountReactElement() {
        ReactDOM.unmountComponentAtNode(domNode);
    }
}