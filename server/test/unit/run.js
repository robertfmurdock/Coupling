const runHelpers = require('../run-helpers');
const webpackRunner = require('../webpackRunner');
let config = require('./webpack.config');

const startJasmine = function () {
  return runHelpers.startJasmine('test/unit', '.tmp', 'test.js', __dirname + '/../../build/test-results/server.unit', 'server.unit');
};
const removeTempDirectory = function () {
  return runHelpers.removeTempDirectory(__dirname + '/.tmp');
};

webpackRunner.run(config)
  .then(startJasmine)
  .finally(removeTempDirectory)
  .then(function () {
    process.exit(0);
  }, function () {
    process.exit(1);
  });