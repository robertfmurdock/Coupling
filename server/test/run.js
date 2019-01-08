const runHelpers = require('../../test/run-helpers');
const webpackRunner = require('../../test/webpackRunner');
let config = require('./webpack.config');

const startJasmine = function () {
  return runHelpers.startJasmine('server/test', '.tmp', 'test.js', __dirname + '/../../../test-output/server.unit', 'server.unit');
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