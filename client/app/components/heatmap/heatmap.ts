import {module, IController} from "angular";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactHeatmap from "./ReactHeatmap";

class HeatmapController implements IController {

    static $inject = ['$element', '$scope'];

    public data: any;

    constructor($element, $scope) {
        connectReactToNg({
            component: ReactHeatmap,
            props: () => ({data: this.data}),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "data"
        });
    }
}

export default module('coupling.heatmap', [])
    .directive('heatmap', function () {
        return {
            template: '<div/>',
            controller: HeatmapController,
            controllerAs: 'me',
            bindToController: true,
            scope: {
                data: '=',
            },
        }
    });