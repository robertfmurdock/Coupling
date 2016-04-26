import PlayerCardController from './PlayerCard'

export default angular.module('coupling.playerCard', [])
    .controller('PlayerCardController', PlayerCardController)
    .directive('playercard', () => {
        return {
            templateUrl: '/app/components/player-card/playercard.html',
            restrict: 'E',
            controller: 'PlayerCardController',
            controllerAs: 'playerCard',
            scope: {
                player: '=',
                size: '=?'
            },
            bindToController: true
        }
    });