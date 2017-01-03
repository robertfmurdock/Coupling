const runHelpers = require('./run-helpers');
const webpackRunner = require('../../webpackRunner');
var config = require('./webpack.config');

const startJasmine = runHelpers.startJasmine;
const removeTempDirectory = runHelpers.removeTempDirectory;

webpackRunner.run(config)
  .then(startJasmine)
  .finally(removeTempDirectory)
  .then(function () {
    process.exit(0);
  }, function () {
    process.exit(1);
  });