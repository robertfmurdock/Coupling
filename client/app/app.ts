import "angular";
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
import newPlayerRoute from "./routes/NewPlayerRoute";
import pinRoute from "./routes/PinRoute";
import historyRoute from "./routes/HistoryRoute";
import editTribeRoute from "./routes/EditTribeRoute";
import prepareTribeRoute from "./routes/PrepareTribeRoute";
import newTribeRoute from "./routes/NewTribeRoute";
import tribeListRoute from "./routes/TribeListRoute";
import {module} from "angular";
import IRoute = ng.route.IRoute
import IRouteProvider = ng.route.IRouteProvider
import IResource = ng.resource.IResource

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
        .when('/', {redirectTo: '/tribes/'})
        .when('/welcome', () => {
        })
        .when('/tribes/', tribeListRoute)
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
        .when('/:tribeId/statistics', statisticsRoute)
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