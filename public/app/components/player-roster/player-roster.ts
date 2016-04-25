export default angular.module("coupling.playerRoster", [])
    .directive('playerRoster', () => {
        return {
            scope: {
                tribe: '=',
                players: '=',
                label: '=?'
            },
            restrict: 'E',
            templateUrl: '/app/components/player-roster/player-roster.html'
        }
    });