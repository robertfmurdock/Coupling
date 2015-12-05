/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />

interface Card {
    name: String
    imagePath : String
}

interface CardPair {
    leftCard: Card
    rightCard: Card
    proverb: String
}

var candidates:[CardPair] = [{
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

class WelcomeController {
    static $inject = ['$timeout', 'randomizer'];

    public show:boolean;
    public proverb:String;
    public leftCard:Card;
    public rightCard:Card;

    constructor($timeout:angular.ITimeoutService, randomizer:Randomizer) {
        this.show = false;
        var indexToUse = randomizer.next(candidates.length - 1);
        var choice = candidates[indexToUse];

        this.leftCard = choice.leftCard;
        this.rightCard = choice.rightCard;
        this.proverb = choice.proverb;
        var self = this;
        $timeout(function () {
            self.show = true;
        }, 0);
    }
}

angular.module('coupling.controllers')
    .controller('WelcomeController', WelcomeController);

angular.module("coupling.directives")
    .directive('welcomepage', function () {
        return {
            restrict: 'E',
            controller: 'WelcomeController',
            controllerAs: 'welcome',
            templateUrl: '/app/components/welcome/welcome.html',
        }
    });