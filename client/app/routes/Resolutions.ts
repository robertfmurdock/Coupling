import {Coupling} from "../services";

export const tribeResolution = ['$route', 'Coupling', function ($route, Coupling: Coupling) {
    return Coupling.getTribe($route.current.params.tribeId);
}];

export const playersResolution = ['$route', 'Coupling', function ($route, Coupling: Coupling) {
    return Coupling.getPlayers($route.current.params.tribeId);
}];

export const retiredPlayersResolution = ['$route', 'Coupling', function ($route, Coupling: Coupling) {
    return Coupling.getRetiredPlayers($route.current.params.tribeId);
}];