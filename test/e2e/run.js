var fs = require('fs-extra');
const ConfigParser = require('protractor/built/configParser').ConfigParser;
const Runner = require('protractor/built/runner').Runner;
const webpackRunner = require('../webpackRunner');

var webpackConfig = require('./webpack.config');

webpackRunner.run(webpackConfig)
  .then(function () {
    const configParser = new ConfigParser();
    configParser.addFileConfig(__dirname + '/protractor-conf');
    const runner = new Runner(configParser.getConfig());
    return runner.run();
  })
  .finally(function () {
    fs.removeSync(__dirname + '/.tmp');
  })
  .then(function (exitCode) {
    process.exit(exitCode);
  }, function (exitCode) {
    process.exit(exitCode);
  });