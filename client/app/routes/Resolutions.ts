import {Coupling} from "../services";
export const tribeResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.getTribe($route.current.params.tribeId);
}];

export const playersResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.getPlayers($route.current.params.tribeId);
}];

export const historyResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.getHistory($route.current.params.tribeId);
}];

export const pinsResolution = ['$route', 'Coupling', function ($route, Coupling: Coupling) {
    return Coupling.getPins($route.current.params.tribeId);
}];

export const tribesResolution = ['Coupling', function (Coupling) {
    return Coupling.getTribes();
}];
