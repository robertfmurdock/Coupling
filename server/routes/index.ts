var config = require('../../config');

export function index(request, response) {
  if (!request.isAuthenticated()) {
    response.redirect('/welcome');
  } else {
    response.render('index', {title: 'Coupling', buildDate: config.buildDate, gitRev: config.gitRev});
  }
}

export function welcome(request, response) {
  response.render('welcome', {buildDate: config.buildDate, gitRev: config.gitRev});
}

export function partials(request, response) {
  response.render('partials/' + request.params.name);
}

export function components(request, response) {
  var formatPathForViewEngine = request.path.slice(1, -5);
  response.render(formatPathForViewEngine);
}