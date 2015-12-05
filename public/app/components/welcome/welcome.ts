/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />

angular.module('coupling.controllers')
    .controller('WelcomeController',
    ['$scope', '$timeout', 'randomizer', function ($scope, $timeout, randomizer) {
        $scope.show = false;
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

        var indexToUse = randomizer.next(candidates.length - 1);
        var choice = candidates[indexToUse];

        $scope.leftCard = choice.leftCard;
        $scope.rightCard = choice.rightCard;
        $scope.proverb = choice.proverb;

        $timeout(function () {
            $scope.show = true;
        }, 0);
    }]);

angular.module("coupling.directives")
    .directive('welcomepage', function () {
        return {
            restrict: 'E',
            controller: 'WelcomeController',
            templateUrl: '/app/components/welcome/welcome.html',
            bindToController: true
        }
    });