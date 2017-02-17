export const tribeResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.getTribe($route.current.params.tribeId);
}];

export const playersResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.getPlayers($route.current.params.tribeId);
}];