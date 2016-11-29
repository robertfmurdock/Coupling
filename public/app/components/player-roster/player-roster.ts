import * as template from './player-roster.pug'
export default angular.module("coupling.playerRoster", [])
    .directive('playerRoster', () => {
        return {
            scope: {
                tribe: '=',
                players: '=',
                label: '=?'
            },
            restrict: 'E',
            template: template
        }
    });