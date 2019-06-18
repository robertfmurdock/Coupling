import * as React from "react";
import {useLayoutEffect, useRef} from "react";
import * as Styles from './styles.css'
import * as classNames from 'classnames'
import flatten from "ramda/es/flatten";
import {select} from "d3-selection";
import {rgb} from "d3-color";
import {interpolateRgbBasis} from "d3-interpolate";

const colorSuggestions = [
    rgb("#2c7bb6"),
    rgb("#00a6ca"),
    rgb("#00ccbc"),
    rgb("#90eb9d"),
    rgb("#ffff8c"),
    rgb("#f9d057"),
    rgb("#f29e2e"),
    rgb("#e76818"),
    rgb("#d7191c")
];

const colorInterpolator = interpolateRgbBasis(colorSuggestions);

function renderD3Heatmap(element, data) {
    select(element)
        .selectAll("div")
        .data(data)
        .enter().append(function () {
        const cellElement = document.createElement('div');
        cellElement.setAttribute('class', Styles.cell);
        return cellElement;
    })
        .style("background-color", function (dataNumber: number) {
            if (dataNumber === null) {
                return '#EEE'
            }
            const percentage = dataNumber / 10;
            return colorInterpolator(percentage);
        });
}

interface Props {
    data,
    className?: string
}

export default function ReactHeatmap(props: Props) {
    const {data, className} = props;

    const rowSize = data.length * 90;
    const heatmapStyle = {
        width: `${rowSize}px`,
        height: `${rowSize}px`
    };

    const ref = useRef(null);

    useLayoutEffect(() => renderD3Heatmap(ref.current, flatten(data)));

    return <div ref={ref} className={classNames(Styles.heatmap, className)} style={heatmapStyle}/>
}