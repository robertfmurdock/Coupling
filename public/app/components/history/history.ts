/// <reference path="../../services.ts" />

class HistoryController {

    tribe:Tribe;

    removeEntry(entry) {
        if (confirm("Are you sure you want to delete these pair assignments?")) {
            entry.$remove();
        }
    }

}

angular.module("coupling.controllers")
    .controller('HistoryController', HistoryController);

angular.module("coupling.directives")
    .directive('history', () => {
        return {
            controller: HistoryController,
            controllerAs: 'history',
            bindToController: true,
            scope: {
                tribe: '=',
                history: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/history/history.html'
        }
    });