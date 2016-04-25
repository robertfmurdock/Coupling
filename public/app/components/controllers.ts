import * as _ from 'underscore'

angular.module('coupling.controllers', ['coupling.services', 'ngFitText'])
    .config(['fitTextConfigProvider', function (fitTextConfigProvider) {
        fitTextConfigProvider.config = {
            debounce: _.debounce,
            delay: 1000
        };
    }]);

angular.module("coupling.directives", []);