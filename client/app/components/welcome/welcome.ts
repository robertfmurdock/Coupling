import {module} from "angular";
import * as services from "../../services";
import * as template from "./welcome.pug";

interface Card {
    name: String
    imagePath: String
}

interface WelcomeCardSet {
    leftCard: Card
    rightCard: Card
    proverb: String
}

const candidates: [WelcomeCardSet] = [{
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

export class WelcomeController {

    private static chooseWelcomeCards(randomizer): WelcomeCardSet {
        const indexToUse = randomizer.next(candidates.length - 1);
        return candidates[indexToUse];
    }

    static $inject = ['$timeout', 'randomizer'];

    public show: boolean;
    public proverb: String;
    public leftCard: Card;
    public rightCard: Card;

    constructor($timeout: angular.ITimeoutService, randomizer: services.Randomizer) {
        this.show = false;
        const choice = WelcomeController.chooseWelcomeCards(randomizer);
        this.leftCard = choice.leftCard;
        this.rightCard = choice.rightCard;
        this.proverb = choice.proverb;
        const self = this;
        $timeout(function () {
            self.show = true;
        }, 0);
    }

}

export default module('coupling.welcome', [])
    .controller('WelcomeController', WelcomeController)
    .directive('welcomepage', function () {
        return {
            restrict: 'E',
            controller: 'WelcomeController',
            controllerAs: 'welcome',
            template: template,
        }
    });