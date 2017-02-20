import {module} from "angular";
import * as d3 from "d3";
import * as Styles from "./styles.css";
import * as template from "./heatmap.pug";
import * as _ from 'underscore'

import IController = angular.IController;

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

class HeatmapController implements IController {

    public data: any;
    public heatmapStyle: any;
    public Styles: any;

    $onInit() {
        const rowSize = this.data.length * 70;
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
                const select = element[0].querySelector('div[ng-class="me.Styles.heatmap"]');
                const data = _.flatten(controller.data);

                d3.select(select)
                    .selectAll("div")
                    .data(data)
                    .enter().append(function () {
                    const cellElement = document.createElement('div');
                    cellElement.setAttribute('class', Styles.cell);
                    return cellElement;
                })
                    .style("background-color", function (dataNumber) {
                        if(dataNumber === null){
                            return '#EEE'
                        }
                        const percentage = dataNumber / 10;
                        return colorInterpolator(percentage);
                    });
            }
        }
    });