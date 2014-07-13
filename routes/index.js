var config = require('../config');

exports.index = function (request, response) {
    if (config.requiresAuthentication && !request.isAuthenticated()) {
        response.redirect('/auth/google');
    } else {
        response.render('index', { title: 'Coupling' });
    }
};

exports.partials = function (req, res) {
    res.render('partials/' + req.params.name);
};