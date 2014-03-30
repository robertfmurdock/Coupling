var tests = [];

Object.keys(window.__karma__.files).forEach(function(file) {
    if (/spec\.js$/.test(file)) {
        tests.push(file);
    }
});

require.config({
    // Karma serves files under /base, which is the basePath from your config file
    baseUrl: '/base/src',

    // example of using shim, to load non AMD libraries (such as underscore and jquery)
    paths: {
        'players': '../routes/players'
    },

    shim: {
        'underscore': {
            exports: '_'
        }
    },

    // dynamically load all test files
    deps: tests,

    // we have to kickoff jasmine, as it is asynchronous
    callback: window.__karma__.start
});
exports = {};
