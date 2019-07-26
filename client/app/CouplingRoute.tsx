import * as React from "react";
import ServiceContext from "./ServiceContext";
import {Route} from "react-router-dom";

export default function CouplingRoute(props: { path: string, component }) {
    const {component, path} = props;
    const WrappedComponent = component;
    return (
        <ServiceContext.Consumer>
            {
                coupling => <Route
                    path={path}
                    exact
                    render={({history, match, location}) =>
                        <WrappedComponent
                            coupling={coupling}
                            pathSetter={pathSetter(history)}
                            {...match.params}
                            search={location.search}
                        />}
                />
            }
        </ServiceContext.Consumer>
    )
}

function pathSetter(history) {
    return path => history.push(path);
}
