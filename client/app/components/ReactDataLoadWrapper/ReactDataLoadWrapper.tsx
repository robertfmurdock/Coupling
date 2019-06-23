import * as React from 'react'
import {useState} from "react";

async function loadData(getDataAsync, setData) {
    const data = await getDataAsync();
    setData(data);
}

export default function reactDataLoadWrapper(WrappedComponent) {
    return (props: { getDataAsync }) => {
        const {getDataAsync} = props;

        const [data, setData] = useState(null);
        const [loadingPromise, setLoading] = useState(null);

        if (data) {
            return <WrappedComponent {...data} {...props} />;
        } else {
            if (!loadingPromise) {
                setLoading(
                    loadData(getDataAsync, setData)
                );
            }
            return <div/>
        }
    }
}