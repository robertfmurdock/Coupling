import {module} from "angular";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactPlayerRoster from "./ReactPlayerRoster";

export default module("coupling.playerRoster", [])
    .directive('playerRoster', () => {
        return {
            scope: {
                tribe: '=',
                players: '=',
                label: '=?'
            },
            bindToController: true,
            restrict: 'E',
            template: "<div/>",
            controller: ["$location", "$scope", "$element",
                function ($location, $scope, $element) {
                    connectReactToNg({
                        component: ReactPlayerRoster,
                        props: () => ({
                            label: this.label,
                            players: this.players,
                            tribeId: this.tribe.id
                        }),
                        domNode: $element[0],
                        $scope: $scope,
                        watchExpression: "players",
                        $location: $location
                    });
                }
            ]
        }
    });