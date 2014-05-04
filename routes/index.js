exports.index = function (request, response) {
    if (request.session.passport.user) {
        response.render('index', { title: 'Coupling' });
    } else {
        response.redirect('/auth/google');
    }
};

exports.partials = function (req, res) {
    res.render('partials/' + req.params.name);
};