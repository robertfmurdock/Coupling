const webpackRunner = require('../../../test/webpackRunner');
let config = require('./webpack.config');

const runHelpers = require('../../../test/run-helpers');

const startJasmine = function () {
  return runHelpers.startJasmine('server/test/endpoint', '.tmp', 'test.js', __dirname + '/../../build/test-results/endpoint', 'endpoint.xml')
};

const removeTempDirectory = function () {
  return runHelpers.removeTempDirectory(__dirname + '/.tmp');
};

webpackRunner.run(config)
  .then(function () {
    process.env.PORT = "3001";
    return require('../../build/executable/app').start()
  })
  .then(startJasmine)
  .finally(removeTempDirectory)
  .then(function () {
    process.exit(0);
  }, function (err) {
    console.error(err);
    process.exit(1);
  });