const config = require('../src/main/javascript/config/config');

export function index(expressEnv) {
    return function (request, response) {
        response.render('index', {
            title: 'Coupling',
            buildDate: config.buildDate,
            gitRev: config.gitRev,
            googleClientId: config.googleClientID,
            expressEnv: expressEnv,
            isAuthenticated: request.isAuthenticated()
        });
    }
}

export function partials(request, response) {
    response.render('partials/' + request.params.name);
}

export function components(request, response) {
    const formatPathForViewEngine = request.path.slice(1, -5);
    response.render(formatPathForViewEngine);
}