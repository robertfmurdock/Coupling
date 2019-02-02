import {IController, module} from "angular";
import * as template from "./login-chooser.pug";
import * as styles from "./styles.css";

import GoogleSignIn from "../../GoogleSignIn";

export class LoginChooserController implements IController {

    styles: any;

    constructor() {
        this.styles = styles;
    }

    async googleSignIn() {
        await GoogleSignIn.signIn();
    }

    microsoftSignIn() {
        window.location.pathname = "/microsoft-login"
    }

}

export default module('coupling.loginChooser', [])
    .controller('LoginChooserController', LoginChooserController)
    .directive('loginchooser', () => {
        return {
            template: template,
            restrict: 'E',
            controller: 'LoginChooserController',
            controllerAs: 'loginChooser',
            bindToController: true
        }
    });