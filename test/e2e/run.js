var webpack = require('webpack');
var webpackConfig = require('./webpack.config');
var fs = require('fs-extra');

const Promise = require('bluebird');
const ConfigParser = require('protractor/built/configParser').ConfigParser;
const Runner = require('protractor/built/runner').Runner;

new Promise(function (resolve, reject) {
  webpack(webpackConfig)
    .run(function (err, stats) {
      console.log(stats.toString('minimal'));
      if (err) {
        reject(err);
      }
      console.log('Starting tests:');
      resolve();
    });
})
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