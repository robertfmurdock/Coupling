import {gravatarUrl} from "../player-card/GravatarHelper";
import * as React from "react";

export default function GravatarImage(props: { email, fallback?: string, className?: string, alt: string, options: { default: string; size: any } }) {

    const {email, fallback, className, alt, options} = props;
    let src = myGravatarUrl(options, email, fallback);
    return <img src={src} width={options.size} height={options.size} alt={alt} className={className}/>;
}

function myGravatarUrl(options, email: string, fallback: string) {
    if (email || !fallback) {
        return gravatarUrl(email, options);
    } else {
        return fallback;
    }
}
