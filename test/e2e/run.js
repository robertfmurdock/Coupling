var fs = require('fs-extra');
const Promise = require('bluebird');
const ConfigParser = require('protractor/built/configParser').ConfigParser;
const Runner = require('protractor/built/runner').Runner;
const webpackRunner = require('../webpackRunner');
var webpackConfig = require('./webpack.config');
const couplingApp = require('../../build/app');

process.env.PORT = 3001;
Promise.all([
  couplingApp.start(),
  webpackRunner.run(webpackConfig)
])
  .then(function () {
    const configParser = new ConfigParser();
    configParser.addFileConfig(__dirname + '/.tmp/config.js');
    const runner = new Runner(configParser.getConfig());
    return runner.run();
  })
  .finally(function () {
    fs.removeSync(__dirname + '/.tmp');
  })
  .then(function (exitCode) {
    process.exit(exitCode);
  }, function (err) {
    console.log('Error', err);
    process.exit(-1);
  });