const fs = require('fs-extra');
const Promise = require('bluebird');
const _ = require('underscore');
const webpackRunner = require('../webpackRunner');
const webpackConfig = require('./webpack.config');
const couplingApp = require('../../build/app');

const childProcess = require('child_process');

process.env.PORT = 3001;
let promise = Promise.all([
  couplingApp.start(),
  webpackRunner.run(webpackConfig)
]);

promise = promise
  .then(function () {
    return new Promise(function (resolve, reject) {
      const process = childProcess.fork(__dirname + '/forkProtractor');

      process.on('exit', function (code) {
        console.log('protractor fork code ' + code);
        if (code === 0)
          resolve(code);
        else {
          reject(code);
        }
      });
    });
  });

promise.finally(function () {
  fs.removeSync(__dirname + '/.tmp');
})
  .then(function (exitCode) {
    process.exit(exitCode);
  }, function (err) {
    console.log('Error', err);
    process.exit(-1);
  });