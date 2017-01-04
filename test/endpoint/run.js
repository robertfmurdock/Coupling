var fs = require('fs-extra');
const webpackRunner = require('../webpackRunner');
var config = require('./webpack.config');

const runHelpers = require('../run-helpers');

var removeTempDirectory = function () {
  runHelpers.removeTempDirectory(__dirname + '/.tmp')
};

const startJasmine = function () {
  const jasmineSavePath = __dirname + '/../../../test-output';
  return runHelpers.startJasmine('.tmp', 'test.js', jasmineSavePath, 'endpoint.xml')
};

webpackRunner.run(config)
  .then(function () {
    process.env.PORT = 3001;
    return require('../../build/app').start()
  })
  .then(startJasmine)
  .finally(function () {
    removeTempDirectory();
  })
  .then(function () {
    process.exit(0);
  }, function (err) {
    console.error(err);
    process.exit(1);
  });