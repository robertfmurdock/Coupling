export const tribeResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.getTribe($route.current.params.tribeId);
}];