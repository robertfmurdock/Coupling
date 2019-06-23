import History from "./history/history";
import PairAssignments from "./pair-assignments/PairAssignmentsDirective";
import PinList from "./pin-list/pin-list";
import PlayerConfig from "./player-config/player-config";
import Prepare from "./prepare/PrepareDirective";
import TribeCard from "./tribe-card/TribeCardController";
import TribeConfig from "./tribe-config/tribe-config";
import TribeList from "./tribe-list/tribe-list";
import Welcome from "./welcome/WelcomeDirective";
import TribeBrowserDirective from "./tribebrowser/TribeBrowserDirective";
import Statistics from "./statistics/statistics";
import RetiredPlayers from "./retired-players/retired-players";
import {module} from "angular";

module('coupling.component', [
    'coupling.services',
    PairAssignments.name,
    History.name,
    PinList.name,
    PlayerConfig.name,
    Prepare.name,
    RetiredPlayers.name,
    Statistics.name,
    TribeCard.name,
    TribeConfig.name,
    TribeList.name,
    Welcome.name,
    TribeBrowserDirective.name,
]);