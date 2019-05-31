import {module, IController} from "angular";
import {rgb} from "d3-color";
import {interpolateRgbBasis} from "d3-interpolate";
import {select} from "d3-selection";
import * as Styles from "./styles.css";
import flatten from "ramda/es/flatten";
import * as template from "./heatmap.pug";

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

class HeatmapController implements IController {

    public data: any;
    public heatmapStyle: any;
    public Styles: any;

    $onInit() {
        const rowSize = this.data.length * 90;
        this.Styles = Styles;
        this.heatmapStyle = {
            width: `${rowSize}px`,
            height: `${rowSize}px`
        }
    }
}


export default module('coupling.heatmap', [])
    .directive('heatmap', function () {
        return {
            template: template,
            controller: HeatmapController,
            controllerAs: 'me',
            bindToController: true,
            scope: {
                data: '=',
            },
            link: function (scope, element, attributes, controller: any) {
                const data = flatten(controller.data);

                select(element[0].querySelector('div[ng-class="me.Styles.heatmap"]'))
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
        }
    });