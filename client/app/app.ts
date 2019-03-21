import * as angular from "angular";
import {module} from "angular";
// @ts-ignore
import * as logging from 'logging'
import "angular-gravatar";
import "angular-route";
import "ng-fittext";
import "prefixfree";
import "angular-native-dragdrop";
import "angular-websocket";
import "./filters";
import "./animations";
import "./components/components";
import "./../stylesheets/style.scss";
import "./../stylesheets/animations.scss";
import "./services";
import statisticsRoute from "./routes/StatisticsRoute";
import newPairAssignmentsRoute from "./routes/NewPairAssignmentsRoute";
import currentPairAssignmentsRoute from "./routes/CurrentPairAssignmentsRoute";
import editPlayerRoute from "./routes/EditPlayerRoute";
import retiredPlayerRoute from "./routes/RetiredPlayerRoute";
import newPlayerRoute from "./routes/NewPlayerRoute";
import pinRoute from "./routes/PinRoute";
import historyRoute from "./routes/HistoryRoute";
import editTribeRoute from "./routes/EditTribeRoute";
import prepareTribeRoute from "./routes/PrepareTribeRoute";
import newTribeRoute from "./routes/NewTribeRoute";
import tribeListRoute from "./routes/TribeListRoute";
import retiredPlayersRoute from "./routes/RetiredPlayersRoute";
import GoogleSignIn from "./GoogleSignIn";
import IRouteProvider = angular.route.IRouteProvider;
import {Coupling} from "./services";

logging.com.zegreatrob.coupling.logging.initializeJasmineLogging(false);

async function bootstrapApp() {
    const isSignedIn = await GoogleSignIn.checkForSignedIn();

    const app = module('coupling', ["ngRoute",
        'ngFitText',
        'ui.gravatar',
        'ang-drag-drop',
        'coupling.component',
        'coupling.filters',
        'coupling.animations']);

    app.config(['$locationProvider', function ($locationProvider) {
        $locationProvider.html5Mode({
            enabled: true,
            requireBase: false
        });
    }]);

    app.config(['$routeProvider', function (routeProvider: IRouteProvider) {
        routeProvider
            .when('/welcome', {template: '<welcomepage />'});

        if (!isSignedIn) {
            routeProvider
                .otherwise({redirectTo: '/welcome'});
        } else {
            routeProvider
                .when('/', {redirectTo: '/tribes/'})
                .when('/tribes/', tribeListRoute)
                .when('/logout/', {
                    resolveRedirectTo: ['Coupling', async function (Coupling: Coupling) {
                        await Promise.all([
                                Coupling.logout(),
                                GoogleSignIn.signOut()
                            ]
                        );
                        return '/welcome';
                    }],
                })
                .when('/new-tribe/', newTribeRoute)
                .when('/:tribeId/', {redirectTo: '/:tribeId/pairAssignments/current/'})
                .when('/:tribeId/prepare/', prepareTribeRoute)
                .when('/:tribeId/edit/', editTribeRoute)
                .when('/:tribeId/history/', historyRoute)
                .when('/:tribeId/pins', pinRoute)
                .when('/:tribeId/pairAssignments/current/', currentPairAssignmentsRoute)
                .when('/:tribeId/pairAssignments/new/', newPairAssignmentsRoute)
                .when('/:tribeId/player/new/', newPlayerRoute)
                .when('/:tribeId/player/:id/', editPlayerRoute)
                .when('/:tribeId/retired-player/:id/', retiredPlayerRoute)
                .when('/:tribeId/statistics', statisticsRoute)
                .when('/:tribeId/players/retired', retiredPlayersRoute)
        }
    }]);

    module('ui.gravatar')
        .config([
            'gravatarServiceProvider',
            function (gravatarServiceProvider) {
                gravatarServiceProvider.defaults = {
                    size: 100,
                    "default": 'mm'
                };
                gravatarServiceProvider.secure = true;
            }
        ]);

    angular.element(() => angular.bootstrap(document, ['coupling']));
}

bootstrapApp()
    .catch(err => console.log(err));