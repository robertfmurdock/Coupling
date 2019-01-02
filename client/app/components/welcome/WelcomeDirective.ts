import * as angular from "angular";
import {module} from "angular";
import * as services from "../../services";
import * as template from "./welcome.pug";
import * as styles from "./styles.css";
import Player from "../../../../common/Player";
import axios from "axios";

interface Card {
    name: string
    imagePath: string
}

interface WelcomeCardSet {
    leftCard: Card
    rightCard: Card
    proverb: string
}

const candidates: WelcomeCardSet[] = [{
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

let makePlayerForCard = function (card: Card) {
    return {
        _id: card.name,
        name: card.name,
        tribe: 'welcome',
        imageURL: `/images/icons/players/${card.imagePath}`
    };
};

export class WelcomeController {

    private static chooseWelcomeCards(randomizer): WelcomeCardSet {
        const indexToUse = randomizer.next(candidates.length - 1);
        return candidates[indexToUse];
    }

    static $inject = ['$timeout', 'randomizer', '$scope'];

    public show: boolean;
    public proverb: String;
    public leftPlayer: Player;
    public rightPlayer: Player;
    public styles: any;

    constructor($timeout: angular.ITimeoutService, randomizer: services.Randomizer, public $scope) {
        this.show = false;
        const choice = WelcomeController.chooseWelcomeCards(randomizer);
        this.leftPlayer = makePlayerForCard(choice.leftCard);
        this.rightPlayer = makePlayerForCard(choice.rightCard);
        this.proverb = choice.proverb;
        this.styles = styles;
        $timeout(() => this.show = true, 0);
    }

    async signIn() {
        const googleAuth = await this.getGoogleAuth();
        const user = await this.getGoogleUser(googleAuth);
        const idToken = user.getAuthResponse().id_token;
        await axios.post(`/auth/google-token`, {idToken: idToken});
        window.location.pathname = "/"
    }

    private async getGoogleAuth() {
        let auth2 = await this.loadGoogleAuth2();

        return await auth2.init({
            // @ts-ignore
            client_id: window.googleClientId
        });
    }

    private async loadGoogleAuth2() : Promise<any> {
        return await new Promise((resolve) => {
            // @ts-ignore
            gapi.load('auth2', function () {
                // @ts-ignore
                resolve(gapi.auth2)
            })
        });
    }

    private async getGoogleUser(googleAuth) {
        const isSignedIn = googleAuth.isSignedIn.get();
        if (!isSignedIn) {
            return await googleAuth.signIn({
                scope: 'profile email'
            });
        } else {
            return await googleAuth.currentUser.get();
        }
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