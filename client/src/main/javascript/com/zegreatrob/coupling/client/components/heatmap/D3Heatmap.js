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

// noinspection JSUnusedGlobalSymbols
export function renderD3Heatmap(element, data, cellClassName) {
    select(element)
        .selectAll("div")
        .data(data)
        .enter().append(function () {
        const cellElement = document.createElement('div');
        cellElement.setAttribute('class', cellClassName);
        return cellElement;
    })
        .style("background-color", function (dataNumber) {
            if (dataNumber === null) {
                return '#EEE'
            }
            const percentage = dataNumber / 10;
            return colorInterpolator(percentage);
        });
}
