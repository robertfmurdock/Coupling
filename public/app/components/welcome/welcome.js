/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var candidates = [{
        leftCard: {
            name: 'Frodo',
            imagePath: 'frodo-icon.png'
        },
        rightCard: {
            name: 'Sam',
            imagePath: 'samwise-icon.png'
        },
        proverb: 'Together, climb mountains.'
    }, {
        leftCard: {
            name: 'Batman',
            imagePath: 'grayson-icon.png'
        },
        rightCard: {
            name: 'Robin',
            imagePath: 'wayne-icon.png'
        },
        proverb: 'Clean up the city, together.'
    }, {
        leftCard: {
            name: 'Rosie',
            imagePath: 'rosie-icon.png'
        },
        rightCard: {
            name: 'Wendy',
            imagePath: 'wendy-icon.png'
        },
        proverb: 'Team up. Get things done.'
    }];
var WelcomeController = (function () {
    function WelcomeController($timeout, randomizer) {
        this.show = false;
        var choice = WelcomeController.chooseWelcomeCards(randomizer);
        this.leftCard = choice.leftCard;
        this.rightCard = choice.rightCard;
        this.proverb = choice.proverb;
        var self = this;
        $timeout(function () {
            self.show = true;
        }, 0);
    }
    WelcomeController.chooseWelcomeCards = function (randomizer) {
        var indexToUse = randomizer.next(candidates.length - 1);
        return candidates[indexToUse];
    };
    WelcomeController.$inject = ['$timeout', 'randomizer'];
    return WelcomeController;
})();
angular.module('coupling.controllers')
    .controller('WelcomeController', WelcomeController);
angular.module("coupling.directives")
    .directive('welcomepage', function () {
    return {
        restrict: 'E',
        controller: 'WelcomeController',
        controllerAs: 'welcome',
        templateUrl: '/app/components/welcome/welcome.html',
    };
});
//# sourceMappingURL=welcome.js.map