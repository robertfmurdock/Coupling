import {module} from "angular";
import * as d3 from "d3";
import * as Styles from "./styles.css";
import * as template from "./heatmap.pug";

const colorSuggestions = [
    d3.rgb("#2c7bb6"),
    d3.rgb("#00a6ca"),
    d3.rgb("#00ccbc"),
    d3.rgb("#90eb9d"),
    d3.rgb("#ffff8c"),
    d3.rgb("#f9d057"),
    d3.rgb("#f29e2e"),
    d3.rgb("#e76818"),
    d3.rgb("#d7191c")
];

const colorInterpolator = d3.interpolateRgbBasis(colorSuggestions);

export default module('coupling.heatmap', [])
    .directive('heatmap', function () {
        return {
            template: template,
            controller: function () {
                this.playerCount = 10;
                const rowSize = this.playerCount * 70;
                this.Styles = Styles;
                this.heatmapStyle = {
                    width: `${rowSize}px`,
                    height: `${rowSize}px`
                }
            },
            controllerAs: 'me',
            bindToController: true,

            link: function (scope, element, attributes, controller: any) {
                const playerCount = controller.playerCount;
                const range = playerCount * playerCount;

                const select = element[0].querySelector('div[ng-class="me.Styles.heatmap"]');
                d3.select(select)
                    .selectAll("div")
                    .data(d3.range(range))
                    .enter().append(function () {
                    const cellElement = document.createElement('div');
                    cellElement.setAttribute('class', Styles.cell);
                    return cellElement;
                })
                    .style("background-color", function (dataNumber, index, elements) {
                        const percentage = dataNumber / elements.length;
                        return colorInterpolator(percentage);
                    });
            }
        }
    });