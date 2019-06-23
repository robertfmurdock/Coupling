import History from "./history/history";
import PairAssignments from "./pair-assignments/PairAssignmentsDirective";
import HeatMap from './heatmap/heatmap'
import PinList from "./pin-list/pin-list";
import PlayerConfig from "./player-config/player-config";
import PlayerRoster from "./player-roster/player-roster";
import Prepare from "./prepare/PrepareDirective";
import ServerMessageDirective from "./server-message/ServerMessageDirective";
import TribeCard from "./tribe-card/TribeCardController";
import TribeConfig from "./tribe-config/tribe-config";
import TribeList from "./tribe-list/tribe-list";
import Welcome from "./welcome/WelcomeDirective";
import TribeBrowserDirective from "./tribebrowser/TribeBrowserDirective";
import Statistics from "./statistics/statistics";
import RetiredPlayers from "./retired-players/retired-players";
import LoginChooser from './login-chooser/login-chooser'
import {module} from "angular";
import debounce from 'lodash.debounce'

module('coupling.component', [
    'coupling.services',
    'ngFitText',
    PairAssignments.name,
    History.name,
    PinList.name,
    PlayerConfig.name,
    PlayerRoster.name,
    Prepare.name,
    RetiredPlayers.name,
    ServerMessageDirective.name,
    Statistics.name,
    TribeCard.name,
    TribeConfig.name,
    TribeList.name,
    Welcome.name,
    HeatMap.name,
    TribeBrowserDirective.name,
    LoginChooser.name
])
    .config(['fitTextConfigProvider', function (fitTextConfigProvider) {
        fitTextConfigProvider.config = {
            debounce: debounce,
            delay: 1000
        };
    }]);