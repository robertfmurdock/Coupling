import * as React from 'react'
import {useState} from "react";
import * as classNames from 'classnames'
import AnimationContext from '../../AnimationContext'

async function loadData(getDataAsync, setData) {
    const data = await getDataAsync();
    setData(data);
}

enum AnimationState {
    Start,
    Stop
}

export default function reactDataLoadWrapper<P>(WrappedComponent) {
    return (props: { getDataAsync: () => void } & any) => {
        const {getDataAsync = () => ({})} = props;

        const [data, setData] = useState(null);
        const [loadingPromise, setLoading] = useState(null);
        const [animationState, setAnimationState] = useState(AnimationState.Start);

        if (!data && !loadingPromise) {
            setLoading(
                loadData(getDataAsync, setData)
            );
        }

        const shouldStartAnimation = !!data && animationState === AnimationState.Start;

        return <AnimationContext.Consumer>
            {animationsDisabled => {
                return <div className={classNames(
                    {
                        "view-frame": true,
                        "ng-enter": shouldStartAnimation && !animationsDisabled
                    }
                )}
                            onAnimationEnd={() => setAnimationState(AnimationState.Stop)}
                >
                    {
                        data
                            ? <WrappedComponent
                                {...data}
                                {...props}
                                reload={() => {
                                    setData(null);
                                    setLoading(null);
                                }}
                            />
                            : []
                    }
                </div>
            }}
        </AnimationContext.Consumer>
    }
}