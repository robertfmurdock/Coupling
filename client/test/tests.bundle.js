const clientTest = require('client_test');


const GoogleSignIn = require("../app/GoogleSignIn").default;

GoogleSignIn["checkForSignedIn"] = async () => Promise.resolve(true);

require('angular');
require('angular-route');
require('angular-mocks');
require("../app/app");

clientTest.setLogLevel();

require('./enzymeConfig');

const context = require.context('.', true, /.+\.spec\*?$/);
context.keys().forEach(context);

module.exports = context;