const runHelpers = require('./../../run-helpers');
const webpackRunner = require('../../webpackRunner');
var config = require('./webpack.config');

const startJasmine = function () {
  runHelpers.startJasmine('.tmp', 'test.js', __dirname + '/../../../test-output');
};
const removeTempDirectory = function(){
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