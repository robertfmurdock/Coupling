const webpackRunner = require('../webpackRunner');
var config = require('./webpack.config');

const runHelpers = require('../run-helpers');

const startJasmine = function () {
  return runHelpers.startJasmine('test/endpoint', '.tmp', 'test.js', __dirname + '/../../../test-output', 'endpoint.xml')
};

const removeTempDirectory = function () {
  return runHelpers.removeTempDirectory(__dirname + '/.tmp');
};

webpackRunner.run(config)
  .then(function () {
    process.env.PORT = 3001;
    return require('../../build/app').start()
  })
  .then(startJasmine)
  .finally(removeTempDirectory)
  .then(function () {
    process.exit(0);
  }, function (err) {
    console.error(err);
    process.exit(1);
  });